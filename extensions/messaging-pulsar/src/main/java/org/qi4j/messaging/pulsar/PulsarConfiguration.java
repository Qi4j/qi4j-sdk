package org.qi4j.messaging.pulsar;

import java.util.List;
import java.util.Map;
import org.qi4j.api.property.Property;

public interface PulsarConfiguration
{
    Property<Map<String, List<String>>> topicAliases();

    Property<List<Host>> hosts();
}
