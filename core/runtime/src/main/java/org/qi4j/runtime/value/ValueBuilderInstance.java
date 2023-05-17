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
package org.qi4j.runtime.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.value.NoSuchValueTypeException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.composite.StateResolver;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.composite.StateResolver;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{

    private final ModuleInstance currentModule;
    private final ValueInstance prototypeInstance;

    public ValueBuilderInstance( ValueDescriptor compositeModel,
                                 ModuleInstance currentModule,
                                 StateResolver stateResolver
    )
    {
        ValueStateInstance state = new ValueStateInstance( compositeModel, currentModule, stateResolver );
        ValueModel model = (ValueModel) compositeModel;
        prototypeInstance = model.newValueInstance( state );
        prototypeInstance.prepareToBuild();
        this.currentModule = currentModule;
    }

    @Override
    public T prototype()
    {
        return prototypeInstance.proxy();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Class<T> primaryType()
    {
        return (Class<T>) prototypeInstance.descriptor().primaryType();
    }

    @Override
    public AssociationStateHolder state()
    {
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T newInstance()
        throws ConstructionException
    {
        Class<Composite> valueType = (Class<Composite>) prototypeInstance.types().findFirst().orElse( null );

        ValueDescriptor valueModel = currentModule.typeLookup().lookupValueModel( valueType );

        if( valueModel == null )
        {
            throw new NoSuchValueTypeException( valueType.getName(), currentModule.descriptor() );
        }
        return new ValueBuilderWithPrototype<>( valueModel, currentModule, prototype() ).newInstance();
    }
}
