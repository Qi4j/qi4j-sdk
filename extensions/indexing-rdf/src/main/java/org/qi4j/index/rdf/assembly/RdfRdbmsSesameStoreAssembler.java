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
package org.qi4j.index.rdf.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.RdfIndexingService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.RdbmsRepositoryConfiguration;
import org.qi4j.library.rdf.repository.RdbmsRepositoryService;

public class RdfRdbmsSesameStoreAssembler  extends AbstractRdfIndexingAssembler<RdfNativeSesameStoreAssembler>
{
    private Visibility repositoryVisibility;

    public RdfRdbmsSesameStoreAssembler()
    {
        this( Visibility.application, Visibility.module );
    }

    @SuppressWarnings( "WeakerAccess" )
    public RdfRdbmsSesameStoreAssembler( Visibility indexingVisibility, Visibility repositoryVisibility )
    {
        visibleIn( indexingVisibility );
        this.repositoryVisibility = repositoryVisibility;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        module.services( RdbmsRepositoryService.class )
              .visibleIn( repositoryVisibility )
              .instantiateOnStartup();
        module.services( RdfIndexingService.class )
              .taggedWith( "rdf", "query", "indexing" )
              .visibleIn( visibility() )
              .instantiateOnStartup();
        module.services( RdfQueryParserFactory.class ).visibleIn( visibility() );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );

        if( hasConfig() )
        {
            configModule().entities( RdbmsRepositoryConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }
}
