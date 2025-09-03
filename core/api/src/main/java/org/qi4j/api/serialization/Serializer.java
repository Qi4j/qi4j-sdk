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
package org.qi4j.api.serialization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.structure.ModuleDescriptor;

import java.io.OutputStream;
import java.io.Writer;
import java.util.function.Function;

/**
 * Serializer.
 * <p>
 * All implementations must handle all {@link Serialization.Options}, they might extend them to provide more control.
 * See their respective documentation for the details.
 */
public interface Serializer
{
    void serialize(ModuleDescriptor module, Serialization.Options options, Writer writer, @Optional Object object);

    void serialize(ModuleDescriptor module, Serialization.Options options, OutputStream output, @Optional Object object);

    String serialize(ModuleDescriptor module, Serialization.Options options, @Optional Object object);

    <T> Function<T, String> serializeFunction(ModuleDescriptor module, Serialization.Options options);

    byte[] toBytes(ModuleDescriptor module, Serialization.Options options, @Optional Object object);

    <T> Function<T, byte[]> toBytesFunction(ModuleDescriptor module, Serialization.Options options);
}
