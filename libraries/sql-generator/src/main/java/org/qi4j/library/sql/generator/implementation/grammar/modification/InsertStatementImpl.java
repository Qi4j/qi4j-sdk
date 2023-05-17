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
package org.qi4j.library.sql.generator.implementation.grammar.modification;

import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.common.TableNameDirect;
import org.qi4j.library.sql.generator.grammar.modification.ColumnSource;
import org.qi4j.library.sql.generator.grammar.modification.InsertStatement;
import org.qi4j.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class InsertStatementImpl extends SQLSyntaxElementBase<InsertStatement, InsertStatement>
    implements InsertStatement
{

    private final TableNameDirect _tableName;
    private final ColumnSource _columnSource;

    public InsertStatementImpl(SQLProcessorAggregator processor, TableNameDirect tableName, ColumnSource columnSource )
    {
        this( processor, InsertStatement.class, tableName, columnSource );
    }

    protected InsertStatementImpl( SQLProcessorAggregator processor, Class<? extends InsertStatement> expressionClass,
                                   TableNameDirect tableName, ColumnSource columnSource )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( tableName, "tableName" );
        Objects.requireNonNull( columnSource, "column source" );
        this._tableName = tableName;
        this._columnSource = columnSource;
    }

    public TableNameDirect getTableName()
    {
        return this._tableName;
    }

    public ColumnSource getColumnSource()
    {
        return this._columnSource;
    }

    @Override
    protected boolean doesEqual( InsertStatement another )
    {
        return this._tableName.equals( another.getTableName() )
               && this._columnSource.equals( another.getColumnSource() );
    }
}
