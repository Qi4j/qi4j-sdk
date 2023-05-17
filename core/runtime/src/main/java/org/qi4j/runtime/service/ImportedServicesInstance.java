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
package org.qi4j.runtime.service;

import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivatorsInstance;

import static java.util.stream.Collectors.toList;

/**
 * JAVADOC
 */
public class ImportedServicesInstance
    implements Activation, ActivationEventListenerRegistration
{
    private final ImportedServicesModel servicesModel;
    private final List<ServiceReference<?>> serviceReferences;
    private final ActivationDelegate activation = new ActivationDelegate( this, false );

    public ImportedServicesInstance( ImportedServicesModel servicesModel,
                                     List<ServiceReference<?>> serviceReferences
    )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;
        for( ServiceReference serviceReference : serviceReferences )
        {
            serviceReference.registerActivationEventListener( activation );
        }
    }

    public Stream<ImportedServiceModel> models()
    {
        return servicesModel.stream();
    }

    @Override
    public void activate()
        throws ActivationException
    {
        Iterable<Activation> activatees = serviceReferences.stream().filter( Activation.class::isInstance )
                                                           .map( Activation.class::cast )
                                                           .collect( toList() );
        activation.activate( ActivatorsInstance.EMPTY, activatees );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    public Stream<ServiceReference<?>> visibleServices( final Visibility visibility )
    {
        return serviceReferences.stream()
            .filter( item ->
                         ( (ImportedServiceReferenceInstance) item ).serviceDescriptor()
                             .visibility()
                             .ordinal() >= visibility.ordinal()
            );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Services{" );
        String sep = " ";
        for( ServiceReference serviceReference : serviceReferences )
        {
            sb.append( sep ).
                append( serviceReference.identity() ).
                append( "(active=" ).append( serviceReference.isActive() ).append( ")" );
            sep = ", ";
        }
        return sb.append( " }" ).toString();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    public Stream<? extends ImportedServiceDescriptor> stream()
    {
        return servicesModel.stream();
    }

    public Stream<ServiceReference<?>> references()
    {
        return serviceReferences.stream();
    }

}
