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
package org.qi4j.library.servlet;

import javax.servlet.ServletContext;
import org.qi4j.api.structure.Application;
import org.qi4j.library.servlet.lifecycle.AbstractQi4jServletBootstrap;

public final class Qi4jServletSupport
{

    public static final String APP_IN_CTX = "qi4j-application-servlet-context-attribute";

    /**
     * @param servletContext    ServletContext
     * @return                  The Application from the servlet context attribute previously set by {@link AbstractQi4jServletBootstrap}
     */
    public static Application application( ServletContext servletContext )
    {
        return ( Application ) servletContext.getAttribute( APP_IN_CTX ); // TODO try/catch and find a suitable Qi4j exception
    }

    private Qi4jServletSupport()
    {
    }

}
