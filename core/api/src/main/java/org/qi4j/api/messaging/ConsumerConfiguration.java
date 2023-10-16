package org.qi4j.api.messaging;

import java.util.List;
import org.qi4j.api.property.Property;

public interface ConsumerConfiguration
{
    Property<String> topicAlias();

    Property<List<String>> topics();

    Property<Long> connectTimeout();

    Property<Long> reconnectInterval();
}
