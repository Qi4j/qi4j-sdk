package org.qi4j.spi.messaging;

import org.qi4j.api.messaging.Consumer;

public interface ConsumerFactory
{
    Consumer create(String topicAlias);
}
