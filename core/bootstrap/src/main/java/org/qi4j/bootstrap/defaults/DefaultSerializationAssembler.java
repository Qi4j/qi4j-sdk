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
package org.qi4j.bootstrap.defaults;

import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.serialization.javaxjson.JavaxJsonAdapters;
import org.qi4j.serialization.javaxjson.JavaxJsonFactories;
import org.qi4j.serialization.javaxjson.JavaxJsonSerialization;
import org.qi4j.spi.serialization.JsonDeserializer;
import org.qi4j.spi.serialization.JsonSerialization;
import org.qi4j.spi.serialization.JsonSerializer;

public class DefaultSerializationAssembler
    implements Assembler
{
    public static final String IDENTITY = "default-serialization";

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.services( JavaxJsonSerialization.class )
              .withTypes( Serialization.class,
                          Serializer.class, Deserializer.class,
                          Converters.class,
                          JsonSerialization.class,
                          JsonSerializer.class, JsonDeserializer.class,
                          JavaxJsonAdapters.class,
                          JavaxJsonFactories.class )
              .identifiedBy( IDENTITY )
              .taggedWith( Serialization.Format.JSON );
    }
}
