package org.qi4j.tools.model.v2;

import org.qi4j.api.property.PropertyDescriptor;

public record Property(String name, String type, boolean immutable, boolean queryable)
{
    public Property(PropertyDescriptor descriptor)
    {
        this(
            descriptor.qualifiedName().toString(),
            descriptor.type().toString(),
            descriptor.isImmutable(),
            descriptor.queryable()
        );
    }
}
