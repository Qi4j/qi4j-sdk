package org.qi4j.tools.model.v2;

import org.qi4j.api.value.ValueDescriptor;

import java.util.List;

public record Value(Class<?> primaryType, String visibility, List<Class<?>> mixins, List<Property> properties, List<Association> associations,
                    List<ManyAssociation> manyAssociations, List<NamedAssociation> namedAssociations)
{
    public Value(ValueDescriptor descriptor)
    {
        this(descriptor.primaryType(),
            descriptor.visibility().toString(),
            descriptor.mixinTypes().toList(),
            descriptor.state().properties().map(Property::new).toList(),
            descriptor.state().associations().map(Association::new).toList(),
            descriptor.state().manyAssociations().map(ManyAssociation::new).toList(),
            descriptor.state().namedAssociations().map(NamedAssociation::new).toList()
        );
    }
}
