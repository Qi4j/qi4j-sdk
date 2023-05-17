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
package org.qi4j.library.shiro.web;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.http.JettyConfiguration;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.library.shiro.web.assembly.HttpShiroAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.FreePortFinder;
import org.junit.jupiter.api.Test;

public class WebHttpShiroTest
    extends AbstractQi4jTest
{
    private int port;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        // START SNIPPET: assembly
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
        // END SNIPPET: assembly

        port = FreePortFinder.findFreePortOnLoopback();
        JettyConfiguration config = module.forMixin( JettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( port );

        // START SNIPPET: assembly
        new HttpShiroAssembler()
            .withConfig( configModule, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly

        configModule.forMixin( ShiroIniConfiguration.class )
                    .declareDefaults().iniResourcePath().set( "classpath:web-shiro.ini" );
    }

    @Test
    public void test()
    {
    }
}
