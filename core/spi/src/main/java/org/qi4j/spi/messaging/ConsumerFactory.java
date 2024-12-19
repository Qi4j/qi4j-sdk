package org.qi4j.spi.messaging;

import org.qi4j.api.messaging.MessageReceiver;

public interface ConsumerFactory
{
    MessageReceiver create( String topicAlias);
}
