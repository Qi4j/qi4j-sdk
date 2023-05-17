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

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.library.logging.debug.Debug;
import org.qi4j.library.logging.log.service.LoggingService;

/**
 * The DebugOnConsoleSideEffect is just a temporary solution for logging output, until a more
 * robust framework has been designed.
 */
public class DebugOnConsoleSideEffect extends SideEffectOf<LoggingService>
    implements DebuggingService
{
    private static PrintStream OUT = System.err;

    private final ResourceBundle bundle;

    public DebugOnConsoleSideEffect( @Invocation Method thisMethod )
    {
        bundle = ResourceBundle.getBundle( thisMethod.getDeclaringClass().getName() );
    }

    @Override
    public int debugLevel()
    {
        return Debug.OFF;
    }

    @Override
    public void debug( Composite composite, String message )
    {
        String localized = bundle.getString( message );
        OUT.println( "DEBUG:" + getCompositeName( composite ) + ": " + localized );
    }

    private String getCompositeName( Composite composite )
    {
        return Qi4jAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types().findFirst().get().getName();
    }

    @Override
    public void debug( Composite composite, String message, Object param1 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1 );
        OUT.println( "DEBUG:" + getCompositeName( composite ) + ": " + formatted );
        if( param1 instanceof Throwable )
        {
            handleException( (Throwable) param1 );
        }
    }

    @Override
    public void debug( Composite composite, String message, Object param1, Object param2 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1, param2 );
        OUT.println( "DEBUG:" + getCompositeName( composite ) + ": " + formatted );
        if( param1 instanceof Throwable )
        {
            handleException( (Throwable) param1 );
        }
    }

    @Override
    public void debug( Composite composite, String message, Object... params )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, (Object) params );
        OUT.println( "DEBUG:" + getCompositeName( composite ) + ": " + formatted );
        if( params[ 0 ] instanceof Throwable )
        {
            handleException( (Throwable) params[ 0 ] );
        }
    }

    private void handleException( Throwable exception )
    {
        if( exception != null )
        {
            exception.printStackTrace( OUT );
        }
    }
}
