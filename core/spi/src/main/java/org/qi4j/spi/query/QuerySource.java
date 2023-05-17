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
package org.qi4j.spi.query;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.OrderBy;

/**
 * Query Source, used in QueryBuilder SPI.
 */
public interface QuerySource
{
    <T> T find( Class<T> resultType,
                Predicate<Composite> whereClause,
                List<OrderBy> orderBySegments,
                Integer firstResult,
                Integer maxResults,
                Map<String, Object> variables
    );

    <T> long count( Class<T> resultType,
                    Predicate<Composite> whereClause,
                    List<OrderBy> orderBySegments,
                    Integer firstResult,
                    Integer maxResults,
                    Map<String, Object> variables
    );

    <T> Stream<T> stream( Class<T> resultType,
                          Predicate<Composite> whereClause,
                          List<OrderBy> orderBySegments,
                          Integer firstResult,
                          Integer maxResults,
                          Map<String, Object> variables
    );
}
