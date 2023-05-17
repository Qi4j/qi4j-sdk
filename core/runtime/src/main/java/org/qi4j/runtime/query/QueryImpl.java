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
package org.qi4j.runtime.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.QuerySource;

/**
 * Default implementation of {@link Query}.
 */
class QueryImpl<T>
    implements Query<T>
{
    /**
     * Type of queried entities.
     */
    private final Class<T> resultType;
    /**
     * Where clause.
     */
    private final Predicate<Composite> whereClause;
    private QuerySource querySource;
    /**
     * Order by clause segments.
     */
    private List<OrderBy> orderBySegments;
    /**
     * First result to be returned.
     */
    private Integer firstResult;
    /**
     * Maximum number of results to be returned.
     */
    private Integer maxResults;
    /**
     * Mapping between variable name and variable values.
     */
    private Map<String, Object> variables;

    /**
     * Constructor.
     *
     * @param resultType  type of queried entities; cannot be null
     * @param whereClause where clause
     */
    QueryImpl( final Class<T> resultType,
               final Predicate<Composite> whereClause,
               final QuerySource querySource
             )
    {
        this.resultType = resultType;
        this.whereClause = whereClause;
        this.querySource = querySource;
    }

    /**
     * @see Query#orderBy(OrderBy[])
     */
    @Override
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = Arrays.asList( segments );
        return this;
    }

    /**
     * @see Query#orderBy(Property, OrderBy.Order)
     */
    @Override
    public Query<T> orderBy( Property<?> property, OrderBy.Order order )
    {
        if( orderBySegments == null )
        {
            orderBySegments = new ArrayList<>();
        }
        orderBySegments.add( new OrderBy( QueryExpressions.property( property ), order ) );
        return this;
    }

    /**
     * @see Query#orderBy(Property)
     */
    @Override
    public Query<T> orderBy( Property<?> property )
    {
        orderBy( property, OrderBy.Order.ASCENDING );
        return this;
    }

    /**
     * @see Query#firstResult(int)
     */
    @Override
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see Query#maxResults(int)
     */
    @Override
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see Query#setVariable(String, Object)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public Query<T> setVariable( final String name, final Object value )
    {
        if( variables == null )
        {
            variables = new HashMap<>();
        }
        variables.put( name, value );

        return this;
    }

    /**
     * @see Query#getVariable(String)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public <V> V getVariable( final String name )
    {
        if( variables == null )
        {
            return null;
        }
        else
        {
            return (V) variables.get( name );
        }
    }

    @Override
    public Class<T> resultType()
    {
        return resultType;
    }

    @Override
    public T find()
        throws QueryExecutionException
    {
        return querySource.find( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );
    }

    @Override
    public long count()
        throws QueryExecutionException
    {
        return querySource.count( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );
    }

    @Override
    public Iterator<T> iterator()
    {
        return stream().iterator();
    }

    @Override
    public Stream<T> stream()
    {
        return querySource.stream( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );
    }

    @Override
    public String toString()
    {
        return "Query{" +
               " FROM " + querySource +
               " WHERE " + whereClause +
               ( orderBySegments != null ? " ORDER BY " + orderBySegments : "" ) +
               ( firstResult != null ? " FIRST " + firstResult : "" ) +
               ( maxResults != null ? " MAX " + maxResults : "" ) +
               " EXPECT " + resultType +
               ( variables != null ? " WITH VARIABLES " + variables : "" ) +
               '}';
    }
}
