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
package org.qi4j.tools.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder;

import static org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor;

@Mixins( EnvisageServlet.Mixin.class )
public interface EnvisageServlet extends Servlet
{
    class Mixin extends HttpServlet
        implements EnvisageServlet
    {
        private JsonObject model;

        public Mixin( @Structure ApplicationDescriptor descriptor )
        {
            model = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( descriptor ).toJson();
        }

        @Override
        protected void doGet( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException
        {
            String pathInfo = req.getPathInfo();
            log( "Fetch " + pathInfo );
            if( isStatic( pathInfo ) )
            {
                serviceStatic( pathInfo, resp );
            }
            if( isJson( pathInfo ) )
            {
                serviceJson( resp );
            }
        }

        private boolean isStatic( String pathInfo )
        {
            return (pathInfo.equals( "/index.html" )
                   || pathInfo.startsWith( "/js/" )
                   || pathInfo.startsWith( "/css/" )
                   || pathInfo.startsWith( "/images/" ) )
                   && !pathInfo.contains( ".." )
                ;
        }

        private boolean isJson( String pathInfo )
        {
            return pathInfo.equals( "/model/" );
        }

        private void serviceStatic( String pathInfo, HttpServletResponse resp )
            throws IOException
        {
            ServletOutputStream out = resp.getOutputStream();
            try( InputStream resource = getClass().getClassLoader().getResourceAsStream( pathInfo ) )
            {
                if( resource == null )
                {
                    resp.setStatus( HttpServletResponse.SC_NOT_FOUND );
                }
                else
                {
                    copy( resource, out );
                }
            }
        }

        private void serviceJson( HttpServletResponse resp )
            throws IOException
        {
            if( model == null )
            {
                resp.setStatus( HttpServletResponse.SC_NO_CONTENT );
            }
            else
            {
                PrintWriter out = resp.getWriter();
                JsonWriter writer = Json.createWriter( out );
                writer.writeObject( model );
                writer.close();
                out.flush();
            }
        }

        private void copy( InputStream resource, ServletOutputStream out )
            throws IOException
        {
            int b;
            while( ( b = resource.read() ) >= 0 )
            {
                out.write( b );
            }
            out.flush();
        }
    }
}
