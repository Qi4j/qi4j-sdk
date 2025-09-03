package org.qi4j.api.messaging;

import org.qi4j.api.property.Property;

import java.util.List;

public interface ConsumerConfiguration
{
    Property<String> topicAlias();

    Property<List<String>> topics();

    Property<Long> connectTimeout();

    Property<Long> reconnectInterval();
}
