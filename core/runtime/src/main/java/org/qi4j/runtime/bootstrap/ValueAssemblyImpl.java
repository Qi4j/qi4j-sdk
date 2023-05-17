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
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.bootstrap.ValueAssembly;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValueStateModel;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.bootstrap.ValueAssembly;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.StateModel;

/**
 * Declaration of a ValueComposite.
 */
public final class ValueAssemblyImpl
    extends CompositeAssemblyImpl
    implements ValueAssembly
{
    private AssociationsModel associationsModel;
    private ManyAssociationsModel manyAssociationsModel;
    private NamedAssociationsModel namedAssociationsModel;

    public ValueAssemblyImpl( Class<?> compositeType )
    {
        super( compositeType );
        // The composite must always implement ValueComposite, as a marker interface
        if( !ValueComposite.class.isAssignableFrom( compositeType ) )
        {
            types.add( ValueComposite.class );
        }
    }

    @Override
    protected StateModel createStateModel()
    {
        return new ValueStateModel( propertiesModel, associationsModel, manyAssociationsModel, namedAssociationsModel );
    }

    ValueModel newValueModel( ModuleDescriptor module,
                              StateDeclarations stateDeclarations,
                              AssemblyHelper helper
    )
    {
        associationsModel = new AssociationsModel();
        manyAssociationsModel = new ManyAssociationsModel();
        namedAssociationsModel = new NamedAssociationsModel();
        buildComposite( helper, stateDeclarations );
        return new ValueModel(
            module, types, visibility, metaInfo, mixinsModel, (ValueStateModel) stateModel, compositeMethodsModel );
    }

    protected AssociationsModel associationsModel()
    {
        return associationsModel;
    }

    protected ManyAssociationsModel manyAssociationsModel()
    {
        return manyAssociationsModel;
    }

    protected NamedAssociationsModel namedAssociationsModel()
    {
        return namedAssociationsModel;
    }
}
