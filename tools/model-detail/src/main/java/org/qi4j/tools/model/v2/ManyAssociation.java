package org.qi4j.tools.model.v2;

import org.qi4j.api.association.AssociationDescriptor;

public record ManyAssociation(String name, String type, boolean immutable, boolean queryable)
{
    public ManyAssociation(AssociationDescriptor ma)
    {
        this(ma.qualifiedName().toString(), ma.type().toString(), ma.isImmutable(), ma.queryable());
    }
}
