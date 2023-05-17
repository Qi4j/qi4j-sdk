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
package org.qi4j.index.elasticsearch.client;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.index.elasticsearch.ElasticSearchIndexingConfiguration;
import org.qi4j.index.elasticsearch.internal.AbstractElasticSearchSupport;
import org.elasticsearch.client.Client;
import org.qi4j.index.elasticsearch.internal.AbstractElasticSearchSupport;

public class ESClientSupport extends AbstractElasticSearchSupport
{
    @This
    private Configuration<ElasticSearchIndexingConfiguration> configuration;

    @Uses
    private ServiceDescriptor descriptor;

    @Override
    protected void activateElasticSearch() throws Exception
    {
        configuration.refresh();
        ElasticSearchIndexingConfiguration config = configuration.get();

        index = config.index().get() == null ? DEFAULT_INDEX_NAME : config.index().get();
        indexNonAggregatedAssociations = config.indexNonAggregatedAssociations().get();

        client = descriptor.metaInfo( Client.class );
    }

    @Override
    protected void passivateClient()
    {
        client = null;
    }
}
