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
package org.apache.polygene.entitystore.redis;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.redis.assembly.RedisEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.cache.AbstractEntityStoreWithCacheTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Docker( image = "redis:4.0.0-alpine",
         ports = @Port( exposed = 8801, inner = 6379),
         newForEachCase = false
)
public class RedisEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        new RedisEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        RedisEntityStoreConfiguration redisConfig = config.forMixin( RedisEntityStoreConfiguration.class )
                                                          .declareDefaults();
        redisConfig.host().set( "localhost" );
        redisConfig.port().set( 8801 );
    }

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
