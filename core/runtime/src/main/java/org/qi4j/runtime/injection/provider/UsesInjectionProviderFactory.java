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
package org.qi4j.runtime.injection.provider;

import java.lang.reflect.Constructor;
import org.qi4j.api.composite.NoSuchTransientTypeException;
import org.qi4j.api.object.NoSuchObjectTypeException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.AccessibleObjects;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.bootstrap.InvalidInjectionException;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private static class UsesInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = uses.useForType( injectionType );

            if( usesObject == null && !dependency.optional() )
            {
                // No @Uses object provided
                // Try instantiating a Transient or Object for the given type
                Module moduleInstance = context.module().instance();

                try
                {
                    if( context.instance() != null )
                    {
                        uses = uses.use( context.instance() );
                    }
                    usesObject = moduleInstance.newTransient( injectionType, uses.toArray() );
                }
                catch( NoSuchTransientTypeException e )
                {
                    try
                    {
                        usesObject = moduleInstance.newObject( injectionType, uses.toArray() );
                    }
                    catch( NoSuchObjectTypeException e1 )
                    {
                        // Could not instantiate an instance - to try instantiate as plain class
                        try
                        {
                            usesObject = injectionType.newInstance();
                        }
                        catch( Throwable e2 )
                        {
                            // Could not instantiate - try with this as first argument
                            try
                            {
                                Constructor constructor = injectionType.getDeclaredConstructor( context.instance()
                                                                                                    .getClass() );
                                AccessibleObjects.accessible( constructor );
                                usesObject = constructor.newInstance( context.instance() );
                            }
                            catch( Throwable e3 )
                            {
                                // Really can't instantiate it - ignore
                            }
                        }
                    }
                }

                if( usesObject != null )
                {
                    context.setUses( context.uses().use( usesObject ) ); // Use this for other injections in same graph
                }

                return usesObject;
            }
            else
            {
                return usesObject;
            }
        }
    }
}
