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
import org.qi4j.library.sql.generator.grammar.common.ValueExpression;
import org.qi4j.library.sql.generator.grammar.common.ValueExpression;

/**
 * This syntax element represents the sort specification used in {@code ORDER BY} clause.
 *
 *
 */
public interface SortSpecification
    extends Typeable<SortSpecification>
{
    /**
     * Returns the {@link Ordering} of this sort specification.
     *
     * @return The {@link Ordering} of this sort specification.
     */
    Ordering getOrderingSpecification();

    /**
     * The value expression, which may be a column reference, for example.
     *
     * @return The value expression of this sort specification.
     */
    ValueExpression getValueExpression();
}
