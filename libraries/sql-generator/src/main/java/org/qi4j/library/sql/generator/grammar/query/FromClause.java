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
package org.qi4j.library.sql.generator.grammar.query;

import java.util.List;
import org.qi4j.library.sql.generator.Typeable;
import org.qi4j.library.sql.generator.grammar.query.joins.JoinedTable;
import org.qi4j.library.sql.generator.grammar.query.joins.JoinedTable;

/**
 * This syntax element represents the {@code FROM} clause in {@code SELECT} statement.
 *
 *
 */
public interface FromClause
    extends Typeable<FromClause>
{
    /**
     * Gets all the table references. Each table reference may be a simple reference to table name, sub-query, or a
     * joined table.
     *
     * @return The table references for this {@code FROM} clause.
     * @see TableReference
     * @see JoinedTable
     */
    List<TableReference> getTableReferences();
}
