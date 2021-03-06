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
package org.apache.polygene.api.query;

import org.apache.polygene.api.indexing.MissingIndexingSystemException;

/**
 * This is used to create QueryBuilders.
 *
 * @see QueryBuilder
 */
public interface QueryBuilderFactory
{
    /**
     * Create a new QueryBuilder.
     *
     * @param <T> Result type
     * @param resultType the type of the result that you want
     *
     * @return a QueryBuilder
     *
     * @throws MissingIndexingSystemException if there is no EntityFinder service available
     */
    <T> QueryBuilder<T> newQueryBuilder( Class<T> resultType )
        throws MissingIndexingSystemException;
}