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
 * This syntax element represents the {@code CORRESPONDING BY} clause in {@code UNION}, {@code INTERSECT}, and
 * {@code EXCEPT} operations on queries.
 *
 *
 */
public interface CorrespondingSpec
    extends Typeable<CorrespondingSpec>
{
    /**
     * Gets the column name correspondence list.
     *
     * @return The column name correspondence list.
     */
    ColumnNameList getColumnList();
}
