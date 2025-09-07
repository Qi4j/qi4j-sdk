package org.qi4j.tools.model.v2;

import org.qi4j.api.association.AssociationDescriptor;

public record NamedAssociation(String name, String type, boolean immutable, boolean queryable)
{
    public NamedAssociation(AssociationDescriptor a)
    {
        this(a.qualifiedName().toString(), a.type().toString(), a.isImmutable(), a.queryable());
    }
}
