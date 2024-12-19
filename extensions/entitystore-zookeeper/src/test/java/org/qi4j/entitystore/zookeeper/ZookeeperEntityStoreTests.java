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
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.UndeclaredThrowableException;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;

@Testcontainers
public class ZookeeperEntityStoreTests extends EntityStoreTestSuite
{
    @Container
    public static GenericContainer<?> zookeeper = new GenericContainer<>("zookeeper:latest")
        .withExposedPorts(2181)
        .withReuse(true);

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        try
        {
            sleep(1000);
        }
        catch( InterruptedException e )
        {
            throw new UndeclaredThrowableException( e );
        }
        module.defaultServices();
        new ZookeeperEntityStoreAssembler()
            .withConfig( configModule, Visibility.application )
            .visibleIn( Visibility.application )
            .assemble( module );
    }

    @Override
    protected void defineConfigModule( ModuleAssembly module )
    {
        super.defineConfigModule( module );
        ZookeeperEntityStoreConfiguration defaults = module.forMixin( ZookeeperEntityStoreConfiguration.class ).declareDefaults();
        defaults.hosts().set( singletonList( zookeeper.getHost() + ":" + zookeeper.getFirstMappedPort() ) );
        defaults.storageNode().set( ZookeeperEntityStoreTest.TEST_ZNODE_NAME );
    }

    @AfterEach
    void cleanUp()
        throws Exception
    {
        ZkUtil.cleanUp( zookeeper.getHost() + ":" + zookeeper.getFirstMappedPort(), ZookeeperEntityStoreTest.TEST_ZNODE_NAME );
    }
}
