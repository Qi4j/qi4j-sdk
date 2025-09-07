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
package org.qi4j.index.opensearch.internal;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.mapping.DynamicMapping;
import org.opensearch.client.opensearch._types.mapping.DynamicTemplate;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.cluster.HealthRequest;
import org.opensearch.client.opensearch.cluster.HealthResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.RefreshRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import org.opensearch.client.util.ObjectBuilder;
import org.qi4j.index.opensearch.OpenSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractOpenSearchSupport
    implements OpenSearchSupport
{
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    protected static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchSupport.class);

    protected static final String DEFAULT_CLUSTER_NAME = "qi4j_cluster";
    protected static final String DEFAULT_INDEX_NAME = "qi4j_index";
    protected static final String ENTITIES_TYPE = "qi4j_entities"; // kept for compatibility with callers

    protected OpenSearchClient client;
    protected String index;
    protected boolean indexNonAggregatedAssociations;

    @Override
    public final void activateService() throws Exception {
        activateOpenSearch();

        // Wait for yellow status: primary shards allocated
        HealthRequest healthReq = new HealthRequest.Builder()
            .waitForStatus(HealthStatus.Yellow)
            .build();
        HealthResponse health = client.cluster().health(healthReq);
        LOGGER.debug("Cluster health: {}", health.status());

        // Ensure index exists
        BooleanResponse exists = client.indices().exists(new ExistsRequest.Builder().index(index).build());
        if (!exists.value()) {
            LOGGER.info("Will create index '{}' as it does not exist.", index);

            // Create index with basic settings and a dynamic template to treat strings as keyword
            // Equivalent JSON:
            // {
            //   "settings": { "index": { "refresh_interval": -1 } },
            //   "mappings": {
            //     "dynamic": false,
            //     "dynamic_templates": [
            //       { "strings_as_keyword": {
            //           "match_mapping_type": "string",
            //           "mapping": { "type": "keyword" }
            //       } }
            //     ]
            //   }
            // }
            CreateIndexResponse createIndexResponse = client.indices()
                .create(CreateIndexRequest.builder()
                    .index(index)
                    .settings(st -> st
                        .index(i -> i.refreshInterval(Time.of(b -> b.time("-1")))) // disable refresh during bulk
                    )
                    .build());

            List<Map<String, DynamicTemplate>> templates = List.of(
                Map.of(
                    "strings_as_keyword",
                    new DynamicTemplate.Builder()
                        .matchMappingType("string")
                        .mapping(new Property.Builder().keyword(k -> k).build())
                        .build()
                )
            );

            boolean mappingsAck = client.indices().putMapping(m -> m
                .index(index)
                .dynamic(DynamicMapping.True )
                .dynamicTemplates(templates)
            ).acknowledged();

            LOGGER.info("Index '{}': acknowledged={}", index, mappingsAck);
        }

        // Refresh index
        client.indices().refresh(new RefreshRequest.Builder().index(index).build());

        // Wait for yellow again (optional but keeps parity with legacy)
        client.cluster().health(new HealthRequest.Builder().waitForStatus(HealthStatus.Yellow).build());

        LOGGER.info("Index/Query connected to OpenSearch");
    }

    protected abstract void activateOpenSearch() throws Exception;

    @Override
    public final void passivateService() throws Exception {
        passivateClient();
        index = null;
        indexNonAggregatedAssociations = false;
        passivateOpenSearch();
    }

    protected void passivateClient() {
        try {
            if (client != null) {
                OpenSearchTransport transport = client._transport(); // underlying transport
                if (transport != null) {
                    try {
                        ((AutoCloseable) transport).close();
                    } catch (Exception e) {
                        LOGGER.warn("Error closing OpenSearch transport", e);
                    }
                }
            }
        } finally {
            client = null;
        }
    }

    protected void passivateOpenSearch() throws Exception {
        // NOOP
    }

    @Override
    public final OpenSearchClient client() {
        return client;
    }

    @Override
    public final String index() {
        return index;
    }

    @Override
    public final String entitiesType() {
        return ENTITIES_TYPE;
    }

    @Override
    public final boolean indexNonAggregatedAssociations() {
        return indexNonAggregatedAssociations;
    }
}
