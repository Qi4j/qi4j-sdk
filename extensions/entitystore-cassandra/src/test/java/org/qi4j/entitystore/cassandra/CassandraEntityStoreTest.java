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
package org.qi4j.entitystore.cassandra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.cassandra.assembly.CassandraEntityStoreAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.test.entity.CanRemoveAll;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test the CassandraEntityStoreService.
 */
@Testcontainers
public class CassandraEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Container
    public static CassandraContainer cassandra = new CassandraContainer("cassandra:5")
        .withReuse(true);

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        module.services( CassandraEntityStoreService.class ).withTypes( CanRemoveAll.class ).withMixins( EmptyCassandraTableMixin.class );

        // START SNIPPET: assembly
        new CassandraEntityStoreAssembler()
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly

        CassandraEntityStoreConfiguration cassandraDefaults = config.forMixin( CassandraEntityStoreConfiguration.class ).declareDefaults();
        String host = cassandra.getHost();
        int port = cassandra.getFirstMappedPort();
        // TODO: Logging system output here.
//        System.out.println("Cassandra: " + host + ":" + port);
        cassandraDefaults.hostnames().set( host + ':' + port );
        cassandraDefaults.createIfMissing().set( true );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        CanRemoveAll cleaner = serviceFinder.findService( CanRemoveAll.class ).get();
        cleaner.removeAll();
        super.tearDown();
    }
}
