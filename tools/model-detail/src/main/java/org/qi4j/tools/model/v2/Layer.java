package org.qi4j.tools.model.v2;

import org.qi4j.api.structure.LayerDescriptor;

import java.util.List;

public record Layer(String name, List<Layer> uses, List<Module> modules, List<? extends Activator> activators)
{
    public Layer(LayerDescriptor descriptor)
    {
        this(descriptor.name(),
            descriptor.usedLayers().layers().map(Layer::new).toList(),
            descriptor.modules().map(Module::new).toList(),
            descriptor.activators().map(Activator::new).toList()
        );
    }
}
