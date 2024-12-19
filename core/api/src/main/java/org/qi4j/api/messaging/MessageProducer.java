package org.qi4j.api.messaging;

import java.util.function.Consumer;
import org.qi4j.api.value.ValueBuilder;

public interface MessageProducer
{
    <T extends Sendable> T createMessage( Class<T> type, Consumer<ValueBuilder<T>> closure );

    void send(String topicAlias, Sendable msg);
    <K extends MessageKey> void send(String topicAlias, K key, Sendable msg);

}
