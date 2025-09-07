package org.qi4j.tools.model.v2;

import org.qi4j.api.composite.TransientDescriptor;

import java.util.List;

public record Transient(Class<?> primaryType, String visibility, List<Class<?>> mixins, List<Property> properties)
{
    public Transient(TransientDescriptor descriptor)
    {
        this(descriptor.primaryType(),
            descriptor.visibility().toString(),
            descriptor.mixinTypes().toList(),
            descriptor.state().properties().map(Property::new).toList()
        );
    }
}
