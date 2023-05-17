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
package org.qi4j.index.rdf.query.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringEscapeUtils;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.grammar.AndPredicate;
import org.qi4j.api.query.grammar.AssociationNotNullPredicate;
import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.ContainsAllPredicate;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.EqPredicate;
import org.qi4j.api.query.grammar.GePredicate;
import org.qi4j.api.query.grammar.GtPredicate;
import org.qi4j.api.query.grammar.LePredicate;
import org.qi4j.api.query.grammar.LtPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.NePredicate;
import org.qi4j.api.query.grammar.Notpredicate;
import org.qi4j.api.query.grammar.OrPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyNotNullPredicate;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.QuerySpecification;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.index.rdf.query.RdfQueryParser;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.serialization.JsonSerializer;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfQueryParserImpl
    implements RdfQueryParser
{
    private static final Map<Class<? extends ComparisonPredicate>, String> OPERATORS;
    private static final Set<Character> RESERVED_CHARS;

    private final Namespaces namespaces = new Namespaces();
    private final Triples triples = new Triples( namespaces );
    private final Qi4jSPI spi;
    private final JsonSerializer stateSerializer;
    private Map<String, Object> variables;

    static
    {
        OPERATORS = new HashMap<>( 6 );
        OPERATORS.put( EqPredicate.class, "=" );
        OPERATORS.put( GePredicate.class, ">=" );
        OPERATORS.put( GtPredicate.class, ">" );
        OPERATORS.put( LePredicate.class, "<=" );
        OPERATORS.put( LtPredicate.class, "<" );
        OPERATORS.put( NePredicate.class, "!=" );

        RESERVED_CHARS = new HashSet<>( Arrays.asList(
            '\"', '^', '.', '\\', '?', '*', '+', '{', '}', '(', ')', '|', '$', '[', ']'
        ) );
    }

    public RdfQueryParserImpl( Qi4jSPI spi, JsonSerializer stateSerializer )
    {
        this.spi = spi;
        this.stateSerializer = stateSerializer;
    }

    @Override
    public String constructQuery( final Class<?> resultType,
                                  final Predicate<Composite> specification,
                                  final List<OrderBy> orderBySegments,
                                  final Integer firstResult,
                                  final Integer maxResults,
                                  final Map<String, Object> variables
    )
    {
        this.variables = variables;

        if( QuerySpecification.isQueryLanguage( "SPARQL", specification ) )
        {
            // Custom query
            StringBuilder queryBuilder = new StringBuilder();
            String query = ( (QuerySpecification) specification ).query();
            queryBuilder.append( query );

            if( orderBySegments != null )
            {
                queryBuilder.append( "\nORDER BY " );
                processOrderBy( orderBySegments, queryBuilder );
            }
            if( firstResult != null )
            {
                queryBuilder.append( "\nOFFSET " ).append( firstResult );
            }
            if( maxResults != null )
            {
                queryBuilder.append( "\nLIMIT " ).append( maxResults );
            }

            return queryBuilder.toString();
        }
        else
        {
            // Add type+reference triples last. This makes queries faster since the query engine can reduce the number
            // of triples to check faster
            triples.addDefaultTriples( resultType.getName() );
        }

        // and collect namespaces
        StringBuilder filter = new StringBuilder();
        processFilter( specification, true, filter );
        StringBuilder orderBy = new StringBuilder();
        processOrderBy( orderBySegments, orderBy );

        StringBuilder query = new StringBuilder();

        for( String namespace : namespaces.namespaces() )
        {
            query.append( format( "PREFIX %s: <%s> %n", namespaces.namespacePrefix( namespace ), namespace ) );
        }
        query.append( "SELECT DISTINCT ?reference\n" );
        if( triples.hasTriples() )
        {
            query.append( "WHERE {\n" );
            StringBuilder optional = new StringBuilder();
            for( Triples.Triple triple : triples )
            {
                final String subject = triple.subject();
                final String predicate = triple.predicate();
                final String value = triple.value();

                if( triple.isOptional() )
                {
                    optional.append( format( "OPTIONAL {%s %s %s}. ", subject, predicate, value ) );
                    optional.append( '\n' );
                }
                else
                {
                    query.append( format( "%s %s %s. ", subject, predicate, value ) );
                    query.append( '\n' );
                }
            }

            // Add OPTIONAL statements last
            if( optional.length() > 0 )
            {
                query.append( optional.toString() );
            }

            if( filter.length() > 0 )
            {
                query.append( "FILTER " ).append( filter );
            }
            query.append( "\n}" );
        }
        if( orderBy.length() > 0 )
        {
            query.append( "\nORDER BY " ).append( orderBy );
        }
        if( firstResult != null )
        {
            query.append( "\nOFFSET " ).append( firstResult );
        }
        if( maxResults != null )
        {
            query.append( "\nLIMIT " ).append( maxResults );
        }

        LoggerFactory.getLogger( getClass() ).debug( "Query:\n" + query );
        return query.toString();
    }

    private void processFilter( final Predicate<Composite> expression, boolean allowInline, StringBuilder builder )
    {
        if( expression == null )
        {
            return;
        }

        if( expression instanceof AndPredicate )
        {
            final AndPredicate conjunction = (AndPredicate) expression;

            int start = builder.length();
            boolean first = true;
            for( Predicate<Composite> operand : conjunction.operands() )
            {
                int size = builder.length();
                processFilter( operand, allowInline, builder );
                if( builder.length() > size )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        builder.insert( size, " && " );
                    }
                }
            }

            if( builder.length() > start )
            {
                builder.insert( start, '(' );
                builder.append( ')' );
            }
        }
        else if( expression instanceof OrPredicate )
        {
            final OrPredicate disjunction = (OrPredicate) expression;

            int start = builder.length();
            boolean first = true;
            for( Predicate<Composite> operand : disjunction.operands() )
            {
                int size = builder.length();
                processFilter( operand, false, builder );
                if( builder.length() > size )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        builder.insert( size, "||" );
                    }
                }
            }

            if( builder.length() > start )
            {
                builder.insert( start, '(' );
                builder.append( ')' );
            }
        }
        else if( expression instanceof Notpredicate )
        {
            builder.insert( 0, "(!" );
            processFilter( ( (Notpredicate) expression ).operand(), false, builder );
            builder.append( ")" );
        }
        else if( expression instanceof ComparisonPredicate )
        {
            processComparisonPredicate( expression, allowInline, builder );
        }
        else if( expression instanceof ContainsAllPredicate )
        {
            processContainsAllPredicate( (ContainsAllPredicate) expression, builder );
        }
        else if( expression instanceof ContainsPredicate<?> )
        {
            processContainsPredicate( (ContainsPredicate<?>) expression, builder );
        }
        else if( expression instanceof MatchesPredicate )
        {
            processMatchesPredicate( (MatchesPredicate) expression, builder );
        }
        else if( expression instanceof PropertyNotNullPredicate<?> )
        {
            processNotNullPredicate( (PropertyNotNullPredicate) expression, builder );
        }
        else if( expression instanceof PropertyNullPredicate<?> )
        {
            processNullPredicate( (PropertyNullPredicate) expression, builder );
        }
        else if( expression instanceof AssociationNotNullPredicate<?> )
        {
            processNotNullPredicate( (AssociationNotNullPredicate) expression, builder );
        }
        else if( expression instanceof AssociationNullPredicate<?> )
        {
            processNullPredicate( (AssociationNullPredicate) expression, builder );
        }
        else if( expression instanceof ManyAssociationContainsPredicate<?> )
        {
            processManyAssociationContainsPredicate( (ManyAssociationContainsPredicate) expression, allowInline, builder );
        }
        else
        {
            throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
        }
    }

    private static void join( String[] strings, String delimiter, StringBuilder builder )
    {
        for( Integer x = 0; x < strings.length; ++x )
        {
            builder.append( strings[ x] );
            if( x + 1 < strings.length )
            {
                builder.append( delimiter );
            }
        }
    }

    private String createAndEscapeJSONString( Object value )
    {
        String serialized = stateSerializer.serialize( Serializer.Options.NO_TYPE_INFO, value );
        return escapeJSONString( serialized );
    }

    private String createRegexStringForContaining( String valueVariable, String containedString )
    {
        // The matching value must start with [, then contain something (possibly nothing),
        // then our value, then again something (possibly nothing), and end with ]
        return format( "regex(str(%s), \"^\\\\u005B.*%s.*\\\\u005D$\", \"s\")", valueVariable, containedString );
    }

    private String escapeJSONString( String jsonStr )
    {
        StringBuilder builder = new StringBuilder();
        char[] chars = jsonStr.toCharArray();
        for( int i = 0; i < chars.length; i++ )
        {
            char c = chars[ i];

            /*
             if ( reservedJsonChars.contains( c ))
             {
             builder.append( "\\\\u" ).append( format( "%04X", (int) '\\' ) );
             }
             */
            if( RESERVED_CHARS.contains( c ) )
            {
                builder.append( "\\\\u" ).append( format( "%04X", (int) c ) );
            }
            else
            {
                builder.append( c );
            }
        }

        return builder.toString();
    }

    private void processContainsAllPredicate( final ContainsAllPredicate<?> predicate, StringBuilder builder )
    {
        Collection<?> values = predicate.containedValues();
        String valueVariable = triples.addTriple( predicate.collectionProperty(), false ).value();
        String[] strings = new String[ values.size() ];
        Integer x = 0;
        for( Object item : values )
        {
            String jsonStr = "";
            if( item != null )
            {
                String serialized = stateSerializer.serialize( Serializer.Options.NO_TYPE_INFO, item );
                if( item instanceof String )
                {
                    serialized = "\"" + StringEscapeUtils.escapeJava( serialized ) + "\"";
                }
                jsonStr = escapeJSONString( serialized );
            }
            strings[ x] = this.createRegexStringForContaining( valueVariable, jsonStr );
            x++;
        }

        if( strings.length > 0 )
        {
            // For some reason, just "FILTER ()" causes error in SPARQL query
            builder.append( "(" );
            join( strings, " && ", builder );
            builder.append( ")" );
        }
        else
        {
            builder.append( this.createRegexStringForContaining( valueVariable, "" ) );
        }
    }

    private void processContainsPredicate( final ContainsPredicate<?> predicate, StringBuilder builder )
    {
        Object value = predicate.value();
        String valueVariable = triples.addTriple( predicate.collectionProperty(), false ).value();
        builder.append( this.createRegexStringForContaining(
            valueVariable,
            this.createAndEscapeJSONString( value )
        ) );
    }

    private void processMatchesPredicate( final MatchesPredicate predicate, StringBuilder builder )
    {
        String valueVariable = triples.addTriple( predicate.property(), false ).value();
        builder.append( format( "regex(%s,\"%s\")", valueVariable, predicate.regexp() ) );
    }

    private void processComparisonPredicate( final Predicate<Composite> predicate,
                                             boolean allowInline,
                                             StringBuilder builder
    )
    {
        if( predicate instanceof ComparisonPredicate )
        {
            ComparisonPredicate<?> comparisonPredicate = (ComparisonPredicate<?>) predicate;
            Triples.Triple triple = triples.addTriple( comparisonPredicate.property(), false );

            // Don't use FILTER for equals-comparison. Do direct match instead
            if( predicate instanceof EqPredicate && allowInline )
            {
                triple.setValue( "\"" + toString( comparisonPredicate.value() ) + "\"" );
            }
            else
            {
                String valueVariable = triple.value();
                builder.append( String.format(
                    "(%s %s \"%s\")",
                    valueVariable,
                    getOperator( comparisonPredicate.getClass() ),
                    toString( comparisonPredicate.value() ) ) );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Operator " + predicate.getClass()
                .getName() + " is not supported" );
        }
    }

    private void processNullPredicate( final PropertyNullPredicate<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTriple( predicate.property(), true ).value();
        builder.append( format( "(! bound(%s))", value ) );
    }

    private void processNotNullPredicate( final PropertyNotNullPredicate<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTriple( predicate.property(), true ).value();
        builder.append( format( "(bound(%s))", value ) );
    }

    private void processNullPredicate( final AssociationNullPredicate<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTripleAssociation( predicate.association(), true ).value();
        builder.append( format( "(! bound(%s))", value ) );
    }

    private void processNotNullPredicate( final AssociationNotNullPredicate<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTripleAssociation( predicate.association(), true ).value();
        builder.append( format( "(bound(%s))", value ) );
    }

    private void processManyAssociationContainsPredicate( ManyAssociationContainsPredicate<?> predicate,
                                                          boolean allowInline, StringBuilder builder
    )
    {
        Triples.Triple triple = triples.addTripleManyAssociation( predicate.manyAssociation(), false );

        if( allowInline )
        {
            triple.setValue( "<" + toString( predicate.value() ) + ">" );
        }
        else
        {
            String valueVariable = triple.value();
            builder.append( String.format( "(%s %s <%s>)", valueVariable, "=", toString( predicate.value() ) ) );
        }
    }

    private void processOrderBy( List<OrderBy> orderBySegments, StringBuilder builder )
    {
        if( orderBySegments != null && orderBySegments.size() > 0 )
        {
            for( OrderBy orderBySegment : orderBySegments )
            {
                processOrderBy( builder, orderBySegment );
            }
        }
    }

    private void processOrderBy( StringBuilder builder, OrderBy orderBySegment )
    {
        if( orderBySegment != null )
        {
            final String valueVariable = triples.addTriple( orderBySegment.property(), false ).value();
            if( orderBySegment.order() == OrderBy.Order.ASCENDING )
            {
                builder.append( format( "ASC(%s)", valueVariable ) );
            }
            else
            {
                builder.append( format( "DESC(%s)", valueVariable ) );
            }
        }
    }

    private String getOperator( final Class<? extends ComparisonPredicate> predicateClass )
    {
        String operator = OPERATORS.get( predicateClass );
        if( operator == null )
        {
            throw new UnsupportedOperationException( "Predicate [" + predicateClass.getName() + "] is not supported" );
        }
        return operator;
    }

    private String toString( Object value )
    {
        if( value == null )
        {
            return null;
        }
        if( value instanceof EntityComposite )
        {
            return "urn:qi4j:entity:" + value.toString();
        }
        if( value instanceof Variable )
        {
            Object realValue = variables.get( ( (Variable) value ).variableName() );

            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + ( (Variable) value ).variableName() + " not bound" );
            }

            return toString( realValue );
        }
        else
        {
            return value.toString();
        }
    }
}
