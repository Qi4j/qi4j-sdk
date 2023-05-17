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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.composite.FunctionStateResolver;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.StateResolver;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.ModuleInstance;

import static org.qi4j.api.composite.CompositeInstance.compositeInstanceOf;

/**
 * Implementation of ValueBuilder with a prototype supplied
 */
public class ValueBuilderWithPrototype<T>
    implements ValueBuilder<T>
{
    private ValueInstance prototypeInstance;
    private final ValueModel valueModel;

    public ValueBuilderWithPrototype( ValueDescriptor compositeModelModule,
                                      ModuleInstance currentModule,
                                      T prototype
                                    )
    {
        valueModel = (ValueModel) compositeModelModule;
        MixinsModel mixinsModel = valueModel.mixinsModel();
        Object[] mixins = mixinsModel.newMixinHolder();
        final ValueStateInstance prototypeState = ( (ValueInstance) compositeInstanceOf( (Composite) prototype ) ).state();
        StateResolver resolver = new FunctionStateResolver(
            new PropertyDescriptorFunction( prototypeState ),
            new AssociationDescriptorEntityReferenceFunction( prototypeState ),
            new AssociationDescriptorIterableFunction( prototypeState ),
            new AssociationDescriptorMapFunction( prototypeState )
        );
        ValueStateInstance state = new ValueStateInstance( compositeModelModule, currentModule, resolver );
        ValueInstance valueInstance = new ValueInstance(
            valueModel,
            mixins,
            state
        );

        int i = 0;
        InjectionContext injectionContext = new InjectionContext( valueInstance, UsesInstance.EMPTY_USES, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        valueInstance.prepareToBuild();
        this.prototypeInstance = valueInstance;
    }

    @Override
    public T prototype()
    {
        verifyUnderConstruction();
        return prototypeInstance.proxy();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Class<T> primaryType()
    {
        return (Class<T>) valueModel.primaryType();
    }

    @Override
    public AssociationStateHolder state()
    {
        verifyUnderConstruction();
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        verifyUnderConstruction();
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    public T newInstance()
        throws ConstructionException
    {
        verifyUnderConstruction();

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        valueModel.checkConstraints( prototypeInstance.state() );

        try
        {
            return prototypeInstance.proxy();
        }
        finally
        {
            // Invalidate builder
            prototypeInstance = null;
        }
    }

    private void verifyUnderConstruction()
    {
        if( prototypeInstance == null )
        {
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );
        }
    }

    private static class PropertyDescriptorFunction
        implements Function<PropertyDescriptor, Object>
    {
        private final ValueStateInstance prototypeState;

        PropertyDescriptorFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Object apply( PropertyDescriptor descriptor )
        {
            return prototypeState.propertyFor( descriptor.accessor() ).get();
        }
    }

    private static class AssociationDescriptorEntityReferenceFunction
        implements Function<AssociationDescriptor, EntityReference>
    {
        private final ValueStateInstance prototypeState;

        AssociationDescriptorEntityReferenceFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public EntityReference apply( AssociationDescriptor descriptor )
        {
            return prototypeState.associationFor( descriptor.accessor() ).reference();
        }
    }

    private static class AssociationDescriptorIterableFunction
        implements Function<AssociationDescriptor, Stream<EntityReference>>
    {
        private final ValueStateInstance prototypeState;

        AssociationDescriptorIterableFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Stream<EntityReference> apply( AssociationDescriptor descriptor )
        {
            return prototypeState.manyAssociationFor( descriptor.accessor() ).references();
        }
    }

    private static class AssociationDescriptorMapFunction
        implements Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>>
    {
        private final ValueStateInstance prototypeState;

        AssociationDescriptorMapFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>> apply( AssociationDescriptor descriptor )
        {
            return prototypeState.namedAssociationFor( descriptor.accessor() ).references();
        }
    }
}
