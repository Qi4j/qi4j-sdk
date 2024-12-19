package org.qi4j.messaging.pulsar;

import org.qi4j.api.property.Property;

public interface Host
{
    Property<String> hostname();
    Property<Integer> port();
}
