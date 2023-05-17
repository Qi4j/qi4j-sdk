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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the @This annotation
 */
public class ThisInjectionTest
        extends AbstractQi4jTest
{
    public static boolean sideEffectInjected;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    /**
     * Tests the injected object for {@link @org.qi4j.composite.scope.This} annotation.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void givenCompositeWithThisInjectionsWhenInstantiatedThenCompositeIsInjected()
        throws Exception
    {
        TestComposite testComposite = transientBuilderFactory.newTransient( TestComposite.class );

        assertThat( "Injection worked", testComposite.isInjected() && sideEffectInjected, is( equalTo( true ) ) );
    }

    public interface TestType
    {
        boolean isInjected();

        String test();
    }

    public interface TestPrivate
    {
        @UseDefaults
        Property<Boolean> testPrivate();
    }

    @SideEffects( TestSideEffect.class )
    @Concerns( TestConcern.class )
    @Mixins( TestMixin.class )
    public interface TestComposite
        extends TransientComposite, TestType
    {
    }

    public static class TestMixin
        implements TestType
    {
        @This
        TestType test;

        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            return test != null && !testPrivate.testPrivate().get();
        }

        public String test()
        {
            return "Foo";
        }
    }

    public static abstract class TestConcern
        extends ConcernOf<TestType>
        implements TestType
    {
        @This
        TestType test;

        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            return test != null && test.test().equals( "Foo" ) &&
                   !testPrivate.testPrivate().get() &&
                   next.isInjected();
        }
    }

    public static abstract class TestSideEffect
        extends SideEffectOf<TestType>
        implements TestType
    {
        @This
        TestType test;
        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            sideEffectInjected = test != null && !testPrivate.testPrivate().get();

            return false;
        }
    }
}
