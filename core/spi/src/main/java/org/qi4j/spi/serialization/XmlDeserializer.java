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
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization.Options;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.module.ModuleSpi;
import org.w3c.dom.Node;

import java.util.function.Function;

/**
 * {@literal javax.xml} deserializer.
 */
public interface XmlDeserializer extends Deserializer
{
    <T> T fromXml(ModuleDescriptor module, Options options, ValueType valueType, @Optional Node state);

    default <T> Function<Node, T> fromXmlFunction(ModuleDescriptor module, Options options, ValueType valueType)
    {
        return state -> fromXml(module, options, valueType, state);
    }

    default <T> T fromXml(ModuleDescriptor module, Options options, Class<T> type, @Optional Node state)
    {
        // TODO Remove (ModuleSpi) cast
        ValueType valueType = ((ModuleSpi) module.instance()).valueTypeFactory().valueTypeOf(module, type);
        return fromXml(module, options, valueType, state);
    }

    default <T> Function<Node, T> fromXml(ModuleDescriptor module, Options options, Class<T> type)
    {
        return state -> fromXml(module, options, type, state);
    }

}
