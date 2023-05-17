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
package org.qi4j.entitystore.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TransactionProvider;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.ThreadLocalTransactionProvider;

/**
 * The JOOQ Context is extended (Qi4j-style) to get additional functionality and still backed by the underlying
 * JOOQ DSLContext implementation.
 */
@Mixins( JooqDslContext.Mixin.class )
public interface JooqDslContext extends DSLContext
{
    Name tableNameOf( String tableName );

    Table<Record> tableOf( String tableName );

    class Mixin
        implements InvocationHandler
    {
        private final DSLContext dsl;

        public Mixin( @Service DataSource dataSource, @Uses Settings settings, @Uses SQLDialect dialect )
        {
            ConnectionProvider connectionProvider = new DataSourceConnectionProvider( dataSource );
            TransactionProvider transactionProvider = new ThreadLocalTransactionProvider( connectionProvider, false );
            Configuration configuration = new DefaultConfiguration()
                .set( dialect )
                .set( connectionProvider )
                .set( transactionProvider )
                .set( settings );
            dsl = DSL.using( configuration );
        }

        @Override
        public Object invoke( Object o, Method method, Object[] args )
            throws Throwable
        {
            if( method.getName().equals( "tableOf" ) )
            {
                return DSL.table( tableNameOf( (String) args[ 0 ] ) );
            }
            if( method.getName().equals( "tableNameOf" ) )
            {
                return tableNameOf( (String) args[ 0 ] );
            }

            return method.invoke( dsl, args );       // delegate all
        }

        private Name tableNameOf( String name )
        {
            return DSL.name( name );
        }
    }
}
