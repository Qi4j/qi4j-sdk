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
 *
 *
 */
package org.qi4j.library.rdf;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.model.Model2XML;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * TODO
 */
public class Model2XMLTest
{
    @Test
    public void testModel2XML() throws AssemblyException, TransformerException
    {
        Energy4Java qi4j = new Energy4Java(  );
        ApplicationDescriptor model = qi4j.newApplicationModel( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();

            assembly.setName( "Test application" );

            LayerAssembly webLayer = assembly.layer( "Web" );
            LayerAssembly domainLayer = assembly.layer( "Domain" );
            LayerAssembly infrastructureLayer = assembly.layer( "Infrastructure" );

            webLayer.uses( domainLayer, infrastructureLayer );
            domainLayer.uses( infrastructureLayer );

            ModuleAssembly rest = webLayer.module( "REST" );
            rest.transients( TestTransient.class ).visibleIn( Visibility.layer );

            domainLayer.module( "Domain" );
            infrastructureLayer.module( "Database" );

            return assembly;
        } );

        Document document = new Model2XML().apply( model );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( "indent", "yes"  );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2"  );
        transformer.transform( new DOMSource( document ), new StreamResult( System.out ) );
    }

    interface TestTransient
        extends TransientComposite
    {}
}
