package org.qi4j.tools.model.v2;

import org.qi4j.api.object.ObjectDescriptor;

import java.util.List;

public record Object(List<Class<?>> types, String visibility)
{
    Object(ObjectDescriptor descriptor)
    {
        this(
            descriptor.types().toList(),
            descriptor.visibility().toString()
        );
    }
}
