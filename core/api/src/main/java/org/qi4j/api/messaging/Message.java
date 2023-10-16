package org.qi4j.api.messaging;

import java.time.Instant;
import java.util.Map;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

public interface Message
{
    Property<Instant> timestamp();

    @UseDefaults
    Property<Map<String,String>> metadata();

    Property<String> topic();

    @UseDefaults
    Property<String> key();

    Property<byte[]> payload();
}
