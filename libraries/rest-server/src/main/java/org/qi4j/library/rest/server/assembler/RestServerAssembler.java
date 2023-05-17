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

package org.qi4j.library.rest.server.assembler;

import freemarker.template.Configuration;
import freemarker.template.Version;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.importer.NewObjectImporter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ClassScanner;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rest.server.restlet.InteractionConstraintsService;
import org.qi4j.library.rest.server.restlet.RequestReaderDelegator;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.restlet.freemarker.ValueCompositeObjectWrapper;
import org.qi4j.library.rest.server.restlet.requestreader.DefaultRequestReader;
import org.qi4j.library.rest.server.restlet.responsewriter.AbstractResponseWriter;
import org.qi4j.library.rest.server.restlet.responsewriter.DefaultResponseWriter;
import org.qi4j.library.rest.server.spi.ResponseWriter;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.library.rest.server.restlet.responsewriter.AbstractResponseWriter;
import org.qi4j.library.rest.server.restlet.responsewriter.DefaultResponseWriter;
import org.restlet.service.MetadataService;

import static java.util.stream.Collectors.toList;
import static org.qi4j.api.util.Classes.hasModifier;
import static org.qi4j.api.util.Classes.isAssignableFrom;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.INSTANCE;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * JAVADOC
 */
public class RestServerAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        Properties props = new Properties();
        try
        {
            props.load( getClass().getResourceAsStream( "/velocity.properties" ) );

            VelocityEngine velocity = new VelocityEngine( props );

            module.importedServices( VelocityEngine.class )
                  .importedBy( INSTANCE ).setMetaInfo( velocity );
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Could not load velocity properties", e );
        }

        Version freemarkerVersion = Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
        Configuration cfg = new Configuration( freemarkerVersion );
        cfg.setClassForTemplateLoading( AbstractResponseWriter.class, "" );
        cfg.setObjectWrapper( new ValueCompositeObjectWrapper( freemarkerVersion ) );

        module.importedServices( Configuration.class ).setMetaInfo( cfg );

        module.importedServices( MetadataService.class );

        module.importedServices( ResponseWriterDelegator.class )
              .identifiedBy( "responsewriterdelegator" )
              .importedBy( NEW_OBJECT )
              .visibleIn( Visibility.layer );
        module.objects( ResponseWriterDelegator.class );

        module.importedServices( RequestReaderDelegator.class )
              .identifiedBy( "requestreaderdelegator" )
              .importedBy( NEW_OBJECT )
              .visibleIn( Visibility.layer );
        module.objects( RequestReaderDelegator.class );

        module.importedServices( InteractionConstraintsService.class )
              .importedBy( NewObjectImporter.class )
              .visibleIn( Visibility.application );
        module.objects( InteractionConstraintsService.class );

        // Standard response writers
        Predicate<Class<?>> isResponseWriterClass = isAssignableFrom( ResponseWriter.class );
        Predicate<Class<?>> isNotAnAbstract = hasModifier( Modifier.ABSTRACT ).negate();
        List<? extends Class<?>> responseWriters = ClassScanner.findClasses( DefaultResponseWriter.class )
                                                               .filter( isNotAnAbstract.and( isResponseWriterClass ) )
                                                               .collect( toList() );
        for( Class<?> responseWriter : responseWriters )
        {
            module.objects( responseWriter );
        }

        // Standard request readers
        module.objects( DefaultRequestReader.class );
    }
}
