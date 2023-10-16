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
package org.qi4j.library.sql.generator.implementation.grammar.query;

import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.common.SetQuantifier;
import org.qi4j.library.sql.generator.grammar.query.SelectColumnClause;
import org.qi4j.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public abstract class SelectColumnClauseImpl<SelectType extends SelectColumnClause> extends
        SQLSyntaxElementBase<SelectColumnClause, SelectType>
    implements SelectColumnClause
{

    private final SetQuantifier _setQuantifier;

    protected SelectColumnClauseImpl(SQLProcessorAggregator processor, Class<? extends SelectType> type,
                                     SetQuantifier quantifier )
    {
        super( processor, type );
        Objects.requireNonNull( quantifier, "set quantifier" );
        this._setQuantifier = quantifier;
    }

    public SetQuantifier getSetQuantifier()
    {
        return this._setQuantifier;
    }

    @Override
    protected boolean doesEqual( SelectType another )
    {
        return this._setQuantifier.equals( another.getSetQuantifier() );
    }
}