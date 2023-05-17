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
package org.qi4j.runtime.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;
import org.qi4j.runtime.property.PropertyEqualityTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert that Value equals/hashcode methods combine ValueDescriptor and ValueState.
 */
public class ValueEqualityTest
    extends AbstractQi4jTest
{

    //
    // --------------------------------------:: Types under test ::-----------------------------------------------------
    //
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( PropertyEqualityTest.PrimitivesValue.class, PropertyEqualityTest.Some.class, PropertyEqualityTest.AnotherSome.class, PropertyEqualityTest.Other.class );
    }

    //
    // -------------------------------:: ValueDescriptor equality tests ::----------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeWhenTestingValueDescriptorEqualityExpectEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue(valueBuilderFactory);
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );

        PropertyEqualityTest.Some other = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        ValueDescriptor otherDescriptor = qi4j.api().valueDescriptorFor( other );

        assertThat( "ValueDescriptors equal",
                    someDescriptor,
                    equalTo( otherDescriptor ) );
        assertThat( "ValueDescriptors hashcode equal",
                    someDescriptor.hashCode(),
                    equalTo( otherDescriptor.hashCode() ) );
    }

    @Test
    public void givenValuesOfCommonTypesWhenTestingValueDescriptorEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );

        PropertyEqualityTest.PrimitivesValue primitive = PropertyEqualityTest.buildPrimitivesValue( valueBuilderFactory );
        ValueDescriptor primitiveDescriptor = qi4j.api().valueDescriptorFor( primitive );

        assertThat( "ValueDescriptors not equal",
                    someDescriptor,
                    not( equalTo( primitiveDescriptor ) ) );
        assertThat( "ValueDescriptors hashcode not equal",
                    someDescriptor.hashCode(),
                    not( equalTo( primitiveDescriptor.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesWhenTestingValueDescriptorEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );

        PropertyEqualityTest.Other other = PropertyEqualityTest.buildOtherValue( valueBuilderFactory );
        ValueDescriptor otherDescriptor = qi4j.api().valueDescriptorFor( other );

        assertThat( "ValueDescriptors not equal",
                    someDescriptor,
                    not( equalTo( otherDescriptor ) ) );
        assertThat( "ValueDescriptors hashcode not equal",
                    someDescriptor.hashCode(),
                    not( equalTo( otherDescriptor.hashCode() ) ) );
    }

    //
    // ---------------------------------:: Value State equality tests ::------------------------------------------------
    //
    @Test
    public void givenValuesOfSameTypesAndSameStateWhenTestingValueStateEqualityExpectEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = qi4j.spi().stateOf( (ValueComposite) some );

        PropertyEqualityTest.Some some2 = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        AssociationStateHolder some2State = qi4j.spi().stateOf( (ValueComposite) some2 );

        assertThat( "ValueStates equal",
                    someState,
                    equalTo( some2State ) );
        assertThat( "ValueStates hashcode equal",
                    someState.hashCode(),
                    equalTo( some2State.hashCode() ) );
    }

    @Test
    public void givenValuesOfSameTypesAndDifferentStateWhenTestingValueStateEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = qi4j.spi().stateOf( (ValueComposite) some );

        PropertyEqualityTest.Some some2 = PropertyEqualityTest.buildSomeValueWithDifferentState( valueBuilderFactory );
        AssociationStateHolder some2State = qi4j.spi().stateOf( (ValueComposite) some2 );

        assertThat( "ValueStates not equal",
                    someState,
                    not( equalTo( some2State ) ) );
        assertThat( "ValueStates hashcode not equal",
                    someState.hashCode(),
                    not( equalTo( some2State.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingValueStateEqualityExpectEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = qi4j.spi().stateOf( (ValueComposite) some );

        PropertyEqualityTest.AnotherSome anotherSome = PropertyEqualityTest.buildAnotherSomeValue( valueBuilderFactory );
        AssociationStateHolder anotherSomeState = qi4j.spi().stateOf( (ValueComposite) anotherSome );

        assertThat( "ValueStates equal",
                    someState,
                    equalTo( anotherSomeState ) );
        assertThat( "ValueStates hashcode equal",
                    someState.hashCode(),
                    equalTo( anotherSomeState.hashCode() ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingValueStateEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = qi4j.spi().stateOf( (ValueComposite) some );

        PropertyEqualityTest.AnotherSome anotherSome = PropertyEqualityTest.buildAnotherSomeValueWithDifferentState( valueBuilderFactory );
        AssociationStateHolder anotherSomeState = qi4j.spi().stateOf( (ValueComposite) anotherSome );

        assertThat( "ValueStates not equal",
                    someState,
                    not( equalTo( anotherSomeState ) ) );
        assertThat( "ValueStates hashcode not equal",
                    someState.hashCode(),
                    not( equalTo( anotherSomeState.hashCode() ) ) );
    }

    //
    // ------------------------------------:: Value equality tests ::---------------------------------------------------
    //
    @Test
    public void givenValuesOfSameTypesAndSameStateWhenTestingValueEqualityExpectEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        PropertyEqualityTest.Some some2 = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        assertThat( "Values equal",
                    some,
                    equalTo( some2 ) );
        assertThat( "Values hashcode equal",
                    some.hashCode(),
                    equalTo( some2.hashCode() ) );
    }

    @Test
    public void givenValuesOfTheSameTypeWithDifferentStateWhenTestingValueEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        PropertyEqualityTest.Some some2 = PropertyEqualityTest.buildSomeValueWithDifferentState( valueBuilderFactory );
        assertThat( "Values not equals",
                    some,
                    not( equalTo( some2 ) ) );
        assertThat( "Values hashcode not equals",
                    some.hashCode(),
                    not( equalTo( some2.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingValueEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        PropertyEqualityTest.Some anotherSome = PropertyEqualityTest.buildAnotherSomeValue( valueBuilderFactory );

        assertThat( "Values not equal",
                    some,
                    not( equalTo( anotherSome ) ) );
        assertThat( "Values hashcode not equal",
                    some.hashCode(),
                    not( equalTo( anotherSome.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingValueEqualityExpectNotEquals()
    {
        PropertyEqualityTest.Some some = PropertyEqualityTest.buildSomeValue( valueBuilderFactory );
        PropertyEqualityTest.Some anotherSome = PropertyEqualityTest.buildAnotherSomeValueWithDifferentState( valueBuilderFactory );
        assertThat( "Values not equal",
                    some,
                    not( equalTo( anotherSome ) ) );
        assertThat( "Values hashcode not equal",
                    some.hashCode(),
                    not( equalTo( anotherSome.hashCode() ) ) );
    }
}
