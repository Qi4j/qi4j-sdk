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

import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * This test is created in response to QI-359
 */
public class ConstructorInjectionOfThisTest
{

    @Test
    public void givenMixinWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.values( Does.class ).withMixins( DoesMixin.class )
        );
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    @Test
    public void givenConcernWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.values( Does.class ).withMixins( NoopMixin.class ).withConcerns( DoesConcern.class )
        );
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    @Test
    public void givenSideEffectWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.values( Does.class ).withMixins( NoopMixin.class ).withSideEffects( DoesSideEffect.class )
        );
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    public static class DoesMixin
        implements Does
    {
        private DoesPrivateFragment doesPrivateFragment;

        public DoesMixin( @This DoesPrivateFragment doesPrivateFragment )
        {
            if( doesPrivateFragment == null )
            {
                throw new NullPointerException();
            }
            this.doesPrivateFragment = doesPrivateFragment;
        }

        @Override
        public void doSomething()
        {
            assertThat( doesPrivateFragment.someState().get(), is( false ) );
        }
    }

    public static class DoesConcern
        extends ConcernOf<Does>
        implements Does
    {

        public DoesConcern( @This Does work )
        {
            if( work == null )
            {
                throw new NullPointerException();
            }
            System.out.print( "Niclas " );
        }

        @Override
        public void doSomething()
        {
            next.doSomething();
        }
    }

    public static class DoesSideEffect
        extends SideEffectOf<Does>
        implements Does
    {

        public DoesSideEffect( @This Does work )
        {
            if( work == null )
            {
                throw new NullPointerException();
            }
            System.out.print( "Niclas " );
        }

        @Override
        public void doSomething()
        {
            this.result.doSomething();
        }
    }

    public interface DoesPrivateFragment
    {
        @UseDefaults
        Property<Boolean> someState();
    }

    public interface Does
    {
        void doSomething();
    }
}
