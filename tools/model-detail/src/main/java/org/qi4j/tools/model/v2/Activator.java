package org.qi4j.tools.model.v2;

import org.qi4j.api.activation.ActivatorDescriptor;

public record Activator(String name)
{
    public Activator(ActivatorDescriptor descriptor)
    {
        this(
            descriptor.toString()
        );
    }
}
