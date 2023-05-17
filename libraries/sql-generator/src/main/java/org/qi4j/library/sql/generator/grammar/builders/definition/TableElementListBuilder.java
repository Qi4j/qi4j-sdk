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
package org.qi4j.library.sql.generator.grammar.builders.definition;

import java.util.List;
import org.qi4j.library.sql.generator.grammar.builders.AbstractBuilder;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElement;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElementList;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElement;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElementList;

/**
 * This is the builder for table element list used in {@code CREATE TABLE} statements.
 *
 */
public interface TableElementListBuilder
    extends AbstractBuilder<TableElementList>
{

    /**
     * Adds the table element to this list.
     *
     * @param element The table element to add to this list.
     * @return This builder.
     */
    TableElementListBuilder addTableElement( TableElement element );

    /**
     * Returns all the elements that this builder has.
     *
     * @return All the elements that this builder has.
     */
    List<TableElement> getTableElements();
}
