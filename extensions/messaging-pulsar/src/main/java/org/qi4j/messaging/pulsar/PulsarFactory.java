package org.qi4j.messaging.pulsar;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.pulsar.client.admin.Namespaces;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.common.policies.data.ClusterData;
import org.apache.pulsar.common.policies.data.RetentionPolicies;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.messaging.InvalidMessageKeyException;
import org.qi4j.api.messaging.MessageKey;
import org.qi4j.api.messaging.MessagingException;

public class PulsarFactory
{
    private static final int NUM_PARTITIONS = 3;

    private final PulsarClient client;
    private final KeyedObjectPool<PulsarTopic, Producer<byte[]>> producers;
    private org.apache.pulsar.client.admin.Topics adminTopics;
    private KeyedObjectPool<PulsarTopic, Producer<byte[]>> notifiers;

    public static PulsarFactory create( String bootstrapServers, String[] httpServices )
        throws MessagingException
    {
        try
        {
            PulsarClient client = createPulsarClient( bootstrapServers );
            PulsarAdmin admin = createPulsarAdmin( bootstrapServers, httpServices );
            return new PulsarFactory( client, admin );
        }
        catch( PulsarAdminException | PulsarClientException e )
        {
            throw new MessagingException( "Unable to create PulsarFactory." );
        }
    }

    public PulsarFactory( PulsarClient client )
    {
        this.client = client;
        producers = new GenericKeyedObjectPool<>( new ProducerPoolFactory( client ) );
    }

    public PulsarFactory( PulsarClient client, PulsarAdmin admin )
        throws PulsarAdminException
    {
        this( client );
        displayPeers( admin );
        initializeTenant( admin );
        initializeNamespaces( admin );
        initializeTopics( admin );
        notifiers = new GenericKeyedObjectPool<>( new NotificationProducerPoolFactory( adminTopics, client ) );
    }

    private static PulsarClient createPulsarClient( String bootstrapServers )
        throws PulsarClientException
    {
        PulsarClient client = PulsarClient.builder()
            .serviceUrl( bootstrapServers )     // example;  "pulsar://10.20.0.1:6550,10.20.0.2:6650,10.20.0.3:6650
            .build();
        return client;
    }

    private static PulsarAdmin createPulsarAdmin( String bootstrapServers, String[] httpServices ) throws PulsarClientException, PulsarAdminException
    {
        int randomIndex = new Random().nextInt( httpServices.length );
        String httpService = httpServices[randomIndex];
        PulsarAdmin admin = PulsarAdmin.builder().serviceHttpUrl( httpService ).build();
        if( !admin.clusters().getClusters().contains( Topics.CLUSTER ) )
        {
            ClusterData clusterData = ClusterData.builder().serviceUrl( bootstrapServers ).build();
            admin.clusters().createCluster( Topics.CLUSTER, clusterData );
        }
        return admin;
    }

    public static MessageKey create( Message<byte[]> msg )
        throws InvalidMessageKeyException
    {
        String key = msg.getKey();
        return MessageKey.parse( key );
    }

    protected void initializeTenant( PulsarAdmin admin ) throws PulsarAdminException
    {
        List<String> tenants = admin.tenants().getTenants();
        if( !tenants.contains( Topics.TENANT ) )
        {
            TenantInfo tenantInfo = TenantInfo.builder()
                .allowedClusters( Set.of( Topics.CLUSTER, "standalone" ) )
                .build();
            admin.tenants().createTenant( Topics.TENANT, tenantInfo );
        }
    }

    protected void initializeTopics( PulsarAdmin admin ) throws PulsarAdminException
    {
        adminTopics = admin.topics();
        List<String> topicList = adminTopics.getPartitionedTopicList( Topics.NAMESPACE );
        for( TopicDescriptor topic : Topics.ALL )
        {
            String topicName = topic.fqtn();
            createTopicIfNotExists( topicList, topicName );
        }
    }

    protected void initializeNamespaces( PulsarAdmin admin ) throws PulsarAdminException
    {
        Namespaces namespaces = admin.namespaces();
        List<String> namespaceNames = namespaces.getNamespaces( Topics.TENANT );
        if( !namespaceNames.contains( Topics.NAMESPACE ) )
        {
            namespaces.createNamespace( Topics.NAMESPACE );
        }
        namespaces.removeNamespaceMessageTTL( Topics.NAMESPACE );
        namespaces.removeRetention( Topics.NAMESPACE );

        if( !namespaceNames.contains( Topics.NAMESPACE_NOTIFICATIONS ) )
        {
            namespaces.createNamespace( Topics.NAMESPACE_NOTIFICATIONS );
        }
        namespaces.setNamespaceMessageTTL( Topics.NAMESPACE_NOTIFICATIONS, 6 * 3600 );
        namespaces.setRetention( Topics.NAMESPACE_NOTIFICATIONS, new RetentionPolicies( -1, 50 ) );
    }

    protected void displayPeers( PulsarAdmin admin ) throws PulsarAdminException
    {
        ClusterData cluster = admin.clusters().getCluster( Topics.CLUSTER );
        LinkedHashSet<String> peers = cluster.getPeerClusterNames();
    }

