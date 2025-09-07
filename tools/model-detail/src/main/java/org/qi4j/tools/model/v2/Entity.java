package org.qi4j.tools.model.v2;

import org.qi4j.api.entity.EntityDescriptor;

import java.util.List;

public record Entity(String primaryType, String visibility, boolean queryable, List<Class<?>> mixins, List<Property> properties, List<Association> associations,
                     List<ManyAssociation> manyAssociations, List<NamedAssociation> namedAssociations)
{
    public Entity(EntityDescriptor descriptor)
    {
        this(descriptor.primaryType().toString(),
            descriptor.visibility().toString(),
            descriptor.queryable(),
            descriptor.mixinTypes().toList(),
            descriptor.state().properties().map(Property::new).toList(),
            descriptor.state().associations().map(Association::new).toList(),
            descriptor.state().manyAssociations().map(ManyAssociation::new).toList(),
            descriptor.state().namedAssociations().map(NamedAssociation::new).toList()
        );
    }
}
