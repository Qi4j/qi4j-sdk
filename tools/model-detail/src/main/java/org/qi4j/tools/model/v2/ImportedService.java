package org.qi4j.tools.model.v2;

import org.qi4j.api.service.ImportedServiceDescriptor;

public record ImportedService(String identity, Class<?> type, String visibility, Iterable<Class<?>> types, Class<?> serviceImporter)
{
    public ImportedService(ImportedServiceDescriptor descriptor)
    {
        this(
            descriptor.identity().toString(),
            descriptor.type(),
            descriptor.visibility().toString(),
            descriptor.types().toList(),
            descriptor.serviceImporter()
        );
    }
}
