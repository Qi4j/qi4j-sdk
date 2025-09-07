package org.qi4j.tools.model.v2;

import org.qi4j.api.service.ServiceDescriptor;

import java.util.List;

public record Service(String identity, String visibility, Class<?> primaryType, boolean instantiateOnStartup, Class<?> configurationType,
                      Iterable<Class<?>> mixinTypes, Iterable<Property> properties, List<Activator> activators)
{
    public Service(ServiceDescriptor descriptor)
    {
        this(
            descriptor.identity().toString(),
            descriptor.visibility().toString(),
            descriptor.primaryType(),
            descriptor.isInstantiateOnStartup(),
            descriptor.configurationType(),
            descriptor.mixinTypes().toList(),
            descriptor.state().properties().map(Property::new).toList(),
            descriptor.activators().map(Activator::new).toList()
        );
    }
}
