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

package org.qi4j.runtime.defaults;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * JAVADOC
 */
public class UseDefaultsTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.transients( TestComposite.class );
        module.forMixin( TestComposite.class ).declareDefaults().assemblyString().set( "habba" );

        module.defaultServices();
    }

    @Test
    public void givenPropertyWithUseDefaultsWhenInstantiatedThenPropertiesAreDefaulted()
    {
        TransientBuilder<TestComposite> builder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
        TestComposite testComposite = builder.newInstance();

        assertThat( "nullInt is null", testComposite.nullInt().get(), nullValue() );
        assertThat( "zeroInt is zero", testComposite.defaultInt().get(), equalTo( 0 ) );
        assertThat( "nullString is null", testComposite.nullString().get(), nullValue() );
        assertThat( "defaultString is empty string", testComposite.defaultString().get(), equalTo( "" ) );
        assertThat( "assemblyString is set string", testComposite.assemblyString().get(), equalTo( "habba" ) );

        assertThat( "nullPrimitiveArray is null", testComposite.nullPrimitiveArray().get(), nullValue() );
        assertThat( "emptyPrimitiveArray is empty",
                    Arrays.equals( testComposite.emptyPrimitiveArray().get(), new int[ 0 ] ), is( true ) );
        assertThat( "nullArray is null", testComposite.nullArray().get(), nullValue() );
        assertThat( "emptyArray is empty array",
                    Arrays.equals( testComposite.emptyArray().get(), new Integer[ 0 ] ), is( true ) );
    }

    @Test
    public void givenPropertyWithValuedUseDefaultsWhenInstantiatedExpectCorrectDefaultValues()
    {
        TransientBuilder<TestComposite> builder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
        TestComposite testComposite = builder.newInstance();

        assertThat( testComposite.initializedStringDefault().get(), equalTo( "abc" ) );
        assertThat( testComposite.initializedIntegerDefaultValue().get(), equalTo( 123 ) );
        assertThat( testComposite.initializedFloatDefaultValue().get(), equalTo( 123.45f ) );
        List<String> expectedList = Collections.singletonList( "abcde" );
        assertThat( testComposite.initializedStringListDefultString().get(), equalTo( expectedList ) );
        Map<String, Integer> expectedMap = Collections.singletonMap( "abcd", 345 );
        assertThat( testComposite.initializedMapDefaultValue().get(), equalTo( expectedMap ) );

        assertThat( "initializedPrimitiveArray is set",
                    Arrays.equals( testComposite.initializedPrimitiveArray().get(), new int[]{ 23, 42 } ),
                    is( true )
        );
        assertThat( "initializedArray is set",
                    Arrays.equals( testComposite.initializedArray().get(), new Integer[]{ 23, 42 } ),
                    is( true )
        );
    }

    interface TestComposite
    {
        @Optional
        Property<Integer> nullInt();

        @Optional
        @UseDefaults
        Property<Integer> defaultInt();

        @Optional
        Property<String> nullString();

        @Optional
        @UseDefaults
        Property<String> defaultString();

        Property<String> assemblyString();

        @UseDefaults( "abc" )
        Property<String> initializedStringDefault();

        @UseDefaults( "123" )
        Property<Integer> initializedIntegerDefaultValue();

        @UseDefaults( "123.45" )
        Property<Float> initializedFloatDefaultValue();

        @UseDefaults( "[\"abcde\"]" )
        Property<List<String>> initializedStringListDefultString();

        @UseDefaults( "{\"abcd\": 345}" )
        Property<Map<String, Integer>> initializedMapDefaultValue();

        @Optional
        Property<int[]> nullPrimitiveArray();

        @UseDefaults
        Property<int[]> emptyPrimitiveArray();

        @UseDefaults( "[23, 42]" )
        Property<int[]> initializedPrimitiveArray();

        @Optional
        Property<Integer[]> nullArray();

        @UseDefaults
        Property<Integer[]> emptyArray();

        @UseDefaults( "[23, 42]" )
        Property<Integer[]> initializedArray();
    }
}
