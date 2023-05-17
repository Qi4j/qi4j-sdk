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

import java.io.InputStream;
import java.io.InputStreamReader;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueType;

import static java.nio.charset.StandardCharsets.UTF_8;

// START SNIPPET: text
/**
 * Base Text Deserializer.
 *
 * Implementations work on Strings, this base deserializer decode bytes in UTF-8 to produce strings.
 *
 * See {@link AbstractTextSerializer}.
 */
public abstract class AbstractTextDeserializer extends AbstractDeserializer
// END SNIPPET: text
{
    @Override
    public <T> T deserialize( ModuleDescriptor module, ValueType valueType, InputStream state )
    {
        return deserialize( module, valueType, new InputStreamReader( state, UTF_8 ) );
    }
}
