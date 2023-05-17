/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.EntityAssembly;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.NamedAssociationDeclarations;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.entity.EntityMixinsModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.EntityStateModel;
import org.qi4j.bootstrap.*;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.StateModel;

/**
 * Declaration of a EntityComposite.
 */
public final class EntityAssemblyImpl
    extends CompositeAssemblyImpl
    implements EntityAssembly
{
    private AssociationDeclarations associationDeclarations;
    private ManyAssociationDeclarations manyAssociationDeclarations;
    private NamedAssociationDeclarations namedAssociationDeclarations;
    private AssociationsModel associationsModel;
    private ManyAssociationsModel manyAssociationsModel;
    private NamedAssociationsModel namedAssociationsModel;

    public EntityAssemblyImpl( Class<?> entityType )
    {
        super( entityType );
        // The composite must always implement EntityComposite, as a marker interface
        if( !EntityComposite.class.isAssignableFrom( entityType ) )
        {
            types.add( EntityComposite.class );
        }
    }

    @Override
    protected MixinsModel createMixinsModel()
    {
        return new EntityMixinsModel();
    }

    @Override
    protected AssociationsModel associationsModel()
    {
        return associationsModel;
    }

    @Override
    protected ManyAssociationsModel manyAssociationsModel()
    {
        return manyAssociationsModel;
    }

    @Override
    protected NamedAssociationsModel namedAssociationsModel()
    {
        return namedAssociationsModel;
    }

    @Override
    protected StateModel createStateModel()
    {
        return new EntityStateModel( propertiesModel, associationsModel, manyAssociationsModel, namedAssociationsModel );
    }

    EntityModel newEntityModel(
        ModuleDescriptor module,
        StateDeclarations stateDeclarations,
        AssociationDeclarations associationDecs,
        ManyAssociationDeclarations manyAssociationDecs,
        NamedAssociationDeclarations namedAssociationDecs,
        AssemblyHelper helper
    )
    {
        this.associationDeclarations = associationDecs;
        this.manyAssociationDeclarations = manyAssociationDecs;
        this.namedAssociationDeclarations = namedAssociationDecs;
        try
        {
            associationsModel = new AssociationsModel();
            manyAssociationsModel = new ManyAssociationsModel();
            namedAssociationsModel = new NamedAssociationsModel();
            buildComposite( helper, stateDeclarations );

            return new EntityModel( module, types, visibility, metaInfo,
                                    (EntityMixinsModel) mixinsModel,
                                    (EntityStateModel) stateModel, compositeMethodsModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }
}
