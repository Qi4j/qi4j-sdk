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

import org.qi4j.library.sql.generator.Typeable;
import org.qi4j.library.sql.generator.grammar.common.ColumnNameList;
import org.qi4j.library.sql.generator.grammar.common.ColumnNameList;

/**
 * This syntax element represents the alias for a table. Table alias may have additional list of column aliases.
 *
 *
 */
public interface TableAlias
    extends Typeable<TableAlias>
{

    /**
     * Returns an alias for a table name.
     *
     * @return The alias for the table name.
     */
    String getTableAlias();

    /**
     * Returns aliases for columns in the original table.
     *
     * @return Aliases for columns in the original table. May be {@code null}.
     */
    ColumnNameList getColumnAliases();
}
