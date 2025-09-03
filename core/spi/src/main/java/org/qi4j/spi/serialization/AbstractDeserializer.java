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

import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.StatefulAssociationCompositeDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization.Options;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.module.ModuleSpi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

/**
 * Base Deserializer.
 * <p>
 * Provides default implementations for convenience API methods.
 * <p>
 * See {@link AbstractSerializer}.
 */
public abstract class AbstractDeserializer
    implements Deserializer
{
    protected static final ValueType ENTITY_REF_LIST_VALUE_TYPE = CollectionType.listOf(EntityReference.class);
    protected static final ValueType ENTITY_REF_MAP_VALUE_TYPE = MapType.of(String.class, EntityReference.class);

    @Override
    public <T> T deserialize(ModuleDescriptor module, Options options, ValueType valueType, String state)
    {
        return deserialize(module, options, valueType, new StringReader(state));
    }

    @Override
    public <T> Function<String, T> deserializeFunction(ModuleDescriptor module, Options options, ValueType valueType)
    {
        return state -> deserialize(module, options, valueType, new StringReader(state));
    }

    @Override
    public <T> T fromBytes(ModuleDescriptor module, Options options, ValueType valueType, byte[] bytes)
    {
        return deserialize(module, options, valueType, new ByteArrayInputStream(bytes));
    }

    @Override
    public <T> Function<byte[], T> fromBytesFunction(ModuleDescriptor module, Options options, ValueType valueType)
    {
        return bytes -> deserialize(module, options, valueType, new ByteArrayInputStream(bytes));
    }

    @Override
    public <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, InputStream state)
    {
        return deserialize(module, options, valueTypeOf(module, type), state);
    }

    @Override
    public <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, Reader state)
    {
        return deserialize(module, options, valueTypeOf(module, type), state);
    }

    @Override
    public <T> T deserialize(ModuleDescriptor module, Options options, Class<T> type, String state)
    {
        return deserialize(module, options, valueTypeOf(module, type), state);
    }

    @Override
    public <T> Function<String, T> deserializeFunction(ModuleDescriptor module, Options options, Class<T> type)
    {
        return deserializeFunction(module, options, valueTypeOf(module, type));
    }

    @Override
    public <T> T fromBytes(ModuleDescriptor module, Options options, Class<T> type, byte[] bytes)
    {
        return fromBytes(module, options, valueTypeOf(module, type), bytes);
    }

    @Override
    public <T> Function<byte[], T> fromBytesFunction(ModuleDescriptor module, Options options, Class<T> type)
    {
        return fromBytesFunction(module, options, valueTypeOf(module, type));
    }

    private ValueType valueTypeOf(ModuleDescriptor module, Class<?> type)
    {
        // TODO Remove (ModuleSpi) cast
        return ((ModuleSpi) module.instance()).valueTypeFactory().valueTypeOf(module, type);
    }

    protected final StatefulAssociationCompositeDescriptor statefulCompositeDescriptorFor(ModuleDescriptor module, String typeName)
    {
        StatefulAssociationCompositeDescriptor descriptor = null;
        try
        {
            descriptor = module.valueDescriptor(typeName);
        }
        catch(AmbiguousTypeException ex1)
        {
            try
            {
                descriptor = module.entityDescriptor(typeName);
            }
            catch(AmbiguousTypeException ex2)
            {
            }
        }
        return descriptor;
    }
}
