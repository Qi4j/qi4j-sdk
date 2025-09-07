package org.qi4j.tools.model.v2;

import org.qi4j.api.association.AssociationDescriptor;

public record Association(String name, String type, boolean immutable, boolean queryable)
{
    public Association(AssociationDescriptor a)
    {
        this(a.qualifiedName().toString(), a.type().toString(), a.isImmutable(), a.queryable());
    }
}
