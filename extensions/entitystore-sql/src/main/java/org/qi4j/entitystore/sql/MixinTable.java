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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;

/**
 * MixinTable is a class that handles the creation of the queries into the Mixin tables, both for insertions/updates
 * as well as retrieval.
 * <p>
 * Note that these tables are only queried as part of a SQL {@code JOIN} statement and never directly.
 * </p>
 * <p>
 * Creation of the actual Mixin tables happens in {@link TypesTable}.
 * </p>
 */
class MixinTable
{
    static final String NAME_COLUMN_NAME = "_name";
    static final String INDEX_COLUMN_NAME = "_index";    // either index in ManyAssociation or name in NamedAssociation
    static final String REFERENCE_COLUMN_NAME = "_reference";
    private static final String ASSOCS_TABLE_POSTFIX = "_ASSOCS";

    private final Table<Record> mixinTable;
    private final Table<Record> mixinAssocsTable;

    private Field<String> nameColumn;
    private Field<String> referenceColumn;
    private Field<String> indexColumn;

    private final JooqDslContext dsl;
    private final Map<QualifiedName, Field<Object>> properties = new ConcurrentHashMap<>();
    private final Map<QualifiedName, Field<String>> associations = new ConcurrentHashMap<>();
    private final List<QualifiedName> manyAssociations = new CopyOnWriteArrayList<>();
    private final List<QualifiedName> namedAssociations = new CopyOnWriteArrayList<>();

    private final Class<?> mixinType;
    private final TypesTable types;
    private Serialization serialization;

    MixinTable( JooqDslContext dsl, SQLDialect dialect, TypesTable types, Class<?> mixinType,
                EntityDescriptor descriptor, Serialization serialization )
    {
        this.types = types;
        this.dsl = dsl;
        this.mixinType = mixinType;
        this.serialization = serialization;
        nameColumn = SqlType.makeField( NAME_COLUMN_NAME, String.class, dialect);
        referenceColumn = SqlType.makeField( REFERENCE_COLUMN_NAME, String.class, dialect );
        indexColumn = SqlType.makeField( INDEX_COLUMN_NAME, String.class, dialect );

        mixinTable = types.tableFor( mixinType, descriptor );
        mixinAssocsTable = getAssocsTable( descriptor );

        descriptor.valueType().properties()
                  .filter( this::isThisMixin )
                  .forEach( propDescriptor ->
                            {
                                QualifiedName propertyName = propDescriptor.qualifiedName();
                                Field<Object> propertyField = types.fieldOf( propDescriptor );
                                properties.put( propertyName, propertyField );
                            }
                          );

        descriptor.valueType().associations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor ->
                            {
                                QualifiedName assocName = assocDescriptor.qualifiedName();
                                Field<String> assocField = types.fieldOf( assocDescriptor );
                                associations.put( assocName, assocField );
                            }
                          );

