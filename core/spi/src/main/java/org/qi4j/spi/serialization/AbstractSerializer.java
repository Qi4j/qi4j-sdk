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
package org.qi4j.spi.serialization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.api.structure.ModuleDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.function.Function;

/**
 * Base Serializer.
 * <p>
 * Provides default implementations for convenience API methods.
 * <p>
 * See {@link AbstractDeserializer}.
 */
public abstract class AbstractSerializer implements Serializer
{
    @Override
    public String serialize(ModuleDescriptor module, Serialization.Options options, @Optional Object object)
    {
        StringWriter writer = new StringWriter();
        serialize(module, options, writer, object);
        return writer.toString();
    }

    @Override
    public <T> Function<T, String> serializeFunction(ModuleDescriptor module, Serialization.Options options)
    {
        return object -> serialize(module, options, object);
    }

    @Override
    public byte[] toBytes(ModuleDescriptor module, Serialization.Options options, @Optional Object object)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialize(module, options, output, object);
        return output.toByteArray();
    }

    @Override
    public <T> Function<T, byte[]> toBytesFunction(ModuleDescriptor module, Serialization.Options options)
    {
        return object -> toBytes(module, options, object);
    }
}
