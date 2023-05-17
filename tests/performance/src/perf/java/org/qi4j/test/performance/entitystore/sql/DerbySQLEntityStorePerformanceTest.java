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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sqlkv.SQLEntityStoreConfiguration;
import org.qi4j.entitystore.sqlkv.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.qi4j.entitystore.sqlkv.SQLEntityStoreConfiguration;
import org.qi4j.entitystore.sqlkv.assembly.AbstractSQLEntityStoreAssembler;
import org.qi4j.entitystore.sqlkv.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;

import static org.qi4j.entitystore.sqlkv.assembly.DerbySQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY;

/**
 * Performance test for DerbySQLEntityStore.
 */
public class DerbySQLEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public DerbySQLEntityStorePerformanceTest()
    {
        super( "DerbySQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return module -> {
            ModuleAssembly config = module.layer().module( "config" );
            new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

            // DataSourceService
            new DBCPDataSourceServiceAssembler()
                .identifiedBy( "derby-datasource-service" )
                .visibleIn( Visibility.module )
                .withConfig( config, Visibility.layer )
                .assemble( module );

            // DataSource
            new DataSourceAssembler()
                .withDataSourceServiceIdentity( "derby-datasource-service" )
                .identifiedBy( "derby-datasource" )
                .withCircuitBreaker()
                .assemble( module );

            // SQL EntityStore
            new DerbySQLEntityStoreAssembler()
                .withConfig( config, Visibility.layer )
                .assemble( module );
        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        if( uowf == null )
        {
            return;
        }
        UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase(
            "Delete " + getClass().getSimpleName() + " test data" )
        );
        try
        {
            SQLEntityStoreConfiguration config = uow.get( SQLEntityStoreConfiguration.class,
                                                          AbstractSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
            connection.setAutoCommit( false );
            try( Statement stmt = connection.createStatement() )
            {
                stmt.execute( String.format( "DELETE FROM \"%s\"",
                                             config.entityTableName().get() ) );
                connection.commit();
            }
        }
        finally
        {
            uow.discard();
            super.cleanUp();
        }
    }
}
