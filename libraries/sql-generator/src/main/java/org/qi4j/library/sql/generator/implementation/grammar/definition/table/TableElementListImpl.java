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
package org.qi4j.library.sql.generator.implementation.grammar.definition.table;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.definition.table.TableContentsSource;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElement;
import org.qi4j.library.sql.generator.grammar.definition.table.TableElementList;
import org.qi4j.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableElementListImpl extends SQLSyntaxElementBase<TableContentsSource, TableElementList>
    implements TableElementList
{

    private final List<TableElement> _elements;

    public TableElementListImpl(SQLProcessorAggregator processor, List<TableElement> elements )
    {
        this( processor, TableElementList.class, elements );
    }

    protected TableElementListImpl( SQLProcessorAggregator processor,
                                    Class<? extends TableElementList> realImplementingType, List<TableElement> elements )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( elements, "Table elements" );

        this._elements = Collections.unmodifiableList( elements );
    }

    @Override
    protected boolean doesEqual( TableElementList another )
    {
        return this._elements.equals( another.getElementList() );
    }

    public List<TableElement> getElementList()
    {
        return this._elements;
    }
}