        descriptor.valueType().manyAssociations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor -> manyAssociations.add( assocDescriptor.qualifiedName() ) );

        descriptor.valueType().namedAssociations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor -> namedAssociations.add( assocDescriptor.qualifiedName() ) );
    }

    void insertMixinState( DefaultEntityState state, String valueIdentity )
    {
        InsertSetMoreStep<Record> primaryTable =
            dsl.insertInto( mixinTable )
               .set( types.identityColumn(), valueIdentity )
               .set( types.createdColumn(), new Timestamp( System.currentTimeMillis() ) );

        properties
            .entrySet()
            .stream()
            .filter( entry -> !entry.getKey().name().equals( "identity" ) )
            .forEach( entry -> primaryTable.set( entry.getValue(), getStateValue( entry.getValue(), state, entry.getKey() ) ) );
        associations.forEach( ( assocName, assocField ) -> primaryTable.set( assocField, referenceToString( state, assocName ) )
                            );
        int result = primaryTable.execute();

        if( mixinAssocsTable != null )
        {
            insertManyAndNamedAssociations( state, valueIdentity );
        }
    }

    private void insertManyAndNamedAssociations( DefaultEntityState state, String valueIdentity )
    {
        manyAssociations.forEach( assocName ->
                                  {
                                      InsertSetStep<Record> assocsTable = dsl.insertInto( mixinAssocsTable );
                                      ManyAssociationState entityReferences = state.manyAssociationValueOf( assocName );
                                      int endCount = entityReferences.count();
                                      int counter = 0;
                                      for( EntityReference ref : entityReferences )
                                      {
                                          InsertSetMoreStep<Record> set = assocsTable.set( types.identityColumn(), valueIdentity )
                                                                                     .set( types.createdColumn(), new Timestamp( System.currentTimeMillis() ) )
                                                                                     .set( nameColumn, assocName.name() )
                                                                                     .set( indexColumn, "" + counter++ )
                                                                                     .set( referenceColumn, ref == null ? null : ref.identity().toString() );
                                          if( ++counter < endCount )
                                          {
                                              set.newRecord();
                                          }
                                      }
                                      if( counter > 0 )
                                      {
                                          InsertSetMoreStep<Record> assocs = assocsTable.set( Collections.emptyMap() );
                                          assocs.execute();
                                      }
                                  } );

        namedAssociations.forEach( assocName ->
                                   {
                                       InsertSetStep<Record> assocsTable = dsl.insertInto( mixinAssocsTable );
                                       NamedAssociationState entityReferences = state.namedAssociationValueOf( assocName );
                                       int count = entityReferences.count();
                                       if( count > 0 )
                                       {
                                           for( String name : entityReferences )
                                           {
                                               EntityReference ref = entityReferences.get( name );
                                               InsertSetMoreStep<Record> set = assocsTable.set( types.identityColumn(), valueIdentity )
                                                                                          .set( types.createdColumn(), new Timestamp( System.currentTimeMillis() ) )
                                                                                          .set( nameColumn, assocName.name() )
                                                                                          .set( indexColumn, name )
                                                                                          .set( referenceColumn, ref.identity().toString() );
                                               if( --count > 0 )
                                               {
                                                   set.newRecord();
                                               }
                                           }
                                           InsertSetMoreStep<Record> assocs = assocsTable.set( Collections.emptyMap() );
                                           assocs.execute();
                                       }
                                   } );
    }

    Table<Record> associationsTable()
    {
        return mixinAssocsTable;
    }

    private boolean isThisMixin( PropertyDescriptor descriptor )
    {
        Class<?> declaringClass = declaredIn( descriptor );
        return mixinType.equals( declaringClass );
    }

    private boolean isThisMixin( AssociationDescriptor descriptor )
    {
        Class<?> declaringClass = declaredIn( descriptor );
        return mixinType.equals( declaringClass );
    }

    private Class<?> declaredIn( PropertyDescriptor descriptor )
    {
        AccessibleObject accessor = descriptor.accessor();
        if( accessor instanceof Method )
        {
            return ( (Method) accessor ).getDeclaringClass();
        }
        throw new UnsupportedOperationException( "Property declared as " + accessor.getClass() + " is not supported in this Entity Store yet." );
    }

    private Class<?> declaredIn( AssociationDescriptor descriptor )
    {
        AccessibleObject accessor = descriptor.accessor();
        if( accessor instanceof Method )
        {
            return ( (Method) accessor ).getDeclaringClass();
        }
        throw new UnsupportedOperationException( "Property declared as " + accessor.getClass() + " is not supported in this Entity Store yet." );
    }

    void modifyMixinState( DefaultEntityState state, String valueId )
    {
        UpdateSetMoreStep<Record> primaryTable =
            dsl.update( mixinTable )
               .set( Collections.emptyMap() );  // empty map is a hack to get the right type returned from JOOQ.

        properties
            .entrySet()
            .stream()
            .filter( entry -> !entry.getKey().name().equals( "identity" ) )
            .forEach( entry -> primaryTable.set( entry.getValue(), getStateValue( entry.getValue(), state, entry.getKey() ) ) );

        // Set the Association<?> fields
        associations.forEach( ( assocName, assocField ) -> primaryTable.set( assocField, referenceToString( state, assocName ) ) );

        int result = primaryTable.execute();

        if( mixinAssocsTable != null )
        {
            // Need to remove existing records.
            dsl.delete( mixinAssocsTable )
               .where( types.identityColumn().eq( valueId ) )
               .execute();
            insertManyAndNamedAssociations( state, valueId );
        }
    }

    private Table<Record> getAssocsTable( EntityDescriptor descriptor )
    {
        if( descriptor.state().manyAssociations().count() > 0
            || descriptor.state().namedAssociations().count() > 0 )
        {
            Table<Record> table = dsl.tableOf( mixinTable.getName() + ASSOCS_TABLE_POSTFIX );
            int result = dsl.createTableIfNotExists( table )
                            .column( types.identityColumn() )
                            .column( types.createdColumn() )
                            .column( nameColumn )
                            .column( indexColumn )
                            .column( referenceColumn )
                            .execute();
            dsl.createIndex( DSL.name( "IDX_" + table.getName() ) )
               .on( table, types.identityColumn() )
               .execute();
            return table;
        }
        else
        {
            return null;
        }
    }

    private Object getStateValue( Field<Object> field, DefaultEntityState state, QualifiedName name )
    {
        PropertyDescriptor property = state.entityDescriptor().state().findPropertyModelByQualifiedName( name );
        ValueType type = property.valueType();
        Object value = state.propertyValueOf( name );
        Class<?> javaType = field.getDataType().getType();
        int sqlType = field.getDataType().getSQLType();

        if( value == null )
        {
            return null;
        }
        if( value.getClass().isPrimitive() )
        {
            return value;
        }
        if( type.equals( ValueType.STRING )
            || type.equals( ValueType.INTEGER )
            || type.equals( ValueType.BOOLEAN )
            || type.equals( ValueType.DOUBLE )
            || type.equals( ValueType.IDENTITY )
            || type.equals( ValueType.LONG )
            || type.equals( ValueType.FLOAT )
            || type.equals( ValueType.BYTE )
            || type.equals( ValueType.CHARACTER )
            || type.equals( ValueType.SHORT )
            )
        {
            return value;
        }
        return serialization.serialize( value );
    }

    private String referenceToString( DefaultEntityState state, QualifiedName assocName )
    {
        EntityReference reference = state.associationValueOf( assocName );
        if( reference == null )
        {
            return null;
        }
        return reference.identity().toString();
    }
}
