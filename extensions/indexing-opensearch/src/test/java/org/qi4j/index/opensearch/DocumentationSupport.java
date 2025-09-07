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

import org.opensearch.client.opensearch.OpenSearchClient;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.opensearch.assembly.OpenSearchClientIndexQueryAssembler;
import org.qi4j.index.opensearch.assembly.OpenSearchClusterIndexQueryAssembler;

public class DocumentationSupport
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly configModule)
        throws AssemblyException
    {
        Visibility configVisibility = Visibility.module;

        // START SNIPPET: filesystem
        new OpenSearchClusterIndexQueryAssembler()
            .withConfig( configModule, configVisibility )
            .assemble(configModule);
        // END SNIPPET: filesystem

        // START SNIPPET: cluster
        new OpenSearchClusterIndexQueryAssembler()
            .withConfig( configModule, configVisibility )
            .assemble(configModule);
        // END SNIPPET: cluster

        OpenSearchClient client = null;
        // START SNIPPET: client
        new OpenSearchClientIndexQueryAssembler( client )
            .withConfig( configModule, configVisibility )
            .assemble(configModule);
        // END SNIPPET: client
    }
}
