/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.bootstrap;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Run this to start DCI Sample, version B
 */
public class Start8082
{
    private Server jetty;

    public static void main( String[] args ) throws Exception
    {
        new Start8082().start();
    }

    public void start() throws Exception
    {
        jetty = new Server();
        ServerConnector connector = new ServerConnector(jetty );
        connector.setIdleTimeout( 1000 * 60 * 60 );
        connector.setSoLingerTime( -1 );
        connector.setPort( 8082 );
        jetty.setConnectors( new Connector[]{connector} );

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath( "/" );
        webAppContext.setWar( "src/main/webapp" );
        jetty.setHandler( webAppContext );

        try
        {
            jetty.start();
            jetty.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit( 100 );
        }
    }

    public void stop() throws Exception
    {
        jetty.stop();
        jetty.join();
    }
}
