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
package org.qi4j.library.invocationcache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;

/**
 * Return value of @Cached calls on exceptions.
 * <p>
 * If an Exception occurs, try to reuse a previous result. Don't do anything on Throwables.
 * </p>
 */
@AppliesTo( Cached.class )
public class ReturnCachedValueOnExceptionConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @This
    private InvocationCache cache;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        boolean voidReturnType = method.getReturnType().equals( Void.TYPE );
        if( cache != null || voidReturnType ) // Skip if void return type or no InvocationCache has been defined.
        {
            String cacheName = method.getName();
            if( args != null )
            {
                cacheName += Arrays.asList( args );
            }
            try
            {
                // Invoke method
                Object result = next.invoke( proxy, method, args );
                // update cache
                cache.setCachedValue( cacheName, result );
                return result;
            }
            catch( Exception e )
            {
                // Try cache
                return cache.cachedValue( cacheName );
            }
        }
        // if no InvocationCache is present.
        return next.invoke( proxy, method, args );
    }
}
