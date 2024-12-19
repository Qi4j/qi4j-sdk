package org.qi4j.messaging.pulsar;

import java.util.function.Consumer;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.messaging.MessageKey;
import org.qi4j.api.messaging.MessageProducer;
import org.qi4j.api.messaging.MessageReceiver;
import org.qi4j.api.messaging.Sendable;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.messaging.ConsumerFactory;

public class PulsarMessageService
    implements ConsumerFactory, MessageProducer
{
    private static final int NUM_PARTITIONS = 3;

    @This
    private Configuration<PulsarConfiguration> configuration;

    @Structure
    private ValueBuilderFactory vbf;

    @Service
    private Serialization serialization;

//    private final PulsarClient client;
//    private final KeyedObjectPool<TopicDescriptor, Producer<byte[]>> producers;
//    private org.apache.pulsar.client.admin.Topics adminTopics;
//    private KeyedObjectPool<TopicDescriptor, Producer<byte[]>> notifiers;
//
//    public PulsarMessageService()
//    {
//        try
//        {
//            PulsarClient client = createPulsarClient( bootstrapServers );
//            PulsarAdmin admin = createPulsarAdmin( bootstrapServers, httpServices );
//            return new PulsarFactory( client, admin );
//        }
//        catch( PulsarAdminException | PulsarClientException e )
//        {
//            MessageKey key = new MessageKey( 1, 0, "bootstrap" );
//            throw new MessagingException( errorCondition( 0L, "pulsar-factory", key, null, "Unable to create PulsarFactory." ) );
//        }
//        this.client = client;
//        producers = new GenericKeyedObjectPool<>( new ProducerPoolFactory( client ) );
//    }
//
    @Override
    public <T extends Sendable> T createMessage( Class<T> type, Consumer<ValueBuilder<T>> closure )
    {
        ValueBuilder<T> builder = vbf.newValueBuilder( type );
        closure.accept( builder );
        return builder.newInstance();
    }

    @Override
    public void send( String topicAlias, Sendable msg )
    {

    }

    @Override
    public void send( String topicAlias, MessageKey key, Sendable msg )
    {

    }

    @Override
    public MessageReceiver create( String topicAlias)
    {return null;}
//    @Override
//    public Consumer create(String topicAlias)
//    {
//        String topic = "";  // TODO
//        ConsumerBuilder<byte[]> builder =
//            client.newConsumer(Schema.BYTES)
//                .subscriptionName(consumerName)
//                .consumerName(consumerName)
//                .subscriptionType(SubscriptionType.Key_Shared)
//                .autoUpdatePartitions(true)
//                .autoUpdatePartitionsInterval(1, TimeUnit.HOURS)
//                .enableRetry(true)
//                .topic(topic)
//                .subscriptionMode(SubscriptionMode.Durable)
//                .subscriptionTopicsMode(RegexSubscriptionMode.PersistentOnly)
//                .messageListener(new MessageListenerWrapper(listener, consumerName));
//        try
//        {
//            Consumer<byte[]> subscribe = builder.subscribe();
//            LOG.info("Created consumer [" + consumerName + "] on " + topic);
//            return subscribe;
//        }
//        catch( PulsarClientException e )
//        {
//            String message = "Unable to create consumer " + consumerName + " on topic " + topic.fqtn();
//            MessageKey key = new MessageKey(2, ADMIN_ORG, topic.fqtn());
//            notify(errorCondition(ADMIN_ORG, consumerName, key, new byte[0], message));
//            return null;
//        }
//    }
//
//    private class MessageListenerWrapper
//        implements org.apache.pulsar.client.api.MessageListener<byte[]>
//    {
//        private final MessageListener listener;
//        private final String consumerName;
//
//        public MessageListenerWrapper(MessageListener listener, String consumerName)
//        {
//            this.listener = listener;
//            this.consumerName = consumerName;
//        }
//
//        @Override
//        public void received(org.apache.pulsar.client.api.Consumer<byte[]> consumer, Message<byte[]> msg)
//        {
//            try
//            {
//                MessageKey key = create(msg);
//                listener.messageReceived(key, msg.getValue());
//            }
//            catch( InvalidMessageKeyException e )
//            {
//                this.notify(new ErrorCondition(ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Invalid message key.", e));
//            }
//            catch( IOException e )
//            {
//                this.notify(new ErrorCondition(ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Unable to deserialize.", e));
//            }
//            catch( Exception e )
//            {
//                this.notify(new ErrorCondition(ADMIN_ORG, consumerName, msg.getKey(), msg.getValue(), "Unexpected Exception", e));
//            }
//            finally
//            {
//                acknowledge(consumer, msg);
//            }
//        }
//    }

}
