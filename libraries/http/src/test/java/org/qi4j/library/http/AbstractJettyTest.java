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
import java.security.GeneralSecurityException;
import java.security.Security;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractJettyTest
    extends AbstractQi4jTest
{
    protected CloseableHttpClient defaultHttpClient;
    protected ResponseHandler<String> stringResponseHandler = new ResponseHandler<String>()
    {
        @Override
        public String handleResponse( HttpResponse hr )
            throws IOException
        {
            return EntityUtils.toString( hr.getEntity(), "UTF-8" );
        }

    };

    @BeforeAll
    public static void beforeJettyTestClass()
    {
        // Be sure that no test trigger a DNS cache, needed by VirtualHosts test plumbing
        Security.setProperty( "networkaddress.cache.ttl", "0" );
    }

    @BeforeEach
    public void before()
        throws GeneralSecurityException, IOException
    {
        // Default HTTP Client
        defaultHttpClient = HttpClients.createDefault();
    }

    @AfterEach
    public void after()
        throws IOException
    {
        if( defaultHttpClient != null )
        {
            defaultHttpClient.close();
        }
    }
}
