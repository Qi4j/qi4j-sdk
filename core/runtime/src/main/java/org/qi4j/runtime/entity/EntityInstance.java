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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.MixinsInstance;

import static java.util.stream.Collectors.toList;

/**
 * Entity instance
 */
public final class EntityInstance
    implements CompositeInstance, MixinsInstance
{
    private final EntityComposite proxy;
    private final UnitOfWork uow;
    private final EntityModel entityModel;
    private final EntityReference reference;
    private final EntityState entityState;

    private Object[] mixins;
    private EntityStateInstance state;

    public EntityInstance( UnitOfWork uow,
                           EntityModel entityModel,
                           EntityState entityState
    )
    {
        this.uow = uow;
        this.entityModel = entityModel;
        this.reference = entityState.entityReference();
        this.entityState = entityState;

        proxy = (EntityComposite) entityModel.newProxy( this );
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return entityModel.invoke( this, this.proxy, method, args );
    }

    public EntityReference reference()
    {
        return reference;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T proxy()
    {
        return (T) proxy;
    }

    @Override
    public EntityDescriptor descriptor()
    {
        return entityModel;
    }

    @Override
    public <T> T newProxy( Class<T> mixinType )
        throws IllegalArgumentException
    {
        return entityModel.newProxy( this, mixinType );
    }

    @Override
    public Object invokeComposite( Method method, Object[] args )
        throws Throwable
    {
        return entityModel.invoke( this, proxy, method, args );
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return entityModel.metaInfo( infoType );
    }

    public EntityModel entityModel()
    {
        return entityModel;
    }

    @Override
    public Stream<Class<?>> types()
    {
        return entityModel.types();
    }

    @Override
    public ModuleDescriptor module()
    {
        return entityModel.module();
    }

    public UnitOfWork unitOfWork()
    {
        return uow;
    }

    public EntityState entityState()
    {
        return entityState;
    }

    @Override
    public EntityStateInstance state()
    {
        if( state == null )
        {
            initState();
        }
        return state;
    }

    public EntityStatus status()
    {
        return entityState.status();
    }

    @Override
    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        if( mixins == null )
        {
            initState();
        }

        Object mixin = methodInstance.getMixinFrom( mixins );

        if( mixin == null )
        {
            mixin = entityModel.newMixin( mixins, state, this, methodInstance.method() );
        }

        return methodInstance.invoke( proxy, params, mixin );
    }

    @Override
    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        return method.invoke( this, args );
    }

    private void initState()
    {
        if( !uow.isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }

        if( status() == EntityStatus.REMOVED )
        {
            throw new NoSuchEntityException(reference, entityModel.types(), unitOfWork().usecase() );
        }

        mixins = entityModel.newMixinHolder();
        state = new EntityStateInstance( entityModel.state(), uow, entityState );
    }

    @Override
    public int hashCode()
    {
        return reference.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        try
        {
            HasIdentity other = ( (HasIdentity) o );
            return other != null && other.identity().get().equals( reference.identity() );
        }
        catch( ClassCastException e )
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        if( Boolean.getBoolean( "qi4j.entity.print.state" ) )
        {
            return state().toString();
        }
        else
        {
            return reference.toString();
        }
    }

    public void remove( UnitOfWork unitOfWork )
    {
        invokeRemove();

        removeAggregatedEntities( unitOfWork );

        entityState.remove();
        mixins = null;
    }

    public void invokeCreate()
    {
        lifecyleInvoke( true );
    }

    private void invokeRemove()
    {
        lifecyleInvoke( false );
    }

    private void lifecyleInvoke( boolean create )
    {
        if( mixins == null )
        {
            initState();
        }

        entityModel.invokeLifecycle( create, mixins, this, state );
    }

    private void removeAggregatedEntities( UnitOfWork unitOfWork )
    {
        // Calculate aggregated Entities
        AssociationStateDescriptor stateDescriptor = entityModel.state();
        Stream.concat(
            stateDescriptor.associations()
                .filter( AssociationDescriptor::isAggregated )
                .map( association -> state.associationFor( association.accessor() ).get() )
                .filter( Objects::nonNull ),

            Stream.concat(
                stateDescriptor.manyAssociations()
                    .filter( AssociationDescriptor::isAggregated )
                    .flatMap( association -> state.manyAssociationFor( association.accessor() ).toList().stream() )
                    .filter( Objects::nonNull ),

                stateDescriptor.namedAssociations()
                    .filter( AssociationDescriptor::isAggregated )
                    .flatMap( association -> state.namedAssociationFor( association.accessor() )
                        .toMap()
                        .values()
                        .stream() )
                    .filter( Objects::nonNull )
            )
        ).distinct().collect( Collectors.toList() ).forEach( unitOfWork::remove );
    }

    public void checkConstraints()
    {
        try
        {
            state.checkConstraints();
        }
        catch( ConstraintViolationException e )
        {
            e.setCompositeDescriptor( descriptor() );
            e.setIdentity( entityState.entityReference().identity() );
            e.setInstanceString( proxy.toString() );
            e.setCompositeDescriptor( entityModel );
            throw e;
        }
    }
}
