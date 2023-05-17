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
package org.qi4j.library.sql.generator.implementation.grammar.booleans;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.booleans.BooleanExpression;
import org.qi4j.library.sql.generator.grammar.booleans.BooleanTest;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class BooleanTestImpl extends ComposedBooleanExpressionImpl<BooleanTest>
    implements BooleanTest
{

    private final BooleanExpression _booleanExpression;
    private final TestType _testType;
    private final TruthValue _truthValue;

    public BooleanTestImpl(SQLProcessorAggregator processor, BooleanExpression expression, TestType testType,
                           TruthValue truthValue )
    {
        this( processor, BooleanTest.class, expression, testType, truthValue );
    }

    protected BooleanTestImpl( SQLProcessorAggregator processor, Class<? extends BooleanTest> expressionClass,
                               BooleanExpression expression, TestType testType, TruthValue truthValue )
    {
        super( processor, expressionClass );

        Objects.requireNonNull( expression, "expression" );

        if( BooleanUtils.isEmpty( expression ) )
        {
            throw new IllegalArgumentException( "Boolean test must be on something." );
        }

        this._booleanExpression = expression;
        this._testType = testType;
        this._truthValue = truthValue;
    }

    public BooleanExpression getBooleanExpression()
    {
        return this._booleanExpression;
    }

    public TestType getTestType()
    {
        return this._testType;
    }

    public TruthValue getTruthValue()
    {
        return this._truthValue;
    }

    public Iterator<BooleanExpression> iterator()
    {
        return Arrays.asList( this._booleanExpression ).iterator();
    }
}
