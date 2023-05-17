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
package org.qi4j.runtime.composite;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;

/**
 * Function based StateResolver.
 */
public class FunctionStateResolver
    implements StateResolver
{
    private final Function<PropertyDescriptor, Object> propertyFunction;
    private final Function<AssociationDescriptor, EntityReference> associationFunction;
    private final Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction;
    private final Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction;

    public FunctionStateResolver( Function<PropertyDescriptor, Object> propertyFunction,
                                  Function<AssociationDescriptor, EntityReference> associationFunction,
                                  Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
                                  Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction )
    {
        this.propertyFunction = propertyFunction;
        this.associationFunction = associationFunction;
        this.manyAssociationFunction = manyAssociationFunction;
        this.namedAssociationFunction = namedAssociationFunction;
    }

    @Override
    public Object getPropertyState( PropertyDescriptor descriptor )
    {
        return propertyFunction.apply( descriptor );
    }

    @Override
    public EntityReference getAssociationState( AssociationDescriptor descriptor )
    {
        return associationFunction.apply( descriptor );
    }

    @Override
    public Stream<EntityReference> getManyAssociationState( AssociationDescriptor descriptor )
    {
        return manyAssociationFunction.apply( descriptor );
    }

    @Override
    public Stream<Map.Entry<String, EntityReference>> getNamedAssociationState( AssociationDescriptor descriptor )
    {
        return namedAssociationFunction.apply( descriptor );
    }

    public void populateState( EntityModel model, EntityState state )
    {
        model.state().properties().forEach(
            propDesc ->
            {
                Object value = getPropertyState( propDesc );
                state.setPropertyValue( propDesc.qualifiedName(), value );
            } );
        model.state().associations().forEach(
            assDesc ->
            {
                EntityReference ref = getAssociationState( assDesc );
                state.setAssociationValue( assDesc.qualifiedName(), ref );
            } );
        model.state().manyAssociations().forEach(
            manyAssDesc ->
            {
                ManyAssociationState associationState = state.manyAssociationValueOf( manyAssDesc.qualifiedName() );
                // First clear existing ones
                associationState.clear();
                // then add the new ones.
                getManyAssociationState( manyAssDesc ).forEach( ref -> associationState.add( 0, ref ) );
            } );
        model.state().namedAssociations().forEach(
            namedAssDesc ->
            {
                NamedAssociationState associationState = state.namedAssociationValueOf( namedAssDesc.qualifiedName() );
                // First clear existing ones
                associationState.clear();
                // then add the new ones.
                getNamedAssociationState( namedAssDesc )
                    .forEach( entry -> associationState.put( entry.getKey(), entry.getValue() ) );
            } );
    }
}
