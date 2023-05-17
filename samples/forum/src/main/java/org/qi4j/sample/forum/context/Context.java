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
package org.qi4j.sample.forum.context;

import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

/**
 * TODO
 */
public class Context
{
    @Structure
    protected Module module;

    protected <T> T role( Class<T> roleType, Object data )
    {
        return module.newObject( roleType, data );
    }

    protected <T> T role( Object object, Class<T> roleType )
    {
        return Qi4jAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (Composite) object ).newProxy( roleType );
    }
}
