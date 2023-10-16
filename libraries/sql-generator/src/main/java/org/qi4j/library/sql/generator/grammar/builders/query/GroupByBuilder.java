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
package org.qi4j.library.sql.generator.grammar.builders.query;

import java.util.List;
import org.qi4j.library.sql.generator.grammar.builders.AbstractBuilder;
import org.qi4j.library.sql.generator.grammar.query.GroupByClause;
import org.qi4j.library.sql.generator.grammar.query.GroupingElement;

/**
 * The builder that builds the {@code GROUP BY} clause in SQL {@code SELECT} query.
 *
 * @see GroupByClause
 * @see GroupingElement
 */
public interface GroupByBuilder
    extends AbstractBuilder<GroupByClause>
{

    /**
     * Adds grouping elements to this {@code GROUP BY} clause.
     *
     * @param elements The grouping elements for this {@code GROUP BY} clause.
     * @return This builder.
     */
    GroupByBuilder addGroupingElements( GroupingElement... elements );

    /**
     * Returns the list of added grouping elements.
     *
     * @return The list of added grouping elements.
     */
    List<GroupingElement> getGroupingElements();
}