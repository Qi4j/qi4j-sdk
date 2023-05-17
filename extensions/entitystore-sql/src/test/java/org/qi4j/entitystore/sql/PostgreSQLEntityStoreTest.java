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
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;

@Docker( image = "org.qi4j:org.qi4j.internal.docker-postgres",
         ports = @Port( exposed = 8801, inner = 5432),
         waitFor = @WaitFor( value = "PostgreSQL init process complete; ready for start up.", timeoutInMillis = 30000),
         newForEachCase = false,
         environments = {
             @Environment( key = "POSTGRES_USER", value = "qi4j" ),
             @Environment( key = "POSTGRES_PASSWORD", value = "ThisIsGreat!" )
         }
)
public class PostgreSQLEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        delay();
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "postgresql-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "postgresql-datasource-service" )
            .identifiedBy( "postgresql-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new PostgreSQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly

//        String host = DOCKER.getDockerHost();
//        int port = DOCKER.getExposedContainerPort( "5432/tcp" );
        int port = 8801;
        String host = "localhost";
        DataSourceConfiguration defaults = config.forMixin( DataSourceConfiguration.class ).declareDefaults();
        defaults.url().set( "jdbc:postgresql://" + host + ":" + port + "/jdbc_test_db" );
        defaults.username().set( "qi4j" );
        defaults.password().set( "ThisIsGreat!" );

        // START SNIPPET: assembly
    }

    static void delay()
    {
        try
        {
            Thread.sleep( 5000L );
        }
        catch( InterruptedException e )
        {
            // ignore.
        }
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables( moduleInstance, SQLDialect.POSTGRES, super::tearDown );
    }
}
