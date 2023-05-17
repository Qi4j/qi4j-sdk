/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.library.appbrowser;

import java.util.Stack;
import org.json.JSONException;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.util.HierarchicalVisitor;

public class Browser
{
    private final ApplicationDescriptor application;
    private final FormatterFactory factory;
    private final Stack<Formatter> stack = new Stack<>();

    public Browser( ApplicationDescriptor application, FormatterFactory factory )
    {
        this.application = application;
        this.factory = factory;
    }

    public void toJson()
        throws BrowserException
    {
        application.accept( new HierarchicalVisitor<Object, Object, BrowserException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws BrowserException
            {
                String simpleName = visited.getClass().getSimpleName();
                Formatter formatter = factory.create( simpleName );
                stack.push(formatter);
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    formatter.enter( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }

            @Override
            public boolean visitLeave( Object visited )
                throws BrowserException
            {
                Formatter formatter = stack.pop();
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    formatter.leave( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }

            @Override
            public boolean visit( Object visited )
                throws BrowserException
            {
                Formatter formatter = stack.peek();
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    formatter.visit( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }
        } );
    }
}
