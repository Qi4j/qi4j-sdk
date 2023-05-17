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

package org.qi4j.library.logging.log.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.time.SystemTime;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.logging.log.LogType;
import org.qi4j.library.logging.log.records.CompositeLogRecord;
import org.qi4j.library.logging.log.records.EntityLogRecord;
import org.qi4j.library.logging.log.records.LogRecord;
import org.qi4j.library.logging.log.records.ServiceLogRecord;


public abstract class LoggingServiceMixin
    implements LoggingService
{
    @Structure private UnitOfWorkFactory uowf;

    public void log( LogType type, Composite composite, String category, String message )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    public void log( LogType type, Composite composite, String category, String message, Object param1 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add( param1 );
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    public void log( LogType type, Composite composite, String category, String message, Object param1, Object param2 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add( param1 );
            paramsList.add( param2 );
            createLogRecord( uow, type, composite, category, message, paramsList );
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
    public void log( LogType type, Composite composite, String category, String message, Object... params )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Object> paramsList = new ArrayList<>( Arrays.asList( params ) );
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    private void createLogRecord( UnitOfWork uow, LogType type, Composite composite, String category, String message, List<Object> params )
    {
        if( composite instanceof ServiceComposite )
        {
            EntityBuilder<ServiceLogRecord> builder = uow.newEntityBuilder( ServiceLogRecord.class );
            ServiceLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( ( (ServiceComposite) composite ).identity().get().toString() );
            ServiceLogRecord slr = builder.newInstance();
        }
        else if( composite instanceof EntityComposite )
        {
            EntityBuilder<EntityLogRecord> builder = uow.newEntityBuilder( EntityLogRecord.class );
            EntityLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( (EntityComposite) composite );
            EntityLogRecord elr = builder.newInstance();
        }
        else
        {
            EntityBuilder<CompositeLogRecord> builder = uow.newEntityBuilder( CompositeLogRecord.class );
            CompositeLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( composite );
            CompositeLogRecord clr = builder.newInstance();
        }
    }

    private void setStandardStuff( LogType type, Composite composite, String category, String message,
                                   LogRecord state, List<Object> params )
    {
        state.logtype().set( type );
        state.time().set( SystemTime.now() );
        state.category().set( category );
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
