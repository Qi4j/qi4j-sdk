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
package org.qi4j.entitystore.sqlkv;

import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sqlkv.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgreSQLEntityStoreTests extends EntityStoreTestSuite
{
    @Container
    public static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres:17-alpine")
        .withDatabaseName("jdbc_test_db");

    @Override
    protected void defineStorageModule(ModuleAssembly module)
    {
        PostgreSQLEntityStoreTest.delay();
        module.defaultServices();
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy("postgresql-datasource-service")
            .visibleIn(Visibility.module)
            .withConfig(configModule, Visibility.application)
            .assemble(module);

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity("postgresql-datasource-service")
            .identifiedBy("postgresql-datasource")
            .visibleIn(Visibility.module)
            .withCircuitBreaker()
            .assemble(module);

        // SQL EntityStore
        new PostgreSQLEntityStoreAssembler()
            .visibleIn(Visibility.application)
            .withConfig(configModule, Visibility.application)
            .assemble(module);

        DataSourceConfiguration defaults = configModule.forMixin(DataSourceConfiguration.class).declareDefaults();
        defaults.url().set(postgres.getJdbcUrl());
        defaults.driver().set(postgres.getDriverClassName());
        defaults.username().set(postgres.getUsername());
        defaults.password().set(postgres.getPassword());
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables(application.findModule(INFRASTRUCTURE_LAYER, STORAGE_MODULE), SQLDialect.POSTGRES, super::tearDown);
    }
}
