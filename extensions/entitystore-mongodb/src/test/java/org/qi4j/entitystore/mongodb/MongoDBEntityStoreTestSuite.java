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
package org.qi4j.entitystore.mongodb;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import com.mongodb.Mongo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.mongodb.assembly.MongoDBEntityStoreAssembler;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test the MongoDBEntityStoreService.
 */
@Docker( image = "mongo:3.5.10",
         ports = @Port( exposed = 8801, inner = 27017),
         waitFor = @WaitFor( value = "MongoDB starting", timeoutInMillis = 30000),
         newForEachCase = false
)
public class MongoDBEntityStoreTestSuite extends EntityStoreTestSuite
{
    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new MongoDBEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        MongoDBEntityStoreConfiguration mongoConfig = configModule.forMixin( MongoDBEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.writeConcern().set( MongoDBEntityStoreConfiguration.WriteConcern.MAJORITY );
        mongoConfig.database().set( "qi4j:test" );
        mongoConfig.collection().set( "qi4j:test:entities" );
        mongoConfig.hostname().set( "localhost" );
        mongoConfig.port().set( 8801 );
    }

    private Mongo mongo;
    private String dbName;

    @BeforeEach
    public void initializeMongo()
        throws Exception
    {
        Module storageModule = application.findModule( "Infrastructure Layer", "Storage Module" );
        MongoDBEntityStoreService es = storageModule.serviceFinder().findService( MongoDBEntityStoreService.class ).get();
        mongo = es.mongoInstanceUsed();
        dbName = es.dbInstanceUsed().getName();
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        mongo.dropDatabase( dbName );
        super.tearDown();
    }
}
