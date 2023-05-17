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

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.util.Classes;
import org.jooq.Constraint;
import org.jooq.CreateTableColumnStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 * This class is effectively the manager of the {@link MixinTable} instances.
 */
class TypesTable
{
    // Common in all tables
    private static final String IDENTITY_COLUMN_NAME = "_identity";
    private static final String CREATED_COLUMN_NAME = "_created_at";
    private static final String LASTMODIFIED_COLUMN_NAME = "_modified_at";

    // Types Table
    private static final String TABLENAME_COLUMN_NAME = "_table_name";

    // Common Fields
    private Field<String> identityColumn;
    private Field<Timestamp> createdColumn;
    private Field<Timestamp> modifiedColumn;

    // Types Table
    private Field<String> tableNameColumn;

    private final Map<Class<?>, Table<Record>> mixinTablesCache = new ConcurrentHashMap<>();

    private final Table<Record> typesTable;
    private final SQLDialect dialect;
    private final SqlEntityStoreConfiguration config;

    private final JooqDslContext dsl;

    TypesTable( JooqDslContext dsl,
                SQLDialect dialect,
                String typesTablesName,
                SqlEntityStoreConfiguration config
              )
    {
        this.dialect = dialect;
        this.config = config;
        typesTable = dsl.tableOf( typesTablesName );
        this.dsl = dsl;
        Integer idMaxLength = config.identityLength().get();
        identityColumn = SqlType.makeField( IDENTITY_COLUMN_NAME, String.class, dialect );
        createdColumn = SqlType.makeField( CREATED_COLUMN_NAME, Timestamp.class, dialect );
        modifiedColumn = SqlType.makeField( LASTMODIFIED_COLUMN_NAME, Timestamp.class, dialect );
        tableNameColumn = SqlType.makeField( TABLENAME_COLUMN_NAME, String.class, dialect );
    }

    private String tableNameOf( Class<?> mixinType )
    {
        Result<Record> typeInfo = fetchTypeInfoFromTable( mixinType );
        if( typeInfo.isEmpty() )
        {
            return null;
        }
        return typeInfo.getValue( 0, tableNameColumn );
    }

    Table<Record> tableFor( Class<?> type, EntityDescriptor descriptor )
    {
        return mixinTablesCache.computeIfAbsent( type, t ->
        {
            String tableName = tableNameOf( t );
            if( tableName == null )
            {
                Result<Record> newMixinTable = createNewMixinTable( type, descriptor );
                return dsl.tableOf( newMixinTable.getValue( 0, tableNameColumn ) );
            }
            return dsl.tableOf( tableName );
        } );
    }

    private Result<Record> fetchTypeInfoFromTable( Class<?> mixinTableName )
    {
        return dsl.select()
                  .from( typesTable )
                  .where( identityColumn.eq( mixinTableName.getName() ) )
                  .fetch();
    }

    private Result<Record> createNewMixinTable( Class<?> mixinType, EntityDescriptor descriptor )
    {
        String mixinTypeName = mixinType.getName();
        String tableName = createNewTableName( mixinType );
        CreateTableColumnStep primaryTable = dsl.createTable( dsl.tableOf( tableName ) )
                                                .column( identityColumn )
                                                .column( createdColumn );
        descriptor.state().properties().forEach(
            property ->
            {
                QualifiedName qualifiedName = property.qualifiedName();
                if( qualifiedName.type().replace( '-', '$' ).equals( mixinTypeName ) )
                {
                    primaryTable.column( fieldOf( property ) );
                }
            } );
        descriptor.state().associations().forEach(
            assoc ->
            {
                QualifiedName qualifiedName = assoc.qualifiedName();
                if( qualifiedName.type().replace( '-', '$' ).equals( mixinTypeName ) )
                {
                    primaryTable.column( fieldOf( assoc ) );
                }
            } );
        primaryTable.constraint( DSL.primaryKey( identityColumn ) );

        int result1 = primaryTable.execute();
        int result3 = dsl.insertInto( typesTable )
                         .set( identityColumn, mixinTypeName )
                         .set( tableNameColumn, tableName )
                         .set( createdColumn, new Timestamp( System.currentTimeMillis() ) )
                         .set( modifiedColumn, new Timestamp( System.currentTimeMillis() ) )
                         .execute();
        return fetchTypeInfoFromTable( mixinType );
    }

    private String createNewTableName( Class<?> mixinType )
    {
        String typeName = mixinType.getSimpleName();
        String postFix = "";
        int counter = 1;
        boolean found;
        do
        {
            found = checkForTableNamed( typeName + postFix );
            postFix = "_" + counter++;
        } while( found );
        return typeName;
    }

    private boolean checkForTableNamed( String tableName )
    {
        if( tableName.equalsIgnoreCase( config.entitiesTableName().get() ) || tableName.equalsIgnoreCase( config.typesTableName().get() ) )
        {
            return true;
        }
        return dsl.select()
                  .from( typesTable )
                  .where( tableNameColumn.eq( tableName ) )
                  .fetch().size() > 0;
    }

    Field<Object> fieldOf( PropertyDescriptor descriptor )
    {
        String propertyName = descriptor.qualifiedName().name();
        return DSL.field( DSL.name( propertyName ), dataTypeOf( descriptor ) );
    }

    Field<String> fieldOf( AssociationDescriptor descriptor )
    {
        String propertyName = descriptor.qualifiedName().name();
        return DSL.field( DSL.name( propertyName ), dataTypeOf( descriptor ) );
    }

    private <T> DataType<T> dataTypeOf( PropertyDescriptor property )
    {
        Type type = property.type();

        @SuppressWarnings( "unchecked" )
        Class<T> rawType = (Class<T>) Classes.RAW_CLASS.apply( type );

        return SqlType.getSqlDataTypeFor( dialect, rawType, false );
    }

    private <T> DataType<T> dataTypeOf( AssociationDescriptor property )
    {
        Type type = property.type();

        @SuppressWarnings( "unchecked" )
        Class<T> rawType = (Class<T>) Classes.RAW_CLASS.apply( type );

        return SqlType.getSqlDataTypeFor( dialect, rawType, false );
    }

    Field<String> identityColumn()
    {
        return identityColumn;
    }

    Field<Timestamp> modifiedColumn()
    {
        return modifiedColumn;
    }

    Field<Timestamp> createdColumn()
    {
        return createdColumn;
    }

    void create()
    {
        dsl.createTableIfNotExists( typesTable )
           .column( identityColumn )
           .column( tableNameColumn )
           .column( createdColumn )
           .column( modifiedColumn )
           .constraint( DSL.primaryKey( identityColumn ) )
           .execute();
    }
}
