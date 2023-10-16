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

import java.util.EnumSet;
import org.qi4j.test.util.FreePortFinder;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;

import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;
import static jakarta.servlet.DispatcherType.INCLUDE;
import static jakarta.servlet.DispatcherType.REQUEST;

public class WebServletShiroTest
{

    @Test
    public void test()
            throws Exception
    {
        int port = FreePortFinder.findFreePortOnLoopback();
        Server server = new Server( port );
        try {

            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath( "/" );

            context.setInitParameter( "shiroConfigLocations", "classpath:web-shiro.ini" );
            context.addEventListener( new EnvironmentLoaderListener() );

            context.addFilter( ShiroFilter.class, "/*", EnumSet.of( REQUEST, FORWARD, INCLUDE, ERROR ) );

            server.setHandler( context );
            server.start();

            // HttpClient client = new DefaultHttpClient();
            // String result = client.execute( new HttpGet( "http://127.0.0.1:" + port + "/" ), new BasicResponseHandler() );

        } finally {
            server.stop();
        }

    }

}
