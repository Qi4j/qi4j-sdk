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

package org.qi4j.runtime.injection;

import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the @State annotation
 */
public class StateInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( StateInjectionTest.PropertyFieldInjectionComposite.class );
    }

    /**
     * Tests that a mixin is injected into member variables annotated with {@link @PropertyField}.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void mixinIsInjectedForMemberVariablesAnnotatedWithPropertyField()
        throws Exception
    {
        TransientBuilder<PropertyFieldInjectionComposite> pficBuilder =
            transientBuilderFactory.newTransientBuilder( StateInjectionTest.PropertyFieldInjectionComposite.class );
        pficBuilder.prototype().testField().set( "X" );
        PropertyFieldInjectionComposite pfic = pficBuilder.newInstance();
        assertThat( "Test field", pfic.testField().get(), is( equalTo( "X" ) ) );
        assertThat( "Named fieldX", pfic.namedField().get(), is( equalTo( "X" ) ) );
        assertThat( "State", pfic.getState()
                                 .propertyFor( PropertyFieldInjectionComposite.class.getMethod( "testField" ) )
                                 .get(), is( equalTo( "X" ) ) );
    }

    @Mixins( PropertyFieldInjectionMixin.class )
    public interface PropertyFieldInjectionComposite
        extends TransientComposite
    {
        @Optional
        Property<String> testField();

        @Optional
        Property<String> namedField();

        StateHolder getState();
    }

    public abstract static class PropertyFieldInjectionMixin
        implements PropertyFieldInjectionComposite
    {
        @State
        Property<String> testField;

        @State( "testField" )
        Property<String> namedField;

        @State
        StateHolder state;

        public StateHolder getState()
        {
            return state;
        }

        public Property<String> testField()
        {
            return testField;
        }

        public Property<String> namedField()
        {
            return namedField;
        }
    }
}
