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

package org.qi4j.library.logging.debug.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.time.SystemTime;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.logging.debug.records.CompositeDebugRecordEntity;
import org.qi4j.library.logging.debug.records.DebugRecord;
import org.qi4j.library.logging.debug.records.EntityDebugRecordEntity;
import org.qi4j.library.logging.debug.records.ServiceDebugRecordEntity;

public class DebuggingServiceMixin
    implements DebuggingService
{
    @Structure private UnitOfWorkFactory uowf;
    @This private Configuration<DebugServiceConfiguration> configuration;

    @Override
    public int debugLevel()
    {
        return configuration.get().debugLevel().get();
    }

    @Override
    public void debug( Composite composite, String message )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Object param1 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add( param1 );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Object param1, Object param2 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add( param1 );
            paramsList.add( param2 );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Object... params )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>( Arrays.asList( params ) );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    private void createDebugRecord( UnitOfWork uow, Composite composite, String message, List<Object> params )
    {
        if( composite instanceof ServiceComposite )
        {
            EntityBuilder<ServiceDebugRecordEntity> builder = uow.newEntityBuilder( ServiceDebugRecordEntity.class );
            ServiceDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( ( (ServiceComposite) composite ).identity().get().toString() );
            ServiceDebugRecordEntity slr = builder.newInstance();
        }
        else if( composite instanceof EntityComposite )
        {
            EntityBuilder<EntityDebugRecordEntity> builder = uow.newEntityBuilder( EntityDebugRecordEntity.class );
            EntityDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( (EntityComposite) composite );
            EntityDebugRecordEntity elr = builder.newInstance();
        }
        else
        {
            EntityBuilder<CompositeDebugRecordEntity> builder = uow.newEntityBuilder( CompositeDebugRecordEntity.class );
            CompositeDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( composite );
            CompositeDebugRecordEntity clr = builder.newInstance();
        }
    }

    private void setStandardStuff( Composite composite, String message, DebugRecord state, List<Object> params )
    {
        state.time().set( SystemTime.now() );
        state.message().set( message );
        state.compositeTypeName().set( getCompositeName( composite ) );
        state.threadName().set( Thread.currentThread().getName() );
        state.parameters().set( params );
    }

    private String getCompositeName( Composite composite )
    {
        return Qi4jAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types().findFirst().get().getName();
    }
}
