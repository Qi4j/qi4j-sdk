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
package org.qi4j.library.sql.generator.grammar.query.joins;

import org.qi4j.library.sql.generator.grammar.query.QueryExpressionBody;
import org.qi4j.library.sql.generator.grammar.query.TableReference;

/**
 * This is common interface for joined tables.
 *
 *
 * @see CrossJoinedTable
 * @see NaturalJoinedTable
 * @see QualifiedJoinedTable
 * @see UnionJoinedTable
 */
public interface JoinedTable
    extends QueryExpressionBody, TableReference
{
    /**
     * Returns the table on the left side of the join.
     *
     * @return The table on the left side of the join.
     */
    TableReference getLeft();

    /**
     * Gets the table on the right side of the join.
     *
     * @return The table on the right side of the join.
     */
    TableReference getRight();
}