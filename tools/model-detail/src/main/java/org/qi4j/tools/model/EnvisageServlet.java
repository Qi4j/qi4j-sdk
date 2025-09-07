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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.tools.model.v2.Application;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Mixins( EnvisageServlet.Mixin.class )
public interface EnvisageServlet extends Servlet
{
    class Mixin extends HttpServlet
        implements EnvisageServlet
    {
        private final String jsonModel;

        public Mixin( @Structure ApplicationDescriptor descriptor )
            throws JsonProcessingException
        {
            jsonModel = new ObjectMapper().writeValueAsString(new Application(descriptor));
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
            PrintWriter pw = resp.getWriter();
            pw.write( jsonModel );
            pw.flush();
            pw.close();
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
