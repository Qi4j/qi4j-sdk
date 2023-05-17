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

package org.qi4j.library.rest.admin;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingService;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.index.rdf.RdfIndexingService;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

/**
 * JAVADOC
 */
public class DomainAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services(
            MemoryEntityStoreService.class,
            RdfIndexingService.class
        ).visibleIn( application );
        module.services( MemoryRepositoryService.class ).identifiedBy( "rdf-indexing" ).visibleIn( layer );

        module.entities( TestEntity.class, TestRole.class, TestEntity2.class ).visibleIn( application );
        module.values( TestValue.class ).visibleIn( application );
        module.services( DummyDataService.class ).instantiateOnStartup();
    }
}
