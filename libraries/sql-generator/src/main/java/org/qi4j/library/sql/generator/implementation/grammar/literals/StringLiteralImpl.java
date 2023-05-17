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
package org.qi4j.library.sql.generator.implementation.grammar.literals;

import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.literals.StringLiteral;
import org.qi4j.library.sql.generator.implementation.grammar.common.NonBooleanExpressionImpl;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class StringLiteralImpl extends NonBooleanExpressionImpl<StringLiteral>
    implements StringLiteral
{

    private final String _literal;

    public StringLiteralImpl(SQLProcessorAggregator processor, String literal )
    {
        this( processor, StringLiteral.class, literal );
    }

    protected StringLiteralImpl( SQLProcessorAggregator processor, Class<? extends StringLiteral> implClass,
                                 String literal )
    {
        super( processor, implClass );
        Objects.requireNonNull( literal, "literal" );

        this._literal = literal;
    }

    public String getString()
    {
        return this._literal;
    }

    @Override
    protected boolean doesEqual( StringLiteral another )
    {
        return this._literal.equals( another.getString() );
    }
}
