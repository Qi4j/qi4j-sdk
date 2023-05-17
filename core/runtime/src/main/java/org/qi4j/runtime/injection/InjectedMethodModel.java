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

package org.qi4j.runtime.injection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.qi4j.api.composite.InjectedMethodDescriptor;
import org.qi4j.api.util.AccessibleObjects;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.bootstrap.InjectionException;
import org.qi4j.bootstrap.InjectionException;

/**
 * JAVADOC
 */
public final class InjectedMethodModel
    implements InjectedMethodDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    // Model
    private Method method;
    private InjectedParametersModel parameters;

    public InjectedMethodModel( Method method, InjectedParametersModel parameters )
    {
        this.method = AccessibleObjects.accessible( method );
        this.parameters = parameters;
    }

    @Override
    public Method method()
    {
        return method;
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return parameters.dependencies();
    }

    // Context
    public void inject( InjectionContext context, Object instance )
        throws InjectionException
    {
        Object[] params = parameters.newParametersInstance( context );
        try
        {
            method.invoke( instance, params );
        }
        catch( IllegalAccessException e )
        {
            throw new InjectionException( e );
        }
        catch( InvocationTargetException e )
        {
            throw new InjectionException( e.getTargetException() );
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            parameters.accept( visitor );
        }
        return visitor.visitLeave( this );
    }
}
