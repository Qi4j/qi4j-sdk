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
package org.qi4j.index.elasticsearch.filesystem;

import java.io.File;
import java.util.stream.Stream;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.index.elasticsearch.ElasticSearchIndexingConfiguration;
import org.qi4j.index.elasticsearch.internal.AbstractElasticSearchSupport;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

public class ESFilesystemSupport
    extends AbstractElasticSearchSupport
{
    @This
    private Configuration<ElasticSearchIndexingConfiguration> configuration;

    @This
    private HasIdentity hasIdentity;

    @Service
    private FileConfiguration fileConfig;

    private Node node;

    @Override
    protected void activateElasticSearch()
        throws Exception
    {
        configuration.refresh();
        ElasticSearchIndexingConfiguration config = configuration.get();

        String clusterName = config.clusterName().get() == null ? DEFAULT_CLUSTER_NAME : config.clusterName().get();
        index = config.index().get() == null ? DEFAULT_INDEX_NAME : config.index().get();
        indexNonAggregatedAssociations = config.indexNonAggregatedAssociations().get();

        Identity identity = hasIdentity.identity().get();
        File homeDir = new File( new File( fileConfig.temporaryDirectory(), identity.toString() ), "home" );
        File logsDir = new File( fileConfig.logDirectory(), identity.toString() );
        File dataDir = new File( fileConfig.dataDirectory(), identity.toString() );
        File confDir = new File( fileConfig.configurationDirectory(), identity.toString() );
        Stream.of( homeDir, logsDir, dataDir, confDir ).forEach( File::mkdirs );
        Settings settings = Settings.builder()
                                    .put( "cluster.name", clusterName )
                                    .put( "path.home", homeDir.getAbsolutePath() )
                                    .put( "path.logs", logsDir.getAbsolutePath() )
                                    .put( "path.data", dataDir.getAbsolutePath() )
                                    .put( "path.conf", confDir.getAbsolutePath() )
                                    .put( "transport.type", "local" )
                                    .put( "http.enabled", false )
                                    .build();
        node = new Node( settings );
        node.start();
        client = node.client();
    }

    @Override
    public void passivateElasticSearch()
        throws Exception
    {
        node.close();
        node = null;
    }
}
