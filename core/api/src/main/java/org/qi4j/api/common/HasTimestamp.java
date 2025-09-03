package org.qi4j.api.common;

import org.qi4j.api.property.Property;

import java.time.Instant;

public interface HasTimestamp
{
    Property<Instant> timestamp();

}
