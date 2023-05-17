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
package org.qi4j.serialization.messagepack;

import java.util.Base64;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.serialization.messagepack.assembly.MessagePackSerializationAssembler;
import org.qi4j.test.serialization.AbstractConvertersSerializationTest;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ValueFactory;

public class MessagePackConvertersSerializationTest extends AbstractConvertersSerializationTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new MessagePackSerializationAssembler().assemble( module );
        super.assemble( module );
    }

    @Override
    protected String getStringFromValueState( String state, String key ) throws Exception
    {
        return MessagePack.newDefaultUnpacker( Base64.getDecoder().decode( state ) )
                          .unpackValue().asMapValue()
                          .map().get( ValueFactory.newString( key ) )
                          .asStringValue().asString();
    }
}
