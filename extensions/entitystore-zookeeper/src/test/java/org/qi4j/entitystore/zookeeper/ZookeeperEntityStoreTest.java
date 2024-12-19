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
package org.qi4j.entitystore.zookeeper;

import org.junit.jupiter.api.AfterEach;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.zookeeper.assembly.ZookeeperEntityStoreAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;


@Testcontainers
public class ZookeeperEntityStoreTest extends AbstractEntityStoreTest
{
    @Container
    public static GenericContainer<?> zookeeper = new GenericContainer<>("zookeeper:latest")
        .withExposedPorts(2181)
        .waitingFor(Wait.forLogMessage(".*binding to port /0\\.0\\.0\\.0:2181.*", 1))
        .withLogConsumer( out -> System.out.println( out.getUtf8StringWithoutLineEnding() ) )
        .withStartupTimeout(Duration.ofSeconds(90))
        .withReuse(true);

    static final String TEST_ZNODE_NAME = "/qi4j/entitystore-test";

    @Override
    // START SNIPPET: assembly
    public void assemble(ModuleAssembly module)
        throws Exception
    {
        // END SNIPPET: assembly
        sleep(1000);
        super.assemble(module);
        ModuleAssembly config = module.layer().module("config");
        new EntityTestAssembler().defaultServicesVisibleIn(Visibility.layer).assemble(config);
        // START SNIPPET: assembly
        ZookeeperEntityStoreAssembler zkAssembler = new ZookeeperEntityStoreAssembler();
        zkAssembler.withConfig(config, Visibility.layer).assemble(module);
        // END SNIPPET: assembly
        ZookeeperEntityStoreConfiguration defaults = zkAssembler.configModule().forMixin(ZookeeperEntityStoreConfiguration.class).declareDefaults();
        defaults.hosts().set(singletonList(zookeeper.getHost() + ":" + zookeeper.getFirstMappedPort()));
        defaults.storageNode().set(TEST_ZNODE_NAME);
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    void cleanUp()
        throws Exception
    {
        ZkUtil.cleanUp(zookeeper.getHost() + ":" + zookeeper.getFirstMappedPort(), TEST_ZNODE_NAME);
    }
}
