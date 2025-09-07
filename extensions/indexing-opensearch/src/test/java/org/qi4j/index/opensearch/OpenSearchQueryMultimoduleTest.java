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
package org.qi4j.index.opensearch;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.opensearch.assembly.OpenSearchClusterIndexQueryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.test.EntityTestAssembler;
import org.testcontainers.junit.jupiter.Container;

public class OpenSearchQueryMultimoduleTest extends OpenSearchQueryTest
{
    @Container
    public static OpenSearchContainer container = new OpenSearchContainer("opensearchproject/opensearch:latest")
        .withLogConsumer( out -> System.out.println( out.getUtf8StringWithoutLineEnding() ) )
        .withReuse(true);

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.layer );

        module = module.layer().module( "module2" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module );

        // Config module
        LayerAssembly configLayer = module.layer().application().layer( "config" );
        module.layer().uses( configLayer );
        ModuleAssembly config = configLayer.module( "config" );
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new OpenSearchClusterIndexQueryAssembler()
            .withConfig( config, Visibility.application )
            .visibleIn( Visibility.layer )
            .assemble( module );

        OpenSearchClusterConfiguration clusterConfig = config.forMixin( OpenSearchClusterConfiguration.class ).declareDefaults();
        clusterConfig.clusterName().set( "qi4j-test" );
        String host = container.getHost();
        Integer port = container.getFirstMappedPort();
        clusterConfig.nodes().set(host + ":" + port);

        OpenSearchIndexingConfiguration openSearchConfig = config.forMixin( OpenSearchIndexingConfiguration.class ).declareDefaults();
        openSearchConfig.index().set( (getClass().getSimpleName() + "." + testName.getMethodName()).toLowerCase());
        openSearchConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
    }
}
