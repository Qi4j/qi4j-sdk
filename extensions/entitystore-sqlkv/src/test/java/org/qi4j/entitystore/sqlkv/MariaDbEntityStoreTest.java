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
import org.qi4j.entitystore.sqlkv.assembly.MariaDbSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MariaDbEntityStoreTest extends AbstractEntityStoreTest
{
    @Container
    public static MariaDBContainer mariaDBContainer = (MariaDBContainer) new MariaDBContainer("mariadb:11")
        .withDatabaseName("jdbc_test_db")
        ;

    @Override
    // START SNIPPET: assembly
    public void assemble(ModuleAssembly module)
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble(module);
        ModuleAssembly config = module.layer().module("config");
        new EntityTestAssembler().defaultServicesVisibleIn(Visibility.layer).assemble(config);

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy("mariadb-datasource-service")
            .visibleIn(Visibility.module)
            .withConfig(config, Visibility.layer)
            .assemble(module);

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity("mariadb-datasource-service")
            .identifiedBy("mariadb-datasource")
            .visibleIn(Visibility.module)
            .withCircuitBreaker()
            .assemble(module);

        // SQL EntityStore
        new MariaDbSQLEntityStoreAssembler()
            .visibleIn(Visibility.application)
            .withConfig(config, Visibility.layer)
            .assemble(module);
        // END SNIPPET: assembly
        DataSourceConfiguration defaults = config.forMixin(DataSourceConfiguration.class).declareDefaults();
        String jdbcUrl = mariaDBContainer.getJdbcUrl();
        String driverClassName = mariaDBContainer.getDriverClassName();
        defaults.url().set(jdbcUrl
            + "?profileSQL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
            + "&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true&useSSL=false");
        defaults.driver().set(driverClassName);
        defaults.enabled().set(true);
        defaults.username().set(mariaDBContainer.getUsername());
        defaults.password().set(mariaDBContainer.getPassword());
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables(moduleInstance, SQLDialect.MARIADB, super::tearDown);
    }
}
