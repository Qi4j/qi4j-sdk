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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByExpression;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableReferenceByExpressionImpl extends TableReferencePrimaryImpl<TableReferenceByExpression>
    implements TableReferenceByExpression
{
    private final QueryExpression _expression;

    public TableReferenceByExpressionImpl( SQLProcessorAggregator processor, QueryExpression expression,
                                           TableAlias alias )
    {
        this( processor, TableReferenceByExpression.class, expression, alias );
    }

    protected TableReferenceByExpressionImpl( SQLProcessorAggregator processor,
                                              Class<? extends TableReferenceByExpression> implClass, QueryExpression expression, TableAlias alias )
    {
        super( processor, implClass, alias );
        Objects.requireNonNull( expression, "collection expression" );
        this._expression = expression;
    }

    public QueryExpression getQuery()
    {
        return this._expression;
    }
}
