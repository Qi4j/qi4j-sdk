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
 */
package org.qi4j.serialization.javaxjson.assembly;

import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.serialization.javaxjson.JavaxJsonAdapters;
import org.qi4j.serialization.javaxjson.JavaxJsonFactories;
import org.qi4j.serialization.javaxjson.JavaxJsonSerialization;
import org.qi4j.serialization.javaxjson.JavaxJsonSettings;
import org.qi4j.spi.serialization.JsonDeserializer;
import org.qi4j.spi.serialization.JsonSerialization;
import org.qi4j.spi.serialization.JsonSerializer;

public class JavaxJsonSerializationAssembler extends Assemblers.VisibilityIdentity<JavaxJsonSerializationAssembler>
{
    private JavaxJsonSettings settings;

    public JavaxJsonSerializationAssembler withJsonSettings( JavaxJsonSettings settings )
    {
        this.settings = settings;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ServiceDeclaration declaration = module.services( JavaxJsonSerialization.class )
                                               .withTypes( Serialization.class,
                                                           Serializer.class, Deserializer.class,
                                                           Converters.class,
                                                           JsonSerialization.class,
                                                           JsonSerializer.class, JsonDeserializer.class,
                                                           JavaxJsonFactories.class, JavaxJsonAdapters.class )
                                               .taggedWith( Serialization.Format.JSON )
                                               .visibleIn( visibility() );
        if( hasIdentity() )
        {
            declaration.identifiedBy( identity() );
        }
        if( settings != null )
        {
            declaration.setMetaInfo( settings );
        }
    }
}
