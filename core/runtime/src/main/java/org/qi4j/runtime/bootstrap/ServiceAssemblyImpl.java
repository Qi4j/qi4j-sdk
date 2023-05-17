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
package org.qi4j.runtime.bootstrap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.ServiceAssembly;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.structure.ModuleModel;

/**
 * Assembly of a Service.
 */
public final class ServiceAssemblyImpl extends CompositeAssemblyImpl
    implements ServiceAssembly
{
    Identity identity;
    boolean instantiateOnStartup = false;
    List<Class<? extends Activator<?>>> activators = new ArrayList<>();

    public ServiceAssemblyImpl( Class<?> serviceType )
    {
        super( serviceType );
        // The composite must always implement ServiceComposite, as a marker interface
        if( !ServiceComposite.class.isAssignableFrom( serviceType ) )
        {
            types.add( ServiceComposite.class );
        }
    }

    @Override
    public Identity identity()
    {
        return identity;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    ServiceModel newServiceModel(ModuleModel module, StateDeclarations stateDeclarations, AssemblyHelper helper )
    {
        try
        {
            buildComposite( helper, stateDeclarations );
            List<Class<? extends Activator<?>>> activatorClasses = Stream
                .concat( activators.stream(), activatorsDeclarations( types ) )
                .collect( Collectors.toList() );
            return new ServiceModel( module, types, visibility, metaInfo,
                                     new ActivatorsModel( activatorClasses ),
                                     mixinsModel, stateModel, compositeMethodsModel,
                                     identity, instantiateOnStartup );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }

    private Stream<Class<? extends Activator<?>>> activatorsDeclarations( List<? extends Class<?>> types )
    {
        return types.stream()
                    .flatMap( Classes::typesOf )
                    //.filter( type -> Annotations.annotationOn( type, Activators.class ) == null )
                    .flatMap( this::getAnnotations );
    }

    private Stream<? extends Class<? extends Activator<?>>> getAnnotations( Type type )
    {
        Activators activators = Annotations.annotationOn( type, Activators.class );
        if( activators == null )
        {
            return Stream.empty();
        }
        return Arrays.stream( activators.value() );
    }


}
