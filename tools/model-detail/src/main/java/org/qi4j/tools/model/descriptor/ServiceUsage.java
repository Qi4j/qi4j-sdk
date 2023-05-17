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
package org.qi4j.tools.model.descriptor;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public final class ServiceUsage
{

    private final Object ownerDescriptor;

    private final InjectedFieldDetailDescriptor field;

    private final ModuleDetailDescriptor module;

    private final LayerDetailDescriptor layer;

    public ServiceUsage( Object ownerDescriptor, InjectedFieldDetailDescriptor field, ModuleDetailDescriptor module, LayerDetailDescriptor layer )
    {
        this.ownerDescriptor = ownerDescriptor;
        this.field = field;
        this.module = module;
        this.layer = layer;
    }

    public Object ownerDescriptor()
    {
        return ownerDescriptor;
    }

    public InjectedFieldDetailDescriptor field()
    {
        return field;
    }

    public ModuleDetailDescriptor module()
    {
        return module;
    }

    public LayerDetailDescriptor layer()
    {
        return layer;
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "layer", layer().toString() );
        if( ownerDescriptor() instanceof CompositeDetailDescriptor )
        {
            builder.add( "composite", ( (CompositeDetailDescriptor) ownerDescriptor() ).toJson() );
        }
        if( ownerDescriptor() instanceof ObjectDetailDescriptor )
        {
            builder.add( "object", ( (ObjectDetailDescriptor) ownerDescriptor() ).toJson() );
        }
        return builder;
    }
}
