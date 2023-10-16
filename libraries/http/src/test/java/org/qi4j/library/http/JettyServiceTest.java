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
package org.qi4j.library.http;

import java.util.Iterator;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpGet;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.FreePortFinder;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import static jakarta.servlet.DispatcherType.REQUEST;
import static org.qi4j.library.http.Servlets.addFilters;
import static org.qi4j.library.http.Servlets.addServlets;
import static org.qi4j.library.http.Servlets.filter;
import static org.qi4j.library.http.Servlets.serve;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public final class JettyServiceTest
    extends AbstractJettyTest
{
    private final int httpPort = FreePortFinder.findFreePortOnLoopback();

    @Override
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );

        // START SNIPPET: assembly
        // Assemble the JettyService
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );

        // Set HTTP port as JettyConfiguration default
        JettyConfiguration config = configModule.forMixin( JettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( httpPort );

        // Serve /helloWorld with HelloWorldServletService
        addServlets( serve( "/helloWorld" ).with( HelloWorldServletService.class ) ).to( module );

        // Filter requests on /* through provided UnitOfWorkFilterService
        addFilters( filter( "/*" ).through( UnitOfWorkFilterService.class ).on( REQUEST ) ).to( module );
        // END SNIPPET: assembly
    }

    @Test
    public final void testInstantiation()
        throws Throwable
    {
        Iterable<ServiceReference<JettyService>> services = serviceFinder.findServices( JettyService.class )
                                                                         .collect( Collectors.toList() );
        assertThat( services, notNullValue() );

        Iterator<ServiceReference<JettyService>> iterator = services.iterator();
        assertThat( iterator.hasNext(), Is.is( true ) );

        ServiceReference<JettyService> serviceRef = iterator.next();
        assertThat( serviceRef, notNullValue() );

        JettyService jettyService = serviceRef.get();
        assertThat( jettyService, notNullValue() );

        String output = defaultHttpClient.execute( new HttpGet( "http://127.0.0.1:" + httpPort + "/helloWorld" ),
                                                   stringResponseHandler );
        assertThat( output, equalTo( "Hello World" ) );
    }
}
