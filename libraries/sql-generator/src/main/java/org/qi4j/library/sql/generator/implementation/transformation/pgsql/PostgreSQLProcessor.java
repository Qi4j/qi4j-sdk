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
package org.qi4j.library.sql.generator.implementation.transformation.pgsql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.library.sql.generator.Typeable;
import org.qi4j.library.sql.generator.grammar.booleans.BinaryPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.NotRegexpPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.RegexpPredicate;
import org.qi4j.library.sql.generator.grammar.common.datatypes.BigInt;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLInteger;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SmallInt;
import org.qi4j.library.sql.generator.grammar.common.datatypes.pgsql.Text;
import org.qi4j.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.qi4j.library.sql.generator.grammar.definition.table.TableDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.pgsql.PgSQLTableCommitAction;
import org.qi4j.library.sql.generator.grammar.literals.TimestampTimeLiteral;
import org.qi4j.library.sql.generator.grammar.manipulation.pgsql.PgSQLDropTableOrViewStatement;
import org.qi4j.library.sql.generator.grammar.modification.pgsql.PgSQLInsertStatement;
import org.qi4j.library.sql.generator.grammar.query.LimitSpecification;
import org.qi4j.library.sql.generator.grammar.query.OffsetSpecification;
import org.qi4j.library.sql.generator.grammar.query.QuerySpecification;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.BinaryPredicateProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ConstantProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefaultSQLProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.TableDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.DefinitionProcessing.PGColumnDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.LiteralExpressionProcessing.PGDateTimeLiteralProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.ManipulationProcessing.PgSQLDropTableOrViewStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.ModificationProcessing.PgSQLInsertStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLLimitSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLOffsetSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLQuerySpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class PostgreSQLProcessor extends DefaultSQLProcessor
{

    private static final Map<Class<? extends Typeable<?>>, SQLProcessor> _defaultProcessors;

    private static final Map<Class<? extends BinaryPredicate>, String> _defaultPgSQLBinaryOperators;

    static
    {
        Map<Class<? extends BinaryPredicate>, String> binaryOperators =
            new HashMap<Class<? extends BinaryPredicate>, String>(
                DefaultSQLProcessor.getDefaultBinaryOperators() );
        binaryOperators.put( RegexpPredicate.class, "~" );
        binaryOperators.put( NotRegexpPredicate.class, "!~" );
        _defaultPgSQLBinaryOperators = binaryOperators;

        Map<Class<? extends Typeable<?>>, SQLProcessor> processors =
            new HashMap<Class<? extends Typeable<?>>, SQLProcessor>(
                DefaultSQLProcessor.getDefaultProcessors() );

        // Override default processor for date-time
        processors.put( TimestampTimeLiteral.class, new LiteralExpressionProcessing.PGDateTimeLiteralProcessor() );

        // Override default processor for column definition
        Map<Class<?>, String> dataTypeSerials = new HashMap<Class<?>, String>();
        dataTypeSerials.put( BigInt.class, "BIGSERIAL" );
        dataTypeSerials.put( SQLInteger.class, "SERIAL" );
        dataTypeSerials.put( SmallInt.class, "SMALLSERIAL" );
        processors.put( ColumnDefinition.class,
                        new DefinitionProcessing.PGColumnDefinitionProcessor( Collections.unmodifiableMap( dataTypeSerials ) ) );

        // Add support for regexp comparing
        processors
            .put(
                RegexpPredicate.class,
                new BinaryPredicateProcessor( _defaultPgSQLBinaryOperators
                                                  .get( RegexpPredicate.class ) ) );
        processors.put(
            NotRegexpPredicate.class,
            new BinaryPredicateProcessor( _defaultPgSQLBinaryOperators
                                              .get( NotRegexpPredicate.class ) ) );

        // Add support for PostgreSQL legacy LIMIT/OFFSET
        processors.put( QuerySpecification.class, new QueryProcessing.PgSQLQuerySpecificationProcessor() );
        processors.put( OffsetSpecification.class, new QueryProcessing.PgSQLOffsetSpecificationProcessor() );
        processors.put( LimitSpecification.class, new QueryProcessing.PgSQLLimitSpecificationProcessor() );

        // Add support for "TEXT" data type
        processors.put( Text.class, new ConstantProcessor( "TEXT" ) );

        // Add "DROP" table commit action
        Map<TableCommitAction, String> commitActions = new HashMap<TableCommitAction, String>(
            TableDefinitionProcessor.getDefaultCommitActions() );
        commitActions.put( PgSQLTableCommitAction.DROP, "DROP" );
        processors.put( TableDefinition.class,
                        new TableDefinitionProcessor( TableDefinitionProcessor.getDefaultTableScopes(),
                                                      commitActions ) );

        // Add "IF EXISTS" functionality to DROP TABLE/VIEW statements
        processors.put( PgSQLDropTableOrViewStatement.class,
                        new ManipulationProcessing.PgSQLDropTableOrViewStatementProcessor() );

        // Add support for PostgreSQL-specific INSTERT statement RETURNING clause
        processors.put( PgSQLInsertStatement.class, new ModificationProcessing.PgSQLInsertStatementProcessor() );

        _defaultProcessors = processors;
    }

    public PostgreSQLProcessor( SQLVendor vendor )
    {
        super( vendor, _defaultProcessors );
    }
}
