package org.qi4j.api.common;

import org.qi4j.api.property.Property;

import java.util.Map;

public interface HasMetadata
{
    @UseDefaults
    Property<Map<String, String>> metadata();
}