    public Closeable consumer( String consumerName, TopicDescriptor topic, MessageListener listener )
    {
        ConsumerBuilder<byte[]> builder =
            client.newConsumer( Schema.BYTES )
                .subscriptionName( consumerName )
                .consumerName( consumerName )
                .subscriptionType( SubscriptionType.Key_Shared )
                .autoUpdatePartitions( true )
                .autoUpdatePartitionsInterval( 1, TimeUnit.HOURS )
                .enableRetry( true )
                .topic( topic.fqtn() )
                .subscriptionMode( SubscriptionMode.Durable )
                .subscriptionTopicsMode( RegexSubscriptionMode.PersistentOnly )
                .messageListener( new MessageListenerWrapper( listener, consumerName ) );
        try
        {
            Consumer<byte[]> subscribe = builder.subscribe();
            LOG.info( "Created consumer [" + consumerName + "] on " + topic );
            return subscribe;
        }
        catch( PulsarClientException e )
        {
            String message = "Unable to create consumer " + consumerName + " on topic " + topic.fqtn();
            MessageKey key = new MessageKey( 2, ADMIN_ORG, topic.fqtn() );
            notify( errorCondition( ADMIN_ORG, consumerName, key, new byte[0], message ) );
            return null;
        }
    }

    public void send(String producerName, TopicDescriptor topic, MessageKey key, byte[] data )
    {
        try
        {
            Producer<byte[]> producer = producers.borrowObject( topic );
            producer.newMessage()
                .key( key.toString() )
                .value( data )
                .sendAsync()
                .handle( ( messageId, throwable ) ->
                {
                    if( throwable != null )
                    {
                        sendMessageExceptionOccurred( producerName, topic, key, data, throwable );
                    }
                    try
                    {
                        producers.returnObject( topic, producer );
                    }
                    catch( Exception e )
                    {
                        closeProducer( producer );
                    }
                    return messageId;
                } )
            ;
        }
        catch( Throwable e )
        {
            sendMessageExceptionOccurred( producerName, topic, key, data, e );
        }
    }

    private void sendMessageExceptionOccurred( String producerName, TopicDescriptor topic, MessageKey key, byte[] data, Throwable t )
    {
        LOG.error( "Unable to send message: [" + producerName + "] on " + topic + " with key=" + key + ", value=" + Messenger.toString( data ), t );
        String message = "Unable to produce to " + topic.fqtn();
        notify( errorCondition( ADMIN_ORG, producerName, key, data, message, t ) );
    }

    private void closeProducer( Producer<byte[]> producer )
    {
        try
        {
            producer.close();
        }
        catch( PulsarClientException ex )
        {
            // ignore, nothing more to do
        }
    }

    public void acknowledge( Consumer<byte[]> consumer, Message<byte[]> msg )
    {
        consumer.acknowledgeAsync( msg )
            .handle( ( unused, throwable ) ->
                {
                    if( throwable != null )
                    {
                        LOG.error( "Unable to ACK pulsar message: " + consumer.getConsumerName() + "/" + msg.getTopicName() );
                    }
                    return null;
                }
            );
    }

    @Override
    public void notify( Notice notice )
    {
        if( notice instanceof ErrorCondition )
        {
            LOG.error( "Unexpected exception:\n" + notice );
        }
        else
        {
            LOG.info( "Notification:\n{}", notice.toNotification() );
        }
        try
        {
            final TopicDescriptor topic = new TopicDescriptor( Topics.NAMESPACE_NOTIFICATIONS, "notifications-" + notice.orgId() );
            final Producer<byte[]> errorProducer = notifiers.borrowObject( topic );
            final String key = notice.sendingKey();
            final byte[] bytes = mapper.writeValueAsBytes( notice.toNotification() );
            errorProducer.newMessage()
                .key( key )
                .value( bytes )
                .sendAsync()
                .handle( ( messageId, throwable ) ->
                    {
                        if( throwable != null )
                        {
                            LOG.error( "Unable to send Notification to topic: " + notice, throwable );
                        }
                        try
                        {
                            notifiers.returnObject( topic, errorProducer );
                        }
                        catch( Exception e )
                        {
                            LOG.error( "Unable to return producer: " + topic.fqtn(), throwable );
                        }
                        return null;
                    }
                );
        }
        catch( Throwable e )
        {
            LOG.error( "Unable to send Notification to topic: " + notice, e );
        }
    }

    public void close()
        throws Exception
    {
        if( client != null )
        {
            client.close();
        }
    }

    private void createTopicIfNotExists( List<String> topicList, String topicName )
    {
        String fullName = "persistent://" + topicName;
        if( !topicList.contains( fullName ) )
        {
            try
            {
                adminTopics.createPartitionedTopic( topicName, NUM_PARTITIONS );
//                LOG.info( "    Pulsar " + topicName + " created." );
            }
            catch( PulsarAdminException e )
            {
                // expected after restarts.
            }
            topicList.add( fullName );
        }
    }

    private class MessageListenerWrapper
        implements org.apache.pulsar.client.api.MessageListener<byte[]>
    {
        private final MessageListener listener;
        private final String consumerName;

        public MessageListenerWrapper( MessageListener listener, String consumerName )
        {
            this.listener = listener;
            this.consumerName = consumerName;
        }

        @Override
        public void received( Consumer<byte[]> consumer, Message<byte[]> msg )
        {
            try
            {
                MessageKey key = create( msg );
                listener.messageReceived( key, msg.getValue() );
            }
            catch( InvalidMessageKeyException e )
            {
                PulsarFactory.this.notify( new ErrorCondition( ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Invalid message key.", e ) );
            }
            catch( IOException e )
            {
                PulsarFactory.this.notify( new ErrorCondition( ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Unable to deserialize.", e ) );
            }
            catch( Exception e )
            {
                PulsarFactory.this.notify( new ErrorCondition( ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Unexpected Exception", e ) );
            }
            finally
            {
                acknowledge( consumer, msg );
            }
        }
    }
}
