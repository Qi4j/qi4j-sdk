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
package org.qi4j.entitystore.sql;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Environment;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.MariaDbSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

@Docker( image = "mariadb:10.1.21",
         ports = @Port( exposed = 8801, inner = 3306 ),
         environments = {
             @Environment( key = "MYSQL_ROOT_PASSWORD", value = "" ),
             @Environment( key = "MYSQL_ALLOW_EMPTY_PASSWORD", value = "yes" ),
             @Environment( key = "MYSQL_DATABASE", value = "jdbc_test_db" )
         },
         waitFor = @WaitFor( value = "mariadb.org binary distribution", timeoutInMillis = 120000 ),
         newForEachCase = false
)
@Disabled
public class MariaDbEntityStoreTest extends AbstractEntityStoreTest
{

    @BeforeAll
    static void waitForDockerToSettle()
        throws Exception
    {
        Thread.sleep( 15000 );
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "mariadb-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "mariadb-datasource-service" )
            .identifiedBy( "mariadb-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new MariaDbSQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly
        String host = "localhost";
        int port = 8801;
        DataSourceConfiguration defaults = config.forMixin( DataSourceConfiguration.class ).declareDefaults();
        defaults.url().set( "jdbc:mysql://" + host + ":" + port
                            + "/jdbc_test_db?profileSQL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
                            + "&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true&useSSL=false" );
        defaults.driver().set( "com.mysql.jdbc.Driver" );
        defaults.enabled().set( true );
        defaults.username().set( "root" );
        defaults.password().set( "" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables( moduleInstance, SQLDialect.MARIADB, super::tearDown );
    }
}
