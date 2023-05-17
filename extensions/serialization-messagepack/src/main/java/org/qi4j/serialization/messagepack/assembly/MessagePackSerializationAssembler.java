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
package org.qi4j.serialization.messagepack.assembly;

import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.serialization.messagepack.MessagePackAdapters;
import org.qi4j.serialization.messagepack.MessagePackSerialization;
import org.qi4j.serialization.messagepack.MessagePackSettings;
import org.qi4j.serialization.messagepack.MessagePackAdapters;
import org.qi4j.serialization.messagepack.MessagePackSerialization;

public class MessagePackSerializationAssembler extends Assemblers.VisibilityIdentity<MessagePackSerializationAssembler>
{
    private MessagePackSettings settings;

    public MessagePackSerializationAssembler withMessagePackSettings( MessagePackSettings settings )
    {
        this.settings = settings;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ServiceDeclaration declaration = module.services( MessagePackSerialization.class )
                                               .withTypes( Serialization.class,
                                                           Serializer.class, Deserializer.class,
                                                           Converters.class,
                                                           MessagePackAdapters.class )
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
