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

import org.qi4j.api.serialization.Serialization.Options;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueType;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

/**
 * Deserializer.
 * <p>
 * Provides methods and functions to deserialize objects and set of objects.
 */
public interface Deserializer
{
    <T> T deserialize(ModuleDescriptor module, Options options, ValueType valueType, InputStream state);

    <T> T deserialize(ModuleDescriptor module, Options options, ValueType valueType, Reader state);

    <T> T deserialize(ModuleDescriptor module, Options options, ValueType valueType, String state);

    <T> Function<String, T> deserializeFunction(ModuleDescriptor module, Options options, ValueType valueType);

    <T> T fromBytes(ModuleDescriptor module, Options options, ValueType valueType, byte[] bytes);

    <T> Function<byte[], T> fromBytesFunction(ModuleDescriptor module, Options options, ValueType valueType);

    <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, InputStream state);

    <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, Reader state);

    <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, String state);

    <T> Function<String, T> deserializeFunction(ModuleDescriptor module, Options options, Class<T> type);

    <T> T fromBytes(ModuleDescriptor module, Options options, Class<T> type, byte[] bytes);

    <T> Function<byte[], T> fromBytesFunction(ModuleDescriptor module, Options options, Class<T> type);
}
