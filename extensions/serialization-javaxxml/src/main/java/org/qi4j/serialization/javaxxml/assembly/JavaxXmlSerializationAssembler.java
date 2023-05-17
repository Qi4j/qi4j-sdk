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
package org.qi4j.serialization.javaxxml.assembly;

import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.serialization.javaxxml.JavaxXmlAdapters;
import org.qi4j.serialization.javaxxml.JavaxXmlFactories;
import org.qi4j.serialization.javaxxml.JavaxXmlSerialization;
import org.qi4j.serialization.javaxxml.JavaxXmlSettings;
import org.qi4j.spi.serialization.XmlDeserializer;
import org.qi4j.spi.serialization.XmlSerialization;
import org.qi4j.spi.serialization.XmlSerializer;
import org.qi4j.serialization.javaxxml.JavaxXmlSettings;

public class JavaxXmlSerializationAssembler extends Assemblers.VisibilityIdentity<JavaxXmlSerializationAssembler>
{
    private JavaxXmlSettings settings;

    public JavaxXmlSerializationAssembler withXmlSettings( JavaxXmlSettings settings )
    {
        this.settings = settings;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ServiceDeclaration declaration = module.services( JavaxXmlSerialization.class )
                                               .withTypes( Serialization.class,
                                                           Serializer.class, Deserializer.class,
                                                           Converters.class,
                                                           XmlSerialization.class,
                                                           XmlSerializer.class, XmlDeserializer.class,
                                                           JavaxXmlFactories.class, JavaxXmlAdapters.class )
                                               .taggedWith( Serialization.Format.XML )
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
