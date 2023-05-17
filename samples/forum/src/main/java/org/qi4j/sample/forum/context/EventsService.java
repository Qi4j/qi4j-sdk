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
package org.qi4j.sample.forum.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.sample.forum.domainevent.DomainEventValue;
import org.qi4j.sample.forum.domainevent.ParameterValue;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.sample.forum.domainevent.DomainEventValue;
import org.qi4j.sample.forum.domainevent.ParameterValue;
import org.restlet.Request;

import static java.util.stream.Collectors.toCollection;

/**
 * TODO
 */
@Mixins( EventsService.Mixin.class )
public interface EventsService
    extends Events, ServiceComposite
{
    public class Mixin
        implements InvocationHandler
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        Application application;

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            UnitOfWork unitOfWork = uowf.currentUnitOfWork();

            ValueBuilder<DomainEventValue> builder = vbf.newValueBuilder( DomainEventValue.class );
            DomainEventValue prototype = builder.prototype();
            prototype.version().set( application.version() );
            prototype.timestamp().set( unitOfWork.currentTime() );
            prototype.context().set( proxy.getClass().getSuperclass().getName().split( "\\$" )[ 0 ] );
            prototype.name().set( method.getName() );

            int idx = 0;
            for( Object arg : args )
            {
                idx++;
                String name = "param" + idx;
                ValueBuilder<ParameterValue> parameterBuilder = vbf.newValueBuilder( ParameterValue.class );
                parameterBuilder.prototype().name().set( name );
                parameterBuilder.prototype().value().set( arg );
                prototype.parameters().get().add( parameterBuilder.newInstance() );
            }

            ObjectSelection.current().selection().stream()
                           .map( Object::toString )
                           .collect( toCollection( () -> prototype.selection().get() ) );

            final DomainEventValue domainEvent = builder.newInstance();

            // Send event to all interested parties in the ObjectSelection
            for( Object selectedObject : ObjectSelection.current().selection() )
            {
                if( selectedObject instanceof Events )
                {
                    method.invoke( selectedObject, args );
                }
            }

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                @Override
                public void beforeCompletion()
                    throws UnitOfWorkCompletionException
                {
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if( status.equals( UnitOfWorkStatus.COMPLETED ) )
                    {
                        Request.getCurrent().getAttributes().put( "event", domainEvent );
                    }
                }
            } );

            return null;
        }
    }
}
