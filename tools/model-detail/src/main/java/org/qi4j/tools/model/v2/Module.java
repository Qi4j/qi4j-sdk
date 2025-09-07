package org.qi4j.tools.model.v2;

import org.qi4j.api.structure.ModuleDescriptor;

import java.util.List;

public record Module(String name,
                     List<Entity> entities,
                     List<Value> values,
                     List<Transient> transients,
                     List<Object> objects,
                     List<Service> services,
                     List<ImportedService> importedServices,
                     List<Activator> activators
)
{
    public Module(ModuleDescriptor descriptor)
    {
        this(
            descriptor.name(),
            descriptor.entityComposites().map(Entity::new).toList(),
            descriptor.valueComposites().map(Value::new).toList(),
            descriptor.transientComposites().map(Transient::new).toList(),
            descriptor.objects().map(Object::new).toList(),
            descriptor.serviceComposites().map(Service::new).toList(),
            descriptor.importedServices().map(ImportedService::new).toList(),
            descriptor.activators().map(Activator::new).toList()
        );
    }
}
