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

import java.lang.reflect.Method;
import java.util.List;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.EntityCompositeType;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.api.util.Annotations;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.unitofwork.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.module.ModuleSpi;

import static org.qi4j.api.identity.HasIdentity.IDENTITY_METHOD;

/**
 * JAVADOC
 */
public final class EntityModel extends CompositeModel
    implements EntityDescriptor
{
    private final EntityCompositeType valueType;
    private final boolean queryable;

    public EntityModel( ModuleDescriptor module,
                        List<Class<?>> types,
                        Visibility visibility,
                        MetaInfo info,
                        EntityMixinsModel mixinsModel,
                        EntityStateModel stateModel,
                        CompositeMethodsModel compositeMethodsModel
    )
    {
        super( module, types, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

        this.valueType = EntityCompositeType.of( this );
        this.queryable = types.stream()
            .flatMap( Annotations.ANNOTATIONS_OF )
            .filter( Annotations.isType( Queryable.class ) )
            .map( annot -> ( (Queryable) annot ).value() )
            .findFirst()
            .orElse( true );
    }

    @Override
    public EntityCompositeType valueType()
    {
        return valueType;
    }

    @Override
    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public EntityStateModel state()
    {
        return (EntityStateModel) super.state();
    }

    public EntityInstance newInstance(ModuleUnitOfWork uow, ModuleSpi moduleInstance, EntityState state )
    {
        return new EntityInstance( uow, this, state );
    }

    public Object[] newMixinHolder()
    {
        return mixinsModel.newMixinHolder();
    }

    public Object newMixin( Object[] mixins,
                            EntityStateInstance entityState,
                            EntityInstance entityInstance,
                            Method method
    )
    {
        return ( (EntityMixinsModel) mixinsModel ).newMixin( entityInstance, entityState, mixins, method );
    }

    public EntityState newEntityState( EntityStoreUnitOfWork store, EntityReference reference )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( reference, this );

            // Set reference property
            PropertyDescriptor persistentPropertyDescriptor = state().propertyModelFor( IDENTITY_METHOD );
            entityState.setPropertyValue( persistentPropertyDescriptor.qualifiedName(), reference.identity() );

            return entityState;
        }
        catch( EntityAlreadyExistsException e )
        {
            throw new EntityCompositeAlreadyExistsException( reference );
        }
        catch( EntityStoreException e )
        {
            throw new ConstructionException( "Could not create new entity in store", e );
        }
        catch( ConstraintViolationException e )
        {
            e.setCompositeDescriptor( this );
            e.setIdentity( reference.identity() );
            throw e;
        }
    }

    public void initState( ModuleDescriptor module, EntityState entityState )
    {
        // Set new properties to default value
        state().properties().forEach(
            propertyDescriptor -> entityState.setPropertyValue( propertyDescriptor.qualifiedName(),
                                                                propertyDescriptor.resolveInitialValue( module ) ) );

        // Set new associations to null
        state().associations().forEach(
            associationDescriptor -> entityState.setAssociationValue( associationDescriptor.qualifiedName(),
                                                                      null ) );

        // Set new many-associations to empty
        state().manyAssociations().forEach(
            associationDescriptor -> entityState.manyAssociationValueOf( associationDescriptor.qualifiedName() ) );

        // Set new named-associations to empty
        state().namedAssociations().forEach(
            associationDescriptor -> entityState.namedAssociationValueOf( associationDescriptor.qualifiedName() ) );
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        ( (EntityMixinsModel) mixinsModel ).invokeLifecycle( create, mixins, instance, state );
    }
}
