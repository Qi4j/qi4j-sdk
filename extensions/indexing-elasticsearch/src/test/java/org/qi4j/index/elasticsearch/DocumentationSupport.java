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
package org.qi4j.index.elasticsearch;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESClientIndexQueryAssembler;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.elasticsearch.client.Client;

public class DocumentationSupport
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        Visibility configVisibility = Visibility.module;

        // START SNIPPET: filesystem
        new ESFilesystemIndexQueryAssembler()
            .withConfig( configModule, configVisibility )
            .assemble( module );
        // END SNIPPET: filesystem

        // START SNIPPET: cluster
        new ESClusterIndexQueryAssembler()
            .withConfig( configModule, configVisibility )
            .assemble( module );
        // END SNIPPET: cluster

        Client client = null;
        // START SNIPPET: client
        new ESClientIndexQueryAssembler( client )
            .withConfig( configModule, configVisibility )
            .assemble( module );
        // END SNIPPET: client
    }
}
