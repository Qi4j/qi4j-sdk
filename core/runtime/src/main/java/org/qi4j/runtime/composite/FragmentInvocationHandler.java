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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
abstract class FragmentInvocationHandler
    implements InvocationHandler
{
    private static final String COMPACT_TRACE = "qi4j.compacttrace";

    private static final CompactLevel compactLevel;

    static
    {
        compactLevel = CompactLevel.valueOf( System.getProperty( COMPACT_TRACE, "proxy" ) );
    }

    protected Object fragment;
    protected Method method;

    void setFragment( Object fragment )
    {
        this.fragment = fragment;
    }

    public void setMethod( Method method )
    {
        this.method = method;
    }

    Throwable cleanStackTrace( Throwable throwable, Object proxy, Method method )
    {
        if( compactLevel == CompactLevel.off )
        {
            return throwable;
        }

        StackTraceElement[] trace = throwable.getStackTrace();

        // Check if exception originated within Qi4j or JDK - if so then skip compaction
        if( trace.length == 0 || !isApplicationClass( trace[ 0 ].getClassName() ) )
        {
            return throwable;
        }

        int count = 0;
        for( int i = 0; i < trace.length; i++ )
        {
            StackTraceElement stackTraceElement = trace[ i ];
            if( !isApplicationClass( stackTraceElement.getClassName() ) )
            {
                // TODO: Should find stack entry outside Runtime, and compact beyond that
                trace[ i ] = null;
                count++;
            }
            else
            {
                boolean classOrigin = stackTraceElement.getClassName().equals( proxy.getClass().getSimpleName() );
                boolean methodOrigin = stackTraceElement.getMethodName().equals( method.getName() );
                if( classOrigin && methodOrigin && compactLevel == CompactLevel.proxy )
                {
                    // Stop removing if the originating method call has been located in the stack.
                    // For 'semi' and 'extensive' compaction, we don't and do the entire stack instead.
                    trace[ i ] = new StackTraceElement( proxy.getClass()
                                                             .getInterfaces()[ 0 ].getName(), method.getName(), null, -1 );
                    break; // Stop compacting this trace
                }
            }
        }

        // Create new trace array
        int idx = 0;
        StackTraceElement[] newTrace = new StackTraceElement[ trace.length - count ];
        for( StackTraceElement stackTraceElement : trace )
        {
            if( stackTraceElement != null )
            {
                newTrace[ idx++ ] = stackTraceElement;
            }
        }
        throwable.setStackTrace( newTrace );

        Throwable nested = throwable.getCause();
        if( nested != null )
        {
            //noinspection ThrowableResultOfMethodCallIgnored
            cleanStackTrace( nested, proxy, method );
        }
        for( Throwable suppressed : throwable.getSuppressed() )
        {
            //noinspection ThrowableResultOfMethodCallIgnored
            cleanStackTrace( suppressed, proxy, method );
        }
        return throwable;
    }

    private boolean isApplicationClass( String className )
    {
        boolean jdkInternals = isJdkInternals( className );
        if( compactLevel == CompactLevel.semi )
        {
            return !jdkInternals;
        }
        boolean qi4jRuntime = className.startsWith( "org.qi4j.runtime" );
        boolean stubClass = className.endsWith( FragmentClassLoader.GENERATED_POSTFIX );
        return !( stubClass ||
                  qi4jRuntime ||
                  jdkInternals );
    }

    private boolean isJdkInternals( String className )
    {
        return className.startsWith( "java.lang.reflect" )
               || className.startsWith( "reflect" )
               || className.startsWith( "com.sun.proxy" )
               || className.startsWith( "sun.reflect" );
    }
}
