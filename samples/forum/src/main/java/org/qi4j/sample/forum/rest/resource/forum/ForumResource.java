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
package org.qi4j.sample.forum.rest.resource.forum;

import org.qi4j.api.identity.StringIdentity;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.library.rest.server.api.SubResources;
import org.qi4j.sample.forum.data.entity.Forum;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.library.rest.server.api.SubResources;
import org.restlet.resource.ResourceException;

/**
 * TODO
 */
public class ForumResource
    extends ContextResource
    implements SubResources
{
    @Override
    public void resource( String segment )
        throws ResourceException
    {
        selectFromManyAssociation( ObjectSelection.current().get( Forum.class ).boards(), StringIdentity.identityOf( segment ) );
        subResource( BoardResource.class );
    }
}
