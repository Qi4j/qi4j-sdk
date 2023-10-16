package org.qi4j.messaging.pulsar;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import org.qi4j.api.messaging.Consumer;
import org.qi4j.spi.messaging.ConsumerFactory;

public class PulsarConsumerFactory
    implements ConsumerFactory
{
    @Override
    public Consumer create(String topicAlias)
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
