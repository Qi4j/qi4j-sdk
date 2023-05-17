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

package org.qi4j.library.rest.admin;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

public class Qi4jFinder
    extends Finder
{
    @Structure
    private ObjectFactory factory;

    public Qi4jFinder()
    {
    }

    @Override
    public ServerResource create( Class<? extends ServerResource> targetClass, Request request, Response response )
    {
        return factory.newObject( targetClass );
    }
}
