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
package org.qi4j.library.sql.generator.implementation.transformation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.qi4j.library.sql.generator.Typeable;
import org.qi4j.library.sql.generator.grammar.booleans.BetweenPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.BinaryPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.BooleanExpression.False;
import org.qi4j.library.sql.generator.grammar.booleans.BooleanExpression.True;
import org.qi4j.library.sql.generator.grammar.booleans.BooleanTest;
import org.qi4j.library.sql.generator.grammar.booleans.Conjunction;
import org.qi4j.library.sql.generator.grammar.booleans.Disjunction;
import org.qi4j.library.sql.generator.grammar.booleans.EqualsPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.ExistsPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.GreaterOrEqualPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.GreaterThanPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.InPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.IsNotNullPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.IsNullPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.LessOrEqualPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.LessThanPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.LikePredicate;
import org.qi4j.library.sql.generator.grammar.booleans.MultiPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.Negation;
import org.qi4j.library.sql.generator.grammar.booleans.NotBetweenPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.NotEqualsPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.NotInPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.NotLikePredicate;
import org.qi4j.library.sql.generator.grammar.booleans.Predicate.EmptyPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.UnaryPredicate;
import org.qi4j.library.sql.generator.grammar.booleans.UniquePredicate;
import org.qi4j.library.sql.generator.grammar.common.ColumnNameList;
import org.qi4j.library.sql.generator.grammar.common.SQLConstants;
import org.qi4j.library.sql.generator.grammar.common.TableNameDirect;
import org.qi4j.library.sql.generator.grammar.common.TableNameFunction;
import org.qi4j.library.sql.generator.grammar.common.datatypes.BigInt;
import org.qi4j.library.sql.generator.grammar.common.datatypes.Decimal;
import org.qi4j.library.sql.generator.grammar.common.datatypes.DoublePrecision;
import org.qi4j.library.sql.generator.grammar.common.datatypes.Numeric;
import org.qi4j.library.sql.generator.grammar.common.datatypes.Real;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLBoolean;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLChar;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLDate;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLFloat;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLInteger;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLInterval;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLTime;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLTimeStamp;
import org.qi4j.library.sql.generator.grammar.common.datatypes.SmallInt;
import org.qi4j.library.sql.generator.grammar.common.datatypes.UserDefinedType;
import org.qi4j.library.sql.generator.grammar.definition.schema.SchemaDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.CheckConstraint;
import org.qi4j.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.ForeignKeyConstraint;
import org.qi4j.library.sql.generator.grammar.definition.table.LikeClause;
import org.qi4j.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.TableDefinition;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElementList;
import org.qi4j.library.sql.generator.grammar.definition.table.UniqueConstraint;
import org.qi4j.library.sql.generator.grammar.definition.view.RegularViewSpecification;
import org.qi4j.library.sql.generator.grammar.definition.view.ViewDefinition;
import org.qi4j.library.sql.generator.grammar.literals.DirectLiteral;
import org.qi4j.library.sql.generator.grammar.literals.NumericLiteral;
import org.qi4j.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.qi4j.library.sql.generator.grammar.literals.StringLiteral;
import org.qi4j.library.sql.generator.grammar.literals.TimestampTimeLiteral;
import org.qi4j.library.sql.generator.grammar.manipulation.AddColumnDefinition;
import org.qi4j.library.sql.generator.grammar.manipulation.AddTableConstraintDefinition;
import org.qi4j.library.sql.generator.grammar.manipulation.AlterColumnAction.DropDefault;
import org.qi4j.library.sql.generator.grammar.manipulation.AlterColumnDefinition;
import org.qi4j.library.sql.generator.grammar.manipulation.AlterTableStatement;
import org.qi4j.library.sql.generator.grammar.manipulation.DropColumnDefinition;
import org.qi4j.library.sql.generator.grammar.manipulation.DropSchemaStatement;
import org.qi4j.library.sql.generator.grammar.manipulation.DropTableConstraintDefinition;
import org.qi4j.library.sql.generator.grammar.manipulation.DropTableOrViewStatement;
import org.qi4j.library.sql.generator.grammar.manipulation.SetColumnDefault;
import org.qi4j.library.sql.generator.grammar.modification.ColumnSource.Defaults;
import org.qi4j.library.sql.generator.grammar.modification.ColumnSourceByQuery;
import org.qi4j.library.sql.generator.grammar.modification.ColumnSourceByValues;
import org.qi4j.library.sql.generator.grammar.modification.DeleteBySearch;
import org.qi4j.library.sql.generator.grammar.modification.InsertStatement;
import org.qi4j.library.sql.generator.grammar.modification.SetClause;
import org.qi4j.library.sql.generator.grammar.modification.TargetTable;
import org.qi4j.library.sql.generator.grammar.modification.UpdateBySearch;
import org.qi4j.library.sql.generator.grammar.modification.UpdateSourceByExpression;
import org.qi4j.library.sql.generator.grammar.modification.ValueSource;
import org.qi4j.library.sql.generator.grammar.query.AsteriskSelect;
import org.qi4j.library.sql.generator.grammar.query.ColumnReferenceByExpression;
import org.qi4j.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.qi4j.library.sql.generator.grammar.query.ColumnReferences;
import org.qi4j.library.sql.generator.grammar.query.CorrespondingSpec;
import org.qi4j.library.sql.generator.grammar.query.FromClause;
import org.qi4j.library.sql.generator.grammar.query.GroupByClause;
import org.qi4j.library.sql.generator.grammar.query.GroupingElement.GrandTotal;
import org.qi4j.library.sql.generator.grammar.query.LimitSpecification;
import org.qi4j.library.sql.generator.grammar.query.OffsetSpecification;
import org.qi4j.library.sql.generator.grammar.query.OrderByClause;
import org.qi4j.library.sql.generator.grammar.query.OrdinaryGroupingSet;
import org.qi4j.library.sql.generator.grammar.query.QueryExpression;
import org.qi4j.library.sql.generator.grammar.query.QueryExpressionBody.EmptyQueryExpressionBody;
import org.qi4j.library.sql.generator.grammar.query.QueryExpressionBodyBinary;
import org.qi4j.library.sql.generator.grammar.query.QuerySpecification;
import org.qi4j.library.sql.generator.grammar.query.RowDefinition;
import org.qi4j.library.sql.generator.grammar.query.RowSubQuery;
import org.qi4j.library.sql.generator.grammar.query.SortSpecification;
import org.qi4j.library.sql.generator.grammar.query.TableReferenceByExpression;
import org.qi4j.library.sql.generator.grammar.query.TableReferenceByName;
import org.qi4j.library.sql.generator.grammar.query.TableValueConstructor;
import org.qi4j.library.sql.generator.grammar.query.joins.CrossJoinedTable;
import org.qi4j.library.sql.generator.grammar.query.joins.JoinCondition;
import org.qi4j.library.sql.generator.grammar.query.joins.NamedColumnsJoin;
import org.qi4j.library.sql.generator.grammar.query.joins.NaturalJoinedTable;
import org.qi4j.library.sql.generator.grammar.query.joins.QualifiedJoinedTable;
import org.qi4j.library.sql.generator.grammar.query.joins.UnionJoinedTable;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.BinaryPredicateProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.BooleanTestProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.ConjunctionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.DisjunctionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.MultiPredicateProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.NegationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.UnaryPredicateProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.UnaryPredicateProcessor.UnaryOperatorOrientation;
import org.qi4j.library.sql.generator.implementation.transformation.ColumnProcessing.ColumnNamesProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ColumnProcessing.ColumnReferenceByExpressionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ColumnProcessing.ColumnReferenceByNameProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.DecimalProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.NumericProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.SQLCharProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.SQLFloatProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.SQLIntervalProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.SQLTimeProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.SQLTimeStampProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DataTypeProcessing.UserDefinedDataTypeProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.CheckConstraintProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.ColumnDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.ForeignKeyConstraintProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.LikeClauseProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.RegularViewSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.SchemaDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.TableConstraintDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.TableDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.TableElementListProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.UniqueConstraintProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.DefinitionProcessing.ViewDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.LiteralExpressionProcessing.DateTimeLiteralProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.LiteralExpressionProcessing.DirectLiteralProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.LiteralExpressionProcessing.NumericLiteralProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.LiteralExpressionProcessing.SQLFunctionLiteralProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.LiteralExpressionProcessing.StringLiteralExpressionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.AddColumnDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.AddTableConstraintDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.AlterColumnDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.AlterTableStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.DropColumnDefaultProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.DropColumnDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.DropSchemaStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.DropTableConstraintDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.DropTableOrViewStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ManipulationProcessing.SetColumnDefaultProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.ColumnSourceByQueryProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.ColumnSourceByValuesProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.DeleteBySearchProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.InsertStatementProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.SetClauseProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.TargetTableProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.UpdateBySearchProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.ModificationProcessing.UpdateSourceByExpressionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.CorrespondingSpecProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.FromProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.GroupByProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.LimitSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.OffsetSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.OrderByProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.OrdinaryGroupingSetProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.QueryExpressionBinaryProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.QueryExpressionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.QuerySpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.RowDefinitionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.RowSubQueryProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.SelectColumnsProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.SortSpecificationProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.QueryProcessing.TableValueConstructorProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.CrossJoinedTableProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.JoinConditionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.NamedColumnsJoinProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.NaturalJoinedTableProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.QualifiedJoinedTableProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableNameDirectProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableNameFunctionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableReferenceByExpressionProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableReferenceByNameProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.TableReferenceProcessing.UnionJoinedTableProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.vendor.UnsupportedElementException;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.vendor.UnsupportedElementException;

