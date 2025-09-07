package org.qi4j.tools.model.v2;

import org.qi4j.api.structure.ApplicationDescriptor;

import java.util.List;

import static org.qi4j.api.structure.Application.Mode;

public record Application(String name, Mode mode, String version, List<Layer> layers, List<Activator> activators)
{
    public Application(ApplicationDescriptor descriptor)
    {
        this(descriptor.name(),
            descriptor.mode(),
            descriptor.version(),
            descriptor.layers().sorted(new UsedLayerComparator()).map(Layer::new).toList(),
            descriptor.activators().map(Activator::new).toList()
        );
    }
}
