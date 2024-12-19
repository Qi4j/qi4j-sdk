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
package org.qi4j.entitystore.riak;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;

@Testcontainers
public class RiakEntityStoreTests extends EntityStoreTestSuite
{
    @Container
    public static GenericContainer<?> riakContainer = new GenericContainer<>( "theblitzapp/riak_kv_2.9:debian" )
        .withExposedPorts( 8087 )
        .withReuse(true)
        .waitingFor(Wait.forLogMessage(".*Starting TCP Monitor.*", 1))
        .withLogConsumer( out -> System.out.println( out.getUtf8StringWithoutLineEnding() ) )
        .withStartupTimeout(Duration.ofSeconds(90))
        ;


    private RiakFixture riakFixture;

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new RiakEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        RiakEntityStoreConfiguration riakConfig = configModule.forMixin( RiakEntityStoreConfiguration.class )
                                                              .declareDefaults();
        String host = riakContainer.getHost();
        int port = riakContainer.getFirstMappedPort();
        riakConfig.hosts().set( Collections.singletonList( host + ':' + port ) );
    }

    @BeforeEach
    public void initializeRiak()
        throws Exception
    {
        Module storageModule = application.findModule( "Infrastructure Layer", "Storage Module" );
        RiakEntityStoreService es = storageModule.findService( RiakEntityStoreService.class ).get();
        riakFixture = new RiakFixture( es.riakClient(), es.riakNamespace() );
        riakFixture.waitUntilReady();
    }

    @AfterEach
    public void cleanUpRiak()
    {
        riakFixture.deleteTestData();
        super.tearDown();
    }
}
