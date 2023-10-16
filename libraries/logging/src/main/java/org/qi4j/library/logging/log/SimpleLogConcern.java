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
package org.qi4j.library.logging.log;

import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.logging.log.service.LoggingService;

public final class SimpleLogConcern
    implements SimpleLog
{
    @Structure
    private Qi4jAPI api;
    @Optional
    @Service
    private LoggingService loggingService;
    private Composite composite;
    private String category;

    public SimpleLogConcern( @This Composite composite )
    {
        this.composite = composite;
        Class<?> type = Qi4jAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types().findFirst().orElse( null );
        category = type.getName();
    }

    @Override
    public void info( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void info( String message, Object param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void info( String message, Object param1, Object param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void info( String message, Object... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }

    @Override
    public void warning( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void warning( String message, Object param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void warning( String message, Object param1, Object param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void warning( String message, Object... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }

    @Override
    public void error( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void error( String message, Object param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void error( String message, Object param1, Object param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void error( String message, Object... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }
}