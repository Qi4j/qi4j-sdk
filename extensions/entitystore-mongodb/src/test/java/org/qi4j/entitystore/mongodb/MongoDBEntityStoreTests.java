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

import com.mongodb.client.MongoClient;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.mongodb.assembly.MongoDBEntityStoreAssembler;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test the MongoDBEntityStoreService.
 */
@Testcontainers
public class MongoDBEntityStoreTests extends EntityStoreTestSuite
{
    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5")
        .withReuse(true);

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
        mongoConfig.connectionString().set( mongoContainer.getConnectionString() );
    }

    private MongoClient mongo;
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
        mongo.getDatabase(dbName).drop();
        super.tearDown();
    }
}
