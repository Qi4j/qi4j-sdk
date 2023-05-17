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
package org.qi4j.runtime.unitofwork;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.identity.Identity;
import org.qi4j.runtime.composite.FunctionStateResolver;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.module.ModuleSpi;

import static org.qi4j.api.identity.HasIdentity.IDENTITY_STATE_NAME;

/**
 * Implementation of EntityBuilder. Maintains an instance of the entity which
 * will not have its state validated until it is created by calling newInstance().
 */
public final class EntityBuilderInstance<T>
    implements EntityBuilder<T>
{
    private final EntityModel model;
    private final ModuleUnitOfWork uow;
    private final EntityStoreUnitOfWork store;
    private Identity identity;

    private final BuilderEntityState entityState;
    private final EntityInstance prototypeInstance;

    public EntityBuilderInstance(
        EntityDescriptor model,
        ModuleUnitOfWork uow,
        EntityStoreUnitOfWork store,
        Identity identity
    )
    {
        this( model, uow, store, identity, null );
    }

    public EntityBuilderInstance(
        EntityDescriptor model,
        ModuleUnitOfWork uow,
        EntityStoreUnitOfWork store,
        Identity identity,
        FunctionStateResolver stateResolver
    )
    {
        this.model = (EntityModel) model;
        this.uow = uow;
        this.store = store;
        this.identity = identity;
        EntityReference reference = EntityReference.create( identity );
        entityState = new BuilderEntityState( model, reference );
        this.model.initState( model.module(), entityState );
        if( stateResolver != null )
        {
            stateResolver.populateState( this.model, entityState );
        }
        entityState.setPropertyValue( IDENTITY_STATE_NAME, identity );
        prototypeInstance = this.model.newInstance( uow, (ModuleSpi) model.module().instance(), entityState );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public T instance()
    {
        checkValid();
        return prototypeInstance.proxy();
    }

    @Override
    public <K> K instanceFor( Class<K> mixinType )
    {
        checkValid();
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T newInstance()
        throws LifecycleException
    {
        checkValid();

        // Figure out whether to use given or generated reference
        Identity identity = (Identity) entityState.propertyValueOf( IDENTITY_STATE_NAME );
        EntityReference entityReference = EntityReference.create( identity );
        EntityState newEntityState = model.newEntityState( store, entityReference );

        prototypeInstance.invokeCreate();

        // Check constraints
        prototypeInstance.checkConstraints();

        entityState.copyTo( newEntityState );

        EntityInstance instance = model.newInstance( uow, (ModuleSpi) model.module().instance(), newEntityState );

        Object proxy = instance.proxy();

        // Add entity in UOW
        uow.addEntity( instance );

        // Invalidate builder
        this.identity = null;

        return (T) proxy;
    }

    private void checkValid()
        throws IllegalStateException
    {
        if( identity == null )
        {
            throw new IllegalStateException( "EntityBuilder is not valid after call to newInstance()" );
        }
    }
}
