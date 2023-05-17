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
package org.qi4j.runtime.entity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.Classes;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.NamedAssociationInstance;
import org.qi4j.runtime.association.NamedAssociationModel;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.runtime.association.*;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.unitofwork.BuilderEntityState;

/**
 * TODO
 */
public final class EntityStateInstance
    implements AssociationStateHolder
{
    private Map<AccessibleObject, Object> state;

    private final EntityStateModel stateModel;
    private EntityState entityState;
    private final BiFunction<EntityReference, Type, Object> entityFunction;

    EntityStateInstance( EntityStateModel stateModel, final UnitOfWork uow, EntityState entityState )
    {
        this.stateModel = stateModel;
        this.entityState = entityState;

        entityFunction = ( entityReference, type ) -> uow.get( Classes.RAW_CLASS.apply( type ), entityReference.identity() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();

        Property<T> property = (Property<T>) state.get( accessor );

        if( property == null )
        {
            PropertyModel entityPropertyModel = stateModel.propertyModelFor( accessor );
            property = new EntityPropertyInstance<>(
                entityState instanceof BuilderEntityState
                ? entityPropertyModel.getBuilderInfo()
                : entityPropertyModel,
                entityState );
            state.put( accessor, property );
        }

        return property;
    }

    @Override
    public Stream<Property<?>> properties()
    {
        return stateModel.properties().map( descriptor -> propertyFor( descriptor.accessor() ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Association<T> associationFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();
        Association<T> association = (Association<T>) state.get( accessor );

        if( association == null )
        {
            final AssociationModel associationModel = stateModel.getAssociation( accessor );
            association = new AssociationInstance<>(
                entityState instanceof BuilderEntityState ? associationModel.builderInfo() : associationModel,
                entityFunction,
                new Property<EntityReference>()
                {
                    @Override
                    public EntityReference get()
                    {
                        return entityState.associationValueOf( associationModel.qualifiedName() );
                    }

                    @Override
                    public void set( EntityReference newValue )
                        throws IllegalArgumentException, IllegalStateException
                    {
                        entityState.setAssociationValue( associationModel.qualifiedName(), newValue );
                    }
                } );
            state.put( accessor, association );
        }
        return association;
    }

    @Override
    public Stream<? extends Association<?>> allAssociations()
    {
        return stateModel.associations().map( descriptor -> associationFor( descriptor.accessor() ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ManyAssociation<T> manyAssociationFor( AccessibleObject accessor )
    {
        Map<AccessibleObject, Object> state = state();
        ManyAssociation<T> manyAssociation = (ManyAssociation<T>) state.get( accessor );
        if( manyAssociation == null )
        {
            ManyAssociationModel associationModel = stateModel.getManyAssociation( accessor );
            manyAssociation = new ManyAssociationInstance<>(
                entityState instanceof BuilderEntityState ? associationModel.builderInfo() : associationModel,
                entityFunction,
                entityState.manyAssociationValueOf( associationModel.qualifiedName() ) );
            state.put( accessor, manyAssociation );
        }
        return manyAssociation;
    }

    @Override
    public Stream<ManyAssociation<?>> allManyAssociations()
    {
        return stateModel.manyAssociations().map( descriptor -> manyAssociationFor( descriptor.accessor() ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> NamedAssociation<T> namedAssociationFor( AccessibleObject accessor )
    {
        Map<AccessibleObject, Object> state = state();
        NamedAssociation<T> namedAssociation = (NamedAssociation<T>) state.get( accessor );
        if( namedAssociation == null )
        {
            NamedAssociationModel associationModel = stateModel.getNamedAssociation( accessor );
            namedAssociation = new NamedAssociationInstance<>(
                entityState instanceof BuilderEntityState ? associationModel.builderInfo() : associationModel,
                entityFunction,
                entityState.namedAssociationValueOf( associationModel.qualifiedName() ) );
            state.put( accessor, namedAssociation );
        }
        return namedAssociation;
    }

    @Override
    public Stream<? extends NamedAssociation<?>> allNamedAssociations()
    {
        return stateModel.namedAssociations().map( descriptor -> namedAssociationFor( descriptor.accessor() ) );
    }

    public void checkConstraints()
    {
        stateModel.properties().forEach( propertyDescriptor ->
                                         {
                                             Property<Object> property = this.propertyFor( propertyDescriptor.accessor() );
                                             propertyDescriptor.checkConstraints( property.get() );
                                         } );

        stateModel.associations().forEach( associationDescriptor ->
                                           {
                                               Association<Object> association = this.associationFor( associationDescriptor.accessor() );
                                               associationDescriptor.checkConstraints( association.get() );
                                           } );

        // TODO Should ManyAssociations and NamedAssociations be checked too?
    }

    private Map<AccessibleObject, Object> state()
    {
        if( state == null )
        {
            state = new HashMap<>();
        }

        return state;
    }

    @Override
    public String toString()
    {
        return entityState.toString();
    }
}
