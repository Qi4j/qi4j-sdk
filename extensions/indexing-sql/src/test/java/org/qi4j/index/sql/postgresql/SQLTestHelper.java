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
package org.qi4j.index.sql.postgresql;

import java.sql.Connection;
import javax.sql.DataSource;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.qi4j.index.sql.support.common.RebuildingStrategy;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.opentest4j.TestAbortedException;

class SQLTestHelper
{
    static void assembleWithMemoryEntityStore( ModuleAssembly mainModule, String host, int port )
        throws AssemblyException
    {
        // EntityStore
        new EntityTestAssembler().visibleIn( Visibility.application ).assemble( mainModule );

        doCommonAssembling( mainModule, host, port );
    }

    private static void doCommonAssembling( ModuleAssembly mainModule, String host, int port )
        throws AssemblyException
    {
        ModuleAssembly config = mainModule.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
                                                identifiedBy( "postgres-datasource-service" ).
                                                visibleIn( Visibility.module ).
                                                withConfig( config, Visibility.layer ).
                                                assemble( mainModule );

        // DataSource
        new DataSourceAssembler().
                                     withDataSourceServiceIdentity( "postgres-datasource-service" ).
                                     identifiedBy( "postgres-datasource" ).
                                     visibleIn( Visibility.module ).
                                     withCircuitBreaker().
                                     assemble( mainModule );

        // SQL Index/Query
        new PostgreSQLIndexQueryAssembler().
                                               visibleIn( Visibility.module ).
                                               withConfig( config, Visibility.layer ).
                                               assemble( mainModule );
        // END SNIPPET: assembly

        config.forMixin( DataSourceConfiguration.class ).declareDefaults()
              .url().set( "jdbc:postgresql://" + host + ":" + port + "/jdbc_test_db" );

        // Always re-build schema in test scenarios because of possibly different app structure in
        // various tests
        mainModule.services( RebuildingStrategy.class ).
            withMixins( RebuildingStrategy.AlwaysNeed.class ).
                      visibleIn( Visibility.module );

        // Always re-index in test scenarios
        mainModule.services( ReindexingStrategy.class ).
            withMixins( ReindexingStrategy.AlwaysNeed.class ).
                      visibleIn( Visibility.module );
        config.entities( ReindexerConfiguration.class ).
            visibleIn( Visibility.layer );
    }

    static void setUpTest( ServiceFinder serviceFinder )
    {
        Connection connection = null;
        try
        {

            DataSource ds = serviceFinder.findService( DataSource.class ).get();
            connection = ds.getConnection();
            if( connection == null )
            {
                throw new TestAbortedException( "Unable to establish a DataSource" );
            }
        }
        catch( Throwable t )
        {

            t.printStackTrace();
            throw new TestAbortedException( "Unable to establish a DataSource", t );
        }
        finally
        {
            SQLUtil.closeQuietly( connection );
        }
    }

    private SQLTestHelper()
    {
    }

    static void sleep()
    {
        try
        {
            Thread.sleep(500);
        }
        catch( InterruptedException e )
        {
        }
    }
}
