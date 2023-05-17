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
 */
package org.qi4j.test.performance.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sqlkv.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.junit.jupiter.api.Disabled;
import org.qi4j.entitystore.sqlkv.assembly.AbstractSQLEntityStoreAssembler;
import org.qi4j.entitystore.sqlkv.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;

import static org.qi4j.entitystore.sqlkv.assembly.PostgreSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY;

/**
 * Performance test for PostgreSQLEntityStore.
 * <p>
 * WARN This test is deactivated on purpose, please do not commit it activated.
 * </p>
 * <p>
 * To run it see PostgreSQLEntityStoreTest.
 * </p>
 */
@Disabled( "WARN Tearing down this test is broken!" )
public class PostgreSQLEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public PostgreSQLEntityStorePerformanceTest()
    {
        super( "PostgreSQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return module -> {
            ModuleAssembly config = module.layer().module( "config" );
            new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

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
                .withCircuitBreaker()
                .assemble( module );

            // SQL EntityStore
            new PostgreSQLEntityStoreAssembler()
                .withConfig( config, Visibility.layer )
                .assemble( module );
        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        try
        {
            super.cleanUp();
        }
        finally
        {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
                {
                    {
                        {
                            createAssembler()
                        }
                    }
                };
            Application application = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            application.activate();

            Module moduleInstance = application.findModule( "Layer 1", "config" );
            UnitOfWorkFactory uowf = moduleInstance.unitOfWorkFactory();
            UnitOfWork uow = uowf.newUnitOfWork(
                UsecaseBuilder.newUsecase( "Delete " + getClass().getSimpleName() + " test data" )
            );
            try
            {
                SQLConfiguration config = uow.get( SQLConfiguration.class, AbstractSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
                Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
                connection.setAutoCommit( false );
                String schemaName = config.schemaName().get();
                try( Statement stmt = connection.createStatement() )
                {
                    stmt.execute( String.format( "DROP SCHEMA \"%s\" CASCADE", schemaName ) );
                    connection.commit();
                }
            }
            finally
            {
                uow.discard();
            }
        }
    }
}
