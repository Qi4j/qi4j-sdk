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
package org.qi4j.index.solr;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.solr.assembly.SolrIndexingAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.test.TemporaryFolder;
import org.qi4j.test.indexing.AbstractEntityFinderTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

@Disabled( "SOLR Index/Query is not working at all" )
@ExtendWith( TemporaryFolder.class )
public class SolrEntityFinderTest
    extends AbstractEntityFinderTest
{
    private TemporaryFolder tmpDir;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
        new SolrIndexingAssembler().assemble( module );
    }
}
