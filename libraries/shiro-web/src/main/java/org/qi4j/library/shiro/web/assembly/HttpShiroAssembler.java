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
package org.qi4j.library.shiro.web.assembly;

import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.library.shiro.web.EnvironmentLoaderService;
import org.qi4j.library.shiro.web.ShiroFilterService;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.library.shiro.web.EnvironmentLoaderService;
import org.qi4j.library.shiro.web.ShiroFilterService;

import static jakarta.servlet.DispatcherType.ASYNC;
import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;
import static jakarta.servlet.DispatcherType.INCLUDE;
import static jakarta.servlet.DispatcherType.REQUEST;
import static org.qi4j.library.http.Servlets.addContextListeners;
import static org.qi4j.library.http.Servlets.addFilters;
import static org.qi4j.library.http.Servlets.filter;
import static org.qi4j.library.http.Servlets.listen;

public class HttpShiroAssembler extends Assemblers.Config<HttpShiroAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        addContextListeners(
            listen().with( EnvironmentLoaderService.class ) )
            .to( module );

        addFilters(
            filter( "/*" )
                .through( ShiroFilterService.class )
                .on( REQUEST, FORWARD, INCLUDE, ERROR, ASYNC ) )
            .to( module );

        if( hasConfig() )
        {
            configModule()
                .entities( ShiroIniConfiguration.class )
                .visibleIn( configVisibility() );
        }
    }
}