/**
 * This is base class for processing the SQL syntax elements defined in API. It contains the default
 * processors for nearly all default syntax elements, and a way to easily extend this class in order
 * to add custom processors, or replace default processors with custom ones.
 *
 *
 */
public class DefaultSQLProcessor
    implements SQLProcessorAggregator
{

    private static final Map<Class<? extends Typeable<?>>, SQLProcessor> _defaultProcessors;

    private static final Map<Class<? extends UnaryPredicate>, UnaryOperatorOrientation> _defaultUnaryOrientations;

    private static final Map<Class<? extends UnaryPredicate>, String> _defaultUnaryOperators;

    private static final Map<Class<? extends BinaryPredicate>, String> _defaultBinaryOperators;

    private static final Map<Class<? extends MultiPredicate>, String> _defaultMultiOperators;

    private static final Map<Class<? extends MultiPredicate>, String> _defaultMultiSeparators;

    private static final Map<Class<? extends MultiPredicate>, Boolean> _defaultParenthesisPolicies;

    static
    {
        Map<Class<? extends UnaryPredicate>, UnaryOperatorOrientation> unaryOrientations =
            new HashMap<Class<? extends UnaryPredicate>, UnaryOperatorOrientation>();
        unaryOrientations.put( IsNullPredicate.class, UnaryOperatorOrientation.AFTER_EXPRESSION );
        unaryOrientations.put( IsNotNullPredicate.class, UnaryOperatorOrientation.AFTER_EXPRESSION );
        unaryOrientations.put( ExistsPredicate.class, UnaryOperatorOrientation.BEFORE_EXPRESSION );
        unaryOrientations.put( UniquePredicate.class, UnaryOperatorOrientation.BEFORE_EXPRESSION );
        _defaultUnaryOrientations = Collections.unmodifiableMap( unaryOrientations );

        Map<Class<? extends UnaryPredicate>, String> unaryOperators =
            new HashMap<Class<? extends UnaryPredicate>, String>();
        unaryOperators.put( IsNullPredicate.class, "IS NULL" );
        unaryOperators.put( IsNotNullPredicate.class, "IS NOT NULL" );
        unaryOperators.put( ExistsPredicate.class, "EXISTS" );
        unaryOperators.put( UniquePredicate.class, "UNIQUE" );
        _defaultUnaryOperators = Collections.unmodifiableMap( unaryOperators );

        Map<Class<? extends BinaryPredicate>, String> binaryOperators =
            new HashMap<Class<? extends BinaryPredicate>, String>();
        binaryOperators.put( EqualsPredicate.class, "=" );
        binaryOperators.put( NotEqualsPredicate.class, "<>" );
        binaryOperators.put( GreaterOrEqualPredicate.class, ">=" );
        binaryOperators.put( GreaterThanPredicate.class, ">" );
        binaryOperators.put( LessOrEqualPredicate.class, "<=" );
        binaryOperators.put( LessThanPredicate.class, "<" );
        binaryOperators.put( LikePredicate.class, "LIKE" );
        binaryOperators.put( NotLikePredicate.class, "NOT LIKE" );
        _defaultBinaryOperators = Collections.unmodifiableMap( binaryOperators );

        Map<Class<? extends MultiPredicate>, String> multiOperators =
            new HashMap<Class<? extends MultiPredicate>, String>();
        multiOperators.put( BetweenPredicate.class, "BETWEEN" );
        multiOperators.put( InPredicate.class, "IN" );
        multiOperators.put( NotBetweenPredicate.class, "NOT BETWEEN" );
        multiOperators.put( NotInPredicate.class, "NOT IN" );
        _defaultMultiOperators = Collections.unmodifiableMap( multiOperators );

        Map<Class<? extends MultiPredicate>, String> multiSeparators =
            new HashMap<Class<? extends MultiPredicate>, String>();
        multiSeparators.put( BetweenPredicate.class, " AND " );
        multiSeparators.put( InPredicate.class, ", " );
        multiSeparators.put( NotBetweenPredicate.class, " AND " );
        multiSeparators.put( NotInPredicate.class, ", " );
        _defaultMultiSeparators = Collections.unmodifiableMap( multiSeparators );

        Map<Class<? extends MultiPredicate>, Boolean> parenthesisPolicies =
            new HashMap<Class<? extends MultiPredicate>, Boolean>();
        parenthesisPolicies.put( BetweenPredicate.class, false );
        parenthesisPolicies.put( InPredicate.class, true );
        parenthesisPolicies.put( NotBetweenPredicate.class, false );
        parenthesisPolicies.put( NotInPredicate.class, true );
        _defaultParenthesisPolicies = parenthesisPolicies;

        Map<Class<? extends Typeable<?>>, SQLProcessor> processors =
            new HashMap<Class<? extends Typeable<?>>, SQLProcessor>();

        // Boolean expressions
        // Constants
        processors.put( True.class, new ConstantProcessor( "TRUE" ) );
        processors.put( False.class, new ConstantProcessor( "FALSE" ) );
        // Unary
        processors.put(
            IsNullPredicate.class,
            new UnaryPredicateProcessor( _defaultUnaryOrientations.get( IsNullPredicate.class ),
                                         _defaultUnaryOperators
                                             .get( IsNullPredicate.class ) ) );
        processors.put( IsNotNullPredicate.class,
                        new UnaryPredicateProcessor( _defaultUnaryOrientations.get( IsNotNullPredicate.class ),
                                                     _defaultUnaryOperators.get( IsNotNullPredicate.class ) ) );
        processors.put(
            ExistsPredicate.class,
            new UnaryPredicateProcessor( _defaultUnaryOrientations.get( ExistsPredicate.class ),
                                         _defaultUnaryOperators
                                             .get( ExistsPredicate.class ) ) );
        processors.put(
            UniquePredicate.class,
            new UnaryPredicateProcessor( _defaultUnaryOrientations.get( UniquePredicate.class ),
                                         _defaultUnaryOperators
                                             .get( UniquePredicate.class ) ) );
        // Binary
        processors.put( EqualsPredicate.class,
                        new BinaryPredicateProcessor( _defaultBinaryOperators.get( EqualsPredicate.class ) ) );
        processors
            .put(
                NotEqualsPredicate.class,
                new BinaryPredicateProcessor( _defaultBinaryOperators
                                                  .get( NotEqualsPredicate.class ) ) );
        processors.put(
            GreaterOrEqualPredicate.class,
            new BinaryPredicateProcessor( _defaultBinaryOperators
                                              .get( GreaterOrEqualPredicate.class ) ) );
        processors
            .put(
                GreaterThanPredicate.class,
                new BinaryPredicateProcessor( _defaultBinaryOperators
                                                  .get( GreaterThanPredicate.class ) ) );
        processors
            .put(
                LessOrEqualPredicate.class,
                new BinaryPredicateProcessor( _defaultBinaryOperators
                                                  .get( LessOrEqualPredicate.class ) ) );
        processors.put( LessThanPredicate.class,
                        new BinaryPredicateProcessor( _defaultBinaryOperators.get( LessThanPredicate.class ) ) );
        processors.put( LikePredicate.class,
                        new BinaryPredicateProcessor( _defaultBinaryOperators.get( LikePredicate.class ) ) );
        processors.put( NotLikePredicate.class,
                        new BinaryPredicateProcessor( _defaultBinaryOperators.get( NotLikePredicate.class ) ) );
        // Multi
        processors.put(
            BetweenPredicate.class,
            new MultiPredicateProcessor( _defaultMultiOperators.get( BetweenPredicate.class ),
                                         _defaultMultiSeparators
                                             .get( BetweenPredicate.class ), _defaultParenthesisPolicies
                                             .get( BetweenPredicate.class ) ) );
        processors.put(
            InPredicate.class,
            new MultiPredicateProcessor(
                _defaultMultiOperators.get( InPredicate.class ), _defaultMultiSeparators
                .get( InPredicate.class ),
                _defaultParenthesisPolicies.get( InPredicate.class ) ) );
        processors.put(
            NotBetweenPredicate.class,
            new MultiPredicateProcessor( _defaultMultiOperators.get( NotBetweenPredicate.class ),
                                         _defaultMultiSeparators.get( NotBetweenPredicate.class ),
                                         _defaultParenthesisPolicies
                                             .get( NotBetweenPredicate.class ) ) );
        processors.put(
            NotInPredicate.class,
            new MultiPredicateProcessor( _defaultMultiOperators.get( NotInPredicate.class ),
                                         _defaultMultiSeparators
                                             .get( NotInPredicate.class ), _defaultParenthesisPolicies
                                             .get( NotInPredicate.class ) ) );
        // Composed
        processors.put( Conjunction.class, new ConjunctionProcessor() );
        processors.put( Disjunction.class, new DisjunctionProcessor() );
        processors.put( Negation.class, new NegationProcessor() );
        processors.put( BooleanTest.class, new BooleanTestProcessor() );
        // Empty
        processors.put( EmptyPredicate.class, new NoOpProcessor() );

        // Column references
        processors.put( ColumnReferenceByName.class, new ColumnProcessing.ColumnReferenceByNameProcessor() );
        processors.put( ColumnReferenceByExpression.class,
                        new ColumnProcessing.ColumnReferenceByExpressionProcessor() );
        processors.put( ColumnNameList.class, new ColumnProcessing.ColumnNamesProcessor() );

        // Literals
        processors.put( StringLiteral.class, new StringLiteralExpressionProcessor() );
        processors.put( TimestampTimeLiteral.class, new DateTimeLiteralProcessor() );
        processors.put( SQLFunctionLiteral.class, new SQLFunctionLiteralProcessor() );
        processors.put( NumericLiteral.class, new NumericLiteralProcessor() );
        processors.put( DirectLiteral.class, new DirectLiteralProcessor() );

        // Queries
        processors.put( QueryExpressionBodyBinary.class, new QueryExpressionBinaryProcessor() );
        processors.put( QuerySpecification.class, new QuerySpecificationProcessor() );
        processors.put( QueryExpression.class, new QueryExpressionProcessor() );
        processors.put( EmptyQueryExpressionBody.class, new NoOpProcessor() );
        processors.put( CorrespondingSpec.class, new CorrespondingSpecProcessor() );
        processors.put( GrandTotal.class, new ConstantProcessor( SQLConstants.OPEN_PARENTHESIS
                                                                 + SQLConstants.CLOSE_PARENTHESIS ) );
        processors.put( OrdinaryGroupingSet.class, new OrdinaryGroupingSetProcessor() );
        processors.put( SortSpecification.class, new SortSpecificationProcessor() );
        processors.put( GroupByClause.class, new GroupByProcessor() );
        processors.put( OrderByClause.class, new OrderByProcessor() );
        processors.put( FromClause.class, new FromProcessor() );
        SelectColumnsProcessor selectProcessor = new SelectColumnsProcessor();
        processors.put( AsteriskSelect.class, selectProcessor );
        processors.put( ColumnReferences.class, selectProcessor );
        processors.put( TableValueConstructor.class, new TableValueConstructorProcessor() );
        processors.put( RowDefinition.class, new RowDefinitionProcessor() );
        processors.put( RowSubQuery.class, new RowSubQueryProcessor() );
        processors.put( OffsetSpecification.class, new OffsetSpecificationProcessor() );
        processors.put( LimitSpecification.class, new LimitSpecificationProcessor() );

        // Table references
        processors.put( TableNameDirect.class, new TableNameDirectProcessor() );
        processors.put( TableNameFunction.class, new TableNameFunctionProcessor() );
        processors.put( TableReferenceByName.class, new TableReferenceByNameProcessor() );
        processors
            .put( TableReferenceByExpression.class, new TableReferenceByExpressionProcessor() );
        processors.put( CrossJoinedTable.class, new CrossJoinedTableProcessor() );
        processors.put( NaturalJoinedTable.class, new NaturalJoinedTableProcessor() );
        processors.put( QualifiedJoinedTable.class, new QualifiedJoinedTableProcessor() );
        processors.put( UnionJoinedTable.class, new UnionJoinedTableProcessor() );
        processors.put( JoinCondition.class, new JoinConditionProcessor() );
        processors.put( NamedColumnsJoin.class, new NamedColumnsJoinProcessor() );

        // Modification clauses
        processors.put( ColumnSourceByQuery.class, new ColumnSourceByQueryProcessor() );
        processors.put( ColumnSourceByValues.class, new ColumnSourceByValuesProcessor() );
        processors.put( DeleteBySearch.class, new DeleteBySearchProcessor() );
        processors.put( InsertStatement.class, new InsertStatementProcessor() );
        processors.put( SetClause.class, new SetClauseProcessor() );
        processors.put( TargetTable.class, new TargetTableProcessor() );
        processors.put( UpdateBySearch.class, new UpdateBySearchProcessor() );
        processors.put( UpdateSourceByExpression.class, new UpdateSourceByExpressionProcessor() );
        processors.put( ValueSource.Null.class, new ConstantProcessor( "NULL" ) );
        processors.put( ValueSource.Default.class, new ConstantProcessor( "DEFAULT" ) );
        processors.put( Defaults.class, new ConstantProcessor( SQLConstants.TOKEN_SEPARATOR
                                                               + "DEFAULT VALUES" ) );

        // Data definition
        // First data types
        processors.put( BigInt.class, new ConstantProcessor( "BIGINT" ) );
        processors.put( DoublePrecision.class, new ConstantProcessor( "DOUBLE PRECISION" ) );
        processors.put( Real.class, new ConstantProcessor( "REAL" ) );
        processors.put( SmallInt.class, new ConstantProcessor( "SMALLINT" ) );
        processors.put( SQLBoolean.class, new ConstantProcessor( "BOOLEAN" ) );
        processors.put( SQLDate.class, new ConstantProcessor( "DATE" ) );
        processors.put( SQLInteger.class, new ConstantProcessor( "INTEGER" ) );
        processors.put( UserDefinedType.class, new UserDefinedDataTypeProcessor() );
        processors.put( Decimal.class, new DecimalProcessor() );
        processors.put( Numeric.class, new NumericProcessor() );
        processors.put( SQLChar.class, new SQLCharProcessor() );
        processors.put( SQLFloat.class, new SQLFloatProcessor() );
        processors.put( SQLInterval.class, new SQLIntervalProcessor() );
        processors.put( SQLTime.class, new SQLTimeProcessor() );
        processors.put( SQLTimeStamp.class, new SQLTimeStampProcessor() );
        // Then statements and clauses
        processors.put( SchemaDefinition.class, new SchemaDefinitionProcessor() );
        processors.put( TableDefinition.class, new TableDefinitionProcessor() );
        processors.put( TableElementList.class, new TableElementListProcessor() );
        processors.put( ColumnDefinition.class, new ColumnDefinitionProcessor() );
        processors.put( LikeClause.class, new LikeClauseProcessor() );
        processors.put( TableConstraintDefinition.class, new TableConstraintDefinitionProcessor() );
        processors.put( CheckConstraint.class, new CheckConstraintProcessor() );
        processors.put( UniqueConstraint.class, new UniqueConstraintProcessor() );
        processors.put( ForeignKeyConstraint.class, new ForeignKeyConstraintProcessor() );
        processors.put( ViewDefinition.class, new ViewDefinitionProcessor() );
        processors.put( RegularViewSpecification.class, new RegularViewSpecificationProcessor() );

        // Data manipulation
        processors.put( AlterTableStatement.class, new AlterTableStatementProcessor() );
        processors.put( AddColumnDefinition.class, new AddColumnDefinitionProcessor() );
        processors.put( AddTableConstraintDefinition.class,
                        new AddTableConstraintDefinitionProcessor() );
        processors.put( AlterColumnDefinition.class, new AlterColumnDefinitionProcessor() );
        processors.put( DropDefault.class, new DropColumnDefaultProcessor() );
        processors.put( SetColumnDefault.class, new SetColumnDefaultProcessor() );
        processors.put( DropColumnDefinition.class, new DropColumnDefinitionProcessor() );
        processors.put( DropTableConstraintDefinition.class,
                        new DropTableConstraintDefinitionProcessor() );
        processors.put( DropSchemaStatement.class, new DropSchemaStatementProcessor() );
        processors.put( DropTableOrViewStatement.class, new DropTableOrViewStatementProcessor() );

        _defaultProcessors = Collections.unmodifiableMap( processors );
    }

    private final Map<Class<? extends Typeable<?>>, SQLProcessor> _processors;
    private final SQLVendor _vendor;

    public DefaultSQLProcessor( SQLVendor vendor )
    {
        this( vendor, _defaultProcessors );
    }

    public DefaultSQLProcessor( SQLVendor vendor,
                                Map<Class<? extends Typeable<?>>, SQLProcessor> processors )
    {
        Objects.requireNonNull( vendor, "Vendor" );
        Objects.requireNonNull( processors, "Processors" );
        this._vendor = vendor;
        this._processors = new HashMap<>( processors );
    }

    public void process( Typeable<?> object, StringBuilder builder )
    {
        SQLProcessor processor = this._processors.get( object.getImplementedType() );
        if( processor != null )
        {
            processor.process( this, object, builder );
        }
        else
        {
            throw new UnsupportedElementException( "The vendor " + this.getClass()
                                                   + " does not know how to handle element of type " + object.getImplementedType()
                                                   + ".", object );
        }
    }

    public SQLVendor getVendor()
    {
        return this._vendor;
    }

    protected Map<Class<? extends Typeable<?>>, SQLProcessor> getProcessors()
    {
        return this._processors;
    }

    public static Map<Class<? extends Typeable<?>>, SQLProcessor> getDefaultProcessors()
    {
        return _defaultProcessors;
    }

    public static Map<Class<? extends BinaryPredicate>, String> getDefaultBinaryOperators()
    {
        return _defaultBinaryOperators;
    }

    public static Map<Class<? extends MultiPredicate>, String> getDefaultMultiOperators()
    {
        return _defaultMultiOperators;
    }

    public static Map<Class<? extends MultiPredicate>, String> getDefaultMultiSeparators()
    {
        return _defaultMultiSeparators;
    }

    public static Map<Class<? extends MultiPredicate>, Boolean> getDefaultParenthesisPolicies()
    {
        return _defaultParenthesisPolicies;
    }

    public static Map<Class<? extends UnaryPredicate>, String> getDefaultUnaryOperators()
    {
        return _defaultUnaryOperators;
    }

    public static Map<Class<? extends UnaryPredicate>, UnaryOperatorOrientation>
    getDefaultUnaryOrientations()
    {
        return _defaultUnaryOrientations;
    }
}
