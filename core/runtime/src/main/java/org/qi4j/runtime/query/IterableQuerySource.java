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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.QuerySource;

import static java.util.stream.Collectors.toList;
import static org.qi4j.api.util.Classes.instanceOf;

/**
 * JAVADOC
 */
public class IterableQuerySource
    implements QuerySource
{
    private final Iterable iterable;

    /**
     * Constructor.
     *
     * @param iterable iterable
     */
    @SuppressWarnings( "raw" )
    IterableQuerySource( final Iterable iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public <T> T find( Class<T> resultType,
                       Predicate<Composite> whereClause,
                       List<OrderBy> orderBySegments,
                       Integer firstResult,
                       Integer maxResults,
                       Map<String, Object> variables
    )
    {
        return stream( resultType, whereClause, orderBySegments, firstResult, maxResults, variables )
            .findFirst().orElse( null );
    }

    @Override
    public <T> long count( Class<T> resultType,
                           Predicate<Composite> whereClause,
                           List<OrderBy> orderBySegments,
                           Integer firstResult,
                           Integer maxResults,
                           Map<String, Object> variables
    )
    {
        return list( resultType, whereClause, orderBySegments, firstResult, maxResults, variables ).size();
    }

    @Override
    public <T> Stream<T> stream( Class<T> resultType,
                                     Predicate<Composite> whereClause,
                                     List<OrderBy> orderBySegments,
                                     Integer firstResult,
                                     Integer maxResults,
                                     Map<String, Object> variables
    )
    {
        return list( resultType, whereClause, orderBySegments, firstResult, maxResults, variables ).stream();
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    private <T> List<T> list( Class<T> resultType,
                              Predicate<Composite> whereClause,
                              List<OrderBy> orderBySegments,
                              Integer firstResult,
                              Integer maxResults,
                              Map<String, Object> variables
    )
    {
        // Ensure it's a list first
        List<T> list = filter( resultType, whereClause );

        // Order list
        if( orderBySegments != null )
        {
            // Sort it
            list.sort( new OrderByComparator( orderBySegments ) );
        }

        // Cut results
        if( firstResult != null )
        {
            if( firstResult > list.size() )
            {
                return Collections.emptyList();
            }

            int toIdx;
            if( maxResults != null )
            {
                toIdx = Math.min( firstResult + maxResults, list.size() );
            }
            else
            {
                toIdx = list.size();
            }

            list = list.subList( firstResult, toIdx );
        }
        else
        {
            int toIdx;
            if( maxResults != null )
            {
                toIdx = Math.min( maxResults, list.size() );
            }
            else
            {
                toIdx = list.size();
            }

            list = list.subList( 0, toIdx );
        }

        return list;
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    private <T> List<T> filter( Class<T> resultType, Predicate whereClause )
    {
        Stream stream = StreamSupport.stream( iterable.spliterator(), false );
        if( whereClause == null )
        {
            return List.class.cast( stream.filter( resultType::isInstance )
                                          .collect( toList() ) );
        }
        else
        {
            return List.class.cast( stream.filter( instanceOf( resultType ).and( whereClause ) )
                                          .collect( toList() ) );
        }
    }

    @Override
    public String toString()
    {
        return "IterableQuerySource{" + iterable + '}';
    }

    private static class OrderByComparator<T extends Composite>
        implements Comparator<T>
    {

        private final Iterable<OrderBy> orderBySegments;

        private OrderByComparator( Iterable<OrderBy> orderBySegments )
        {
            this.orderBySegments = orderBySegments;
        }

        @Override
        @SuppressWarnings( {"raw", "unchecked"} )
        public int compare( T o1, T o2 )
        {
            for( OrderBy orderBySegment : orderBySegments )
            {
                try
                {
                    final Property prop1 = orderBySegment.property().apply( o1 );
                    final Property prop2 = orderBySegment.property().apply( o2 );
                    if( prop1 == null || prop2 == null )
                    {
                        if( prop1 == null && prop2 == null )
                        {
                            return 0;
                        }
                        else if( prop1 != null )
                        {
                            return 1;
                        }
                        return -1;
                    }
                    final Object value1 = prop1.get();
                    final Object value2 = prop2.get();
                    if( value1 == null || value2 == null )
                    {
                        if( value1 == null && value2 == null )
                        {
                            return 0;
                        }
                        else if( value1 != null )
                        {
                            return 1;
                        }
                        return -1;
                    }
                    if( value1 instanceof Comparable )
                    {
                        int result = ( (Comparable) value1 ).compareTo( value2 );
                        if( result != 0 )
                        {
                            if( orderBySegment.order() == OrderBy.Order.ASCENDING )
                            {
                                return result;
                            }
                            else
                            {
                                return -result;
                            }
                        }
                    }
                }
                catch( Exception e )
                {
                    return 0;
                }
            }

            return 0;
        }
    }
}
