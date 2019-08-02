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
package org.apache.polygene.entitystore.zookeeper;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.zookeeper.assembly.ZookeeperEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.TemporaryFolder;
import org.apache.polygene.test.cache.AbstractEntityStoreWithCacheTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;
import static org.apache.polygene.entitystore.zookeeper.ZookeeperEntityStoreTest.TEST_ZNODE_NAME;

@Docker( image = "zookeeper:3.4.11",
         ports = @Port( exposed = 32181, inner = 2181),
         newForEachCase = false
)
public class ZookeeperEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        super.assemble( module );
        sleep(1000);
        ModuleAssembly config = module.layer().module( "config" );

        ZookeeperEntityStoreAssembler zkAssembler = new ZookeeperEntityStoreAssembler();
        zkAssembler.withConfig( config, Visibility.layer ).assemble( module );

        ZookeeperEntityStoreConfiguration defaults = zkAssembler.configModule().forMixin( ZookeeperEntityStoreConfiguration.class ).declareDefaults();
        defaults.hosts().set( singletonList( "localhost:32181" ) );
        defaults.storageNode().set( TEST_ZNODE_NAME );

        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

    }

    @AfterEach
    void cleanUp()
        throws Exception
    {
        ZkUtil.cleanUp( "localhost:32181", TEST_ZNODE_NAME );
    }

}
