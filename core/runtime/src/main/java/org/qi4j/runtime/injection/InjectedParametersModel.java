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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.composite.InjectedParametersDescriptor;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class InjectedParametersModel
    implements InjectedParametersDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    private final List<DependencyModel> parameterDependencies;

    public InjectedParametersModel()
    {
        parameterDependencies = new ArrayList<>();
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return parameterDependencies.stream();
    }

    // Context
    public Object[] newParametersInstance( InjectionContext context )
    {
        Object[] parametersInstance = new Object[ parameterDependencies.size() ];

        // Inject parameterDependencies
        for( int j = 0; j < parameterDependencies.size(); j++ )
        {
            DependencyModel dependencyModel = parameterDependencies.get( j );
            Object parameter = dependencyModel.inject( context );
            parametersInstance[ j ] = parameter;
        }

        return parametersInstance;
    }

    public void addDependency( DependencyModel dependency )
    {
        parameterDependencies.add( dependency );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( DependencyModel parameterDependency : parameterDependencies )
            {
                if( !visitor.visit( parameterDependency ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        return "InjectedParametersModel{" +
               "parameterDependencies=" + parameterDependencies +
               '}';
    }
}

