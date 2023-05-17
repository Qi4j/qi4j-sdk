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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.jooq.SQLDialect;

class TearDown
{
    static void dropTables( Module module, SQLDialect dialect, Runnable after )
    {
        if( module == null ){
            System.err.println( "WARNING: 'module' was null. Happens if there was a pre-activation error. Otherwise an InternalError" );
        }
        UnitOfWorkFactory unitOfWorkFactory = module.unitOfWorkFactory();
        ServiceFinder serviceFinder = module.serviceFinder();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Cleaning up. Drop Tables" ) ) )
        {
            try( Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection() )
            {
                connection.setAutoCommit( true );
                try( Statement stmt = connection.createStatement() )
                {
                    dropTable( dialect, stmt, "TYPES" );
                    dropTable( dialect, stmt, "ENTITIES" );
                    dropTable( dialect, stmt, "TestEntity" );
                    dropTable( dialect, stmt, "TestEntity_ASSOCS" );
                    dropIndex( dialect, stmt, "TestEntity_ASSOCS" );
                    dropTable( dialect, stmt, "PhoneNumber" );
                    dropTable( dialect, stmt, "PhoneNumber_ASSOCS" );
                    dropIndex( dialect, stmt, "PhoneNumber_ASSOCS" );
                    dropTable( dialect, stmt, "Person" );
                    dropTable( dialect, stmt, "Person_ASSOCS" );
                    dropIndex( dialect, stmt, "Person_ASSOCS" );
                    dropTable( dialect, stmt, "City" );
                    dropTable( dialect, stmt, "City_ASSOCS" );
                    dropIndex( dialect, stmt, "City_ASSOCS" );
                    dropTable( dialect, stmt, "Address" );
                    dropTable( dialect, stmt, "Address_ASSOCS" );
                    dropIndex( dialect, stmt, "Address_ASSOCS" );
                    dropTable( dialect, stmt, "Country" );
                    dropTable( dialect, stmt, "Country_ASSOCS" );
                    dropIndex( dialect, stmt, "Country_ASSOCS" );
                }
            }
        }
        catch( SQLException e )
        {
            throw new RuntimeException( "Unable to clean up tables.", e );
        }
        finally
        {
            after.run();
        }
    }

    private static void dropTable( SQLDialect dialect, Statement stmt, String tableName )
    {
        try
        {
            if( dialect == SQLDialect.MYSQL || dialect == SQLDialect.MARIADB )
            {
                stmt.execute( String.format( "DROP TABLE `%s`", tableName ) );
            }
            else
            {
                stmt.execute( String.format( "DROP TABLE \"%s\"", tableName ) );
            }
        }
        catch( SQLException e )
        {
            //  ignore. Not all tables will be present in all tests.
        }
    }

    private static void dropIndex( SQLDialect dialect, Statement stmt, String tableName )
    {
        try
        {
            if( dialect == SQLDialect.MYSQL || dialect == SQLDialect.MARIADB )
            {
                stmt.execute( String.format( "DROP INDEX `IDX_%s`", tableName ) );
            }
            else
            {
                stmt.execute( String.format( "DROP INDEX \"IDX_%s\"", tableName ) );
            }
        }
        catch( SQLException e )
        {
            //  ignore. Not all tables will be present in all tests.
        }
    }
}
