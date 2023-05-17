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
package org.qi4j.library.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConstraintTest extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestCaseComposite.class );
    }

    @Test
    public void testContainsFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().containsString().set( "bar" );
        } );
    }

    @Test
    public void testContainsOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().containsString().set( "foo" );
        cb.prototype().containsString().set( "xxxfooyyy" );
    }

    @Test
    public void testEmailFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().email().set( "foo.com" );
        } );
    }

    @Test
    public void testEmailOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().email().set( "rickard@gmail.com" );
    }

    @Test
    public void testURLFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().url().set( "this is no url" );
        } );
    }

    @Test
    public void testURLOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().url().set( "http://qi4j.org/path?query=string#fragment" );
    }

    @Test
    public void testURIFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().uri().set( "" );
        } );
    }

    @Test
    public void testURIOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().uri().set( "http://qi4j.org/path?query=string#fragment" );
    }

    @Test
    public void testGreaterThanFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().greaterThan().set( 10 );
        } );
    }

    @Test
    public void testGreaterThanOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().greaterThan().set( 11 );
    }

    @Test
    public void testInstanceOfFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().instanceOf().set( new HashSet() );
        } );
    }

    @Test
    public void testInstanceOfOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().instanceOf().set( new ArrayList() );
    }

    @Test
    public void testLessThanFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().lessThan().set( 10 );
        } );
    }

    @Test
    public void testLessThanOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().lessThan().set( 9 );
    }

    @Test
    public void testMatchesFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().matches().set( "cba" );
        } );
    }

    @Test
    public void testMatchesOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().matches().set( "abbccc" );
    }

    @Test
    public void testMaxLengthFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().maxLength().set( "xxxxx" );
        } );
    }

    @Test
    public void testMaxLengthOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().maxLength().set( "xxx" );
    }

    @Test
    public void testMinLengthFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().minLength().set( "xx" );
        } );
    }

    @Test
    public void testMinLengthOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().minLength().set( "xxx" );
    }

    @Test
    public void testNotEmptyFail()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        assertThrows( ConstraintViolationException.class, () -> {
            cb.prototype().notEmptyString().set( "" );
        } );

        assertThrows( ConstraintViolationException.class, () -> {
            cb.prototype().notEmptyCollection().set( new ArrayList() );
        } );

        assertThrows( ConstraintViolationException.class, () -> {
            cb.prototype().notEmptyList().set( new ArrayList() );
        } );
    }

    @Test
    public void testNotEmptyOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().notEmptyString().set( "X" );
        cb.prototype().notEmptyCollection().set( Collections.singletonList( "X" ) );
        cb.prototype().notEmptyList().set( Collections.singletonList( "X" ) );
    }

    @Test
    public void testOneOfFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().oneOf().set( "Foo" );
        } );
    }

    @Test
    public void testOneOfOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().oneOf().set( "Bar" );
    }

    @Test
    public void testRangeFail()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
            cb.prototype().range().set( 101 );
        } );
    }

    @Test
    public void testRangeOk()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().range().set( 0 );
        cb.prototype().range().set( 50 );
        cb.prototype().range().set( 100 );
    }

    @Test
    public void testMethodParameters()
    {
        TransientBuilder<TestCaseComposite> cb = transientBuilderFactory.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().testParameters( 15 );
    }
}
