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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Base64;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueType;

import static java.util.stream.Collectors.joining;

// START SNIPPET: binary
/**
 * Base Binary Deserializer.
 *
 * Implementations work on bytes, this base deserializer decode Strings from Base64 to produce bytes.
 *
 * See {@link AbstractBinarySerializer}.
 */
public abstract class AbstractBinaryDeserializer extends AbstractDeserializer
// END SNIPPET: binary
{
    @Override
    public <T> T deserialize( ModuleDescriptor module, ValueType valueType, Reader state )
    {
        String stateString;
        try( BufferedReader buffer = new BufferedReader( state ) )
        {
            stateString = buffer.lines().collect( joining( "\n" ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        byte[] decoded = Base64.getDecoder().decode( stateString );
        return deserialize( module, valueType, new ByteArrayInputStream( decoded ) );
    }
}
