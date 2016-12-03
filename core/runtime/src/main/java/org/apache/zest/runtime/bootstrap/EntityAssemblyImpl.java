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
package org.apache.zest.runtime.bootstrap;

import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.bootstrap.AssociationDeclarations;
import org.apache.zest.bootstrap.EntityAssembly;
import org.apache.zest.bootstrap.ManyAssociationDeclarations;
import org.apache.zest.bootstrap.NamedAssociationDeclarations;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.runtime.association.AssociationsModel;
import org.apache.zest.runtime.association.ManyAssociationsModel;
import org.apache.zest.runtime.association.NamedAssociationsModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.entity.EntityMixinsModel;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.runtime.entity.EntityStateModel;

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
