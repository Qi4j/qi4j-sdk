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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.HierarchicalVisitorAdapter;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldModel;

import static org.qi4j.api.util.Classes.interfacesOf;

/**
 * Base implementation of model for mixins. This records the mapping between methods in the Composite
 * and mixin implementations.
 */
public class MixinsModel
    implements Binder, VisitableHierarchy<Object, Object>, Dependencies
{
    protected final Map<Method, MixinModel> methodImplementation = new HashMap<>();
    protected final Map<Method, Integer> methodIndex = new HashMap<>();
    protected List<MixinModel> mixinModels = new ArrayList<>();

    private final Map<Class, Integer> mixinIndex = new HashMap<>();
    private final Set<Class<?>> mixinTypes = new LinkedHashSet<>();

    public Stream<Class<?>> mixinTypes()
    {
        return mixinTypes.stream();
    }

    public <T> boolean isImplemented( Class<T> mixinType )
    {
        return mixinTypes.contains( mixinType );
    }

    public List<MixinModel> mixinModels()
    {
        return mixinModels;
    }

    public MixinModel mixinFor( Method method )
    {
        return methodImplementation.get( method );
    }

    public MixinModel getMixinModel( Class mixinClass )
    {
        for( MixinModel mixinModel : mixinModels )
        {
            if( mixinModel.mixinClass().equals( mixinClass ) )
            {
                return mixinModel;
            }
        }
        return null;
    }

    public void addMixinType( Class mixinType )
    {
        Stream<? extends Type> stream = interfacesOf( mixinType );
        Stream<Class<?>> rawClass = stream.map( Classes.RAW_CLASS );
        rawClass.forEach( mixinTypes::add );
    }

    public void addMixinModel( MixinModel mixinModel )
    {
        mixinModels.add( mixinModel );
    }

    public void addMethodMixin( Method method, MixinModel mixinModel )
    {
        methodImplementation.put( method, mixinModel );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( MixinModel mixinModel : mixinModels )
            {
                mixinModel.accept( visitor );
            }
        }
        return visitor.visitLeave( this );
    }

    // Binding
    @Override
    public void bind( final Resolution resolution )
        throws BindingException
    {
        // Order mixins based on @This usages
        UsageGraph<MixinModel> deps = new UsageGraph<>( mixinModels, new Uses(), true );
        mixinModels = deps.resolveOrder();

        // Populate mappings
        for( int i = 0; i < mixinModels.size(); i++ )
        {
            MixinModel mixinModel = mixinModels.get( i );
            mixinIndex.put( mixinModel.mixinClass(), i );
        }

        for( Map.Entry<Method, MixinModel> methodClassEntry : methodImplementation.entrySet() )
        {
            methodIndex.put( methodClassEntry.getKey(), mixinIndex.get( methodClassEntry.getValue().mixinClass() ) );
        }

        for( MixinModel mixinModel : mixinModels )
        {
            mixinModel.accept( new HierarchicalVisitorAdapter<Object, Object, BindingException>()
            {
                @Override
                public boolean visitEnter( Object visited )
                    throws BindingException
                {
                    if( visited instanceof InjectedFieldModel)
                    {
                        InjectedFieldModel fieldModel = (InjectedFieldModel) visited;
                        fieldModel.bind( resolution.forField( fieldModel.field() ) );
                        return false;
                    }
                    else if( visited instanceof Binder )
                    {
                        Binder constructorsModel = (Binder) visited;
                        constructorsModel.bind( resolution );

                        return false;
                    }
                    return true;
                }

                @Override
                public boolean visit( Object visited )
                    throws BindingException
                {
                    if( visited instanceof Binder )
                    {
                        ( (Binder) visited ).bind( resolution );
                    }
                    return true;
                }
            } );
        }
    }

    // Context

    public Object[] newMixinHolder()
    {
        return new Object[ mixinIndex.size() ];
    }

    public FragmentInvocationHandler newInvocationHandler( final Method method )
    {
        MixinModel mixinModel = mixinFor( method );
        if( mixinModel == null )
        {
            throw new InternalError( "MixinModel can't be found." );
        }
        return mixinModel.newInvocationHandler( method );
    }

    public Stream<DependencyModel> dependencies()
    {
        return mixinModels.stream().flatMap( Dependencies::dependencies );
    }

    public Stream<Method> invocationsFor( final Class<?> mixinClass )
    {
        return methodImplementation.entrySet()
            .stream().filter( entry -> entry.getValue().mixinClass().equals( mixinClass ) )
            .map( Map.Entry::getKey );
    }

    private class Uses
        implements UsageGraph.Use<MixinModel>
    {
        @Override
        public Collection<MixinModel> uses( MixinModel source )
        {
            // System.out.println("BEGIN> MixinsModel.Uses.uses( "+source+" )");
            Iterable<Class<?>> thisMixinTypes = source.thisMixinTypes();
            List<MixinModel> usedMixinClasses = new ArrayList<>();
            // System.out.println("\tSource Mixin Types and Methods: ");
            for( Class thisMixinType : thisMixinTypes )
            {
                // System.out.println("\t\t"+thisMixinType);
                for( Method method : thisMixinType.getMethods() )
                {
                    // System.out.println("\t\t\t"+method);
                    if( !Modifier.isStatic( method.getModifiers() ) )
                    {
                        MixinModel used = methodImplementation.get( method );
                        if( used != null )
                        {
                            // TODO: Should we actually throw an Exception, since this means that a method implementation is missing??
                            usedMixinClasses.add( used );
                        }
                    }
                }
            }
            // System.out.println( "END>   MixinsModel.Uses.uses( " + source + " )" );
            return usedMixinClasses;
        }
    }
}
