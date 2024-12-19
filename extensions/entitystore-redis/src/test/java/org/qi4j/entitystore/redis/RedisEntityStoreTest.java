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
package org.qi4j.entitystore.redis;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.redis.assembly.RedisEntityStoreAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

//@Docker( image = "redis:7.4-alpine",
//         ports = @Port( exposed = 8801, inner = 6379),
//         newForEachCase = false
//)
@Testcontainers
public class RedisEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>( "redis:7.4-alpine" )
        .withExposedPorts( 6379 )
        .withReuse(true)
        ;

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        // START SNIPPET: assembly
        new RedisEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        // END SNIPPET: assembly
        RedisEntityStoreConfiguration redisConfig = config.forMixin( RedisEntityStoreConfiguration.class )
                                                          .declareDefaults();
        redisConfig.host().set( redisContainer.getHost() );
        redisConfig.port().set( redisContainer.getFirstMappedPort() );
        super.assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    private JedisPool jedisPool;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        RedisEntityStoreService es = serviceFinder.findService( RedisEntityStoreService.class ).get();
        jedisPool = es.jedisPool();
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        try( Jedis jedis = jedisPool.getResource() )
        {
            jedis.flushDB();
        }
        super.tearDown();
    }
}
