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

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HelloWorldServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected final void doGet( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException
    {
        X509Certificate[] certs = ( X509Certificate[] ) req.getAttribute( "jakarta.servlet.request.X509Certificate" );
        PrintWriter writer = resp.getWriter();
        if ( certs != null && certs.length > 0 ) {
            writer.append( "Hello Mutual World" );
        } else {
            writer.append( "Hello World" );
        }
    }
}
