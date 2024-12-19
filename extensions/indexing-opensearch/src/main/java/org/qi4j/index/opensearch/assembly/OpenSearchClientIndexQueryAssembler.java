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
package org.qi4j.index.opensearch.assembly;

import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.opensearch.OpenSearchIndexingConfiguration;
import org.qi4j.index.opensearch.client.OpenSearchClientIndexQueryService;
import org.qi4j.index.opensearch.internal.AbstractOpenSearchAssembler;
import org.opensearch.client.opensearch.OpenSearchClient;

public class OpenSearchClientIndexQueryAssembler
    extends AbstractOpenSearchAssembler<OpenSearchClientIndexQueryAssembler>
{
    private final OpenSearchClient client;

    public OpenSearchClientIndexQueryAssembler(final OpenSearchClient client )
    {
        this.client = client;
    }

    @Override
    public void assemble( final ModuleAssembly module )
    {
        super.assemble( module );
        module.services( OpenSearchClientIndexQueryService.class )
              .taggedWith( "elasticsearch", "query", "indexing" )
              .identifiedBy( identity() )
              .setMetaInfo( client )
              .visibleIn( visibility() )
              .instantiateOnStartup();

        if( hasConfig() )
        {
            configModule().entities( OpenSearchIndexingConfiguration.class )
                          .visibleIn( configVisibility() );
        }
    }
}
