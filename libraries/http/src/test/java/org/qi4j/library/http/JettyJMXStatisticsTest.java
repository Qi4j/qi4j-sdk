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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.jmx.JMXAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.FreePortFinder;
import org.junit.jupiter.api.Test;

import static org.qi4j.library.http.Servlets.addServlets;
import static org.qi4j.library.http.Servlets.serve;

public class JettyJMXStatisticsTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        // START SNIPPET: jmx
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
        new JMXAssembler().assemble( module ); // Assemble both JettyService and JMX

        JettyConfiguration config = configModule.forMixin( JettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( FreePortFinder.findFreePortOnLoopback() );
        config.statistics().set( Boolean.TRUE ); // Set statistics default to TRUE in configuration

        // Hello world servlet related assembly
        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
        // END SNIPPET: jmx
    }

    /**
     * Run this test with -Djmxtest make it to not return so you can connect to the JVM using a JMX client.
     */
    @Test
    public void dummy()
        throws InterruptedException
    {
        if( !"false".equals( System.getProperty( "jmxtest", "false" ) ) )
        {
            Thread.sleep( Long.MAX_VALUE );
        }
    }
}
