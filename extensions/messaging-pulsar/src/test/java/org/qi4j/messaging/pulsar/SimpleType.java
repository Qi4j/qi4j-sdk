package org.qi4j.messaging.pulsar;

import org.qi4j.api.messaging.Sendable;
import org.qi4j.api.property.Property;

public interface SimpleType extends Sendable
{
    Property<String> name();
    Property<String> payload();
}
