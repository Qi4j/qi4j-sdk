package org.qi4j.api.common;

import java.time.Instant;
import org.qi4j.api.property.Property;

public interface HasTimestamp
{
    Property<Instant> timestamp();

}
