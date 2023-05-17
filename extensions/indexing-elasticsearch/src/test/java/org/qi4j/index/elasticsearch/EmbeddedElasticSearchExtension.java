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
package org.qi4j.index.elasticsearch;

import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.elasticsearch.client.Client;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findFields;

/**
 * Embedded Elasticsearch JUnit Rule.
 * <p>
 * Starting from Elasticsearch 5, startup is way slower.
 * Reuse an embedded instance across tests.
 */
public class EmbeddedElasticSearchExtension
    implements BeforeAllCallback, AfterAllCallback
{
    private TemporaryFolder tmpDir;
    private Client client;
    private Application application;

    public Client client()
    {
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        return client;
    }

    public String indexName( String className, String methodName )
    {
        String indexName = className;
        if( methodName != null )
        {
            indexName += '-' + methodName;
        }
        return indexName.toLowerCase( Locale.US );
    }

    private SingletonAssembler activateEmbeddedElasticsearch( final String name )
    {
        try
        {
            return new SingletonAssembler(
                module -> {
                    module.layer().application().setName( name );
                    ModuleAssembly config = module.layer().module( "config" );
                    new EntityTestAssembler().assemble( config );
                    new EntityTestAssembler().assemble( module );
                    new FileConfigurationAssembler()
                        .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
                        .assemble( module );
                    new ESFilesystemIndexQueryAssembler()
                        .identifiedBy( name )
                        .withConfig( config, Visibility.layer )
                        .assemble( module );
                }
            );
        }
        catch( ActivationException | AssemblyException ex )
        {
            throw new RuntimeException( "Embedded Elasticsearch Rule - Failed to activate", ex );
        }
    }

    private Client findClient( Module module )
    {
        Client client = module.serviceFinder().findService( ElasticSearchSupport.class ).get().client();
        if( client == null )
        {
            throw new IllegalStateException( "Embedded Elasticsearch Rule - Failed to find client" );
        }
        return client;
    }

    @Override
    public void beforeAll( ExtensionContext context )
    {
        this.tmpDir = new TemporaryFolder();
        this.tmpDir.createDir();

        String name = indexName( context.getRequiredTestClass().getSimpleName(), null );
        SingletonAssembler assembler = activateEmbeddedElasticsearch( name );
        application = assembler.application();
        client = findClient( assembler.module() );
        inject( context );
    }

    private void inject( ExtensionContext context )
    {
        findFields( context.getRequiredTestClass(),
                    f -> f.getType().equals( EmbeddedElasticSearchExtension.class ), BOTTOM_UP )
            .forEach( f -> {
                try
                {
                    f.setAccessible( true );
                    if( Modifier.isStatic( f.getModifiers() ) )
                    {
                        // only allow static field injections (for now).
                        f.set( null, EmbeddedElasticSearchExtension.this );
                    }
                }
                catch( IllegalAccessException e )
                {
                    throw new UndeclaredThrowableException( e );
                }
            } );
    }

    @Override
    public void afterAll( ExtensionContext context )
        throws Exception
    {
        if( application != null )
        {
            application.passivate();
        }
        if( client != null )
        {
            client.close();
        }
        if( tmpDir != null )
        {
            tmpDir.afterEach( context );
        }
    }
}
