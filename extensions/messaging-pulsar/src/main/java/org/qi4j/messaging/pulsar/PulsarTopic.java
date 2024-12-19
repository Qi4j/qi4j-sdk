package org.qi4j.messaging.pulsar;

import org.qi4j.api.property.Property;

public interface PulsarTopic
{
    Property<String> tenant();
    Property<String> namespace();
    Property<String> topic();
}
