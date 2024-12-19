package org.qi4j.api.common;

import java.util.Map;
import org.qi4j.api.property.Property;

public interface HasMetadata
{
    @UseDefaults
    Property<Map<String,String>> metadata();
}
