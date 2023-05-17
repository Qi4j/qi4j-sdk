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

package org.qi4j.runtime.mixin;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of Initializable interface
 */
public class InitializableTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
    {
        module.objects( TestObject.class );
        module.transients( TestComposite.class, NoMethod.class );
        module.values( TestComposite.class );
        module.services( TestComposite.class );
        module.entities( TestComposite.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenTransientWithInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        TestComposite instance = transientBuilderFactory.newTransient( TestComposite.class );
        assertThat( "mixin has been initialized", instance.ok(), equalTo( true ) );
    }

    @Test
    public void givenValueWithInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        TestComposite instance = valueBuilderFactory.newValue( TestComposite.class );
        assertThat( "mixin has been initialized", instance.ok(), equalTo( true ) );
    }

    @Test
    public void givenServiceWithInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        TestComposite instance = serviceFinder.findService( TestComposite.class ).get();
        assertThat( "mixin has been initialized", instance.ok(), equalTo( true ) );
    }

    @Test
    public void givenEntityWithInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestComposite instance = uow.newEntity( TestComposite.class );
            assertThat( "mixin has been initialized", instance.ok(), equalTo( true ) );
        }
    }

    @Test
    public void givenObjectImplementingInitializableWhenInstantiatedThenInvokeInitialize()
    {
        TestObject instance = objectFactory.newObject( TestObject.class );
        assertThat( "object has been initialized", instance.ok(), equalTo( true ) );
    }

    // TODO: (niclas) This is part of the whole lifecycle mess, that needs to be worked out once and for all.
    @Disabled( "Mixin of types with no method are not scrutinized for Initializable implementation" )
    @Test
    public void givenTypeWithNoMethodsAndInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        NoMethod instance = transientBuilderFactory.newTransient( NoMethod.class );
        assertThat( "mixin has not been initialized", noMethodMixinOk, equalTo( true ) );
    }

    @Mixins( TestMixin.class )
    public interface TestComposite extends ComposedInitializable
    {
        boolean ok();
    }

    public abstract static class TestMixin implements TestComposite, Initializable
    {
        boolean ok = false;

        @This
        ComposedInitializable composedInitializable;

        @This
        PrivateInitializable privateInitializable;

        @Override
        public void initialize()
        {
            ok = true;
        }

        @Override
        public boolean ok()
        {
            return ok && composedInitializable.composedOk() && privateInitializable.ok();
        }
    }

    @Mixins( ComposedInitializableMixin.class )
    public interface ComposedInitializable
    {
        boolean composedOk();
    }

    public static class ComposedInitializableMixin implements ComposedInitializable, Initializable
    {
        boolean ok = false;

        @Override
        public void initialize()
        {
            ok = true;
        }

        @Override
        public boolean composedOk()
        {
            return ok;
        }
    }

    @Mixins( PrivateInitializableMixin.class )
    public interface PrivateInitializable
    {
        boolean ok();
    }

    public static class PrivateInitializableMixin implements PrivateInitializable, Initializable
    {
        @This
        NestedInitializable nestedInitializable;

        boolean ok = false;

        @Override
        public void initialize()
        {
            ok = true;
        }

        @Override
        public boolean ok()
        {
            return ok && nestedInitializable.nestedOk();
        }
    }

    @Mixins( NestedInitializableMixin.class )
    public interface NestedInitializable
    {
        boolean nestedOk();
    }

    public static class NestedInitializableMixin implements NestedInitializable, Initializable
    {
        boolean ok = false;

        @Override
        public void initialize()
        {
            ok = true;
        }

        @Override
        public boolean nestedOk()
        {
            return ok;
        }
    }

    static boolean noMethodMixinOk;

    @BeforeEach
    public void resetNoMethodMixinStaticState()
    {
        noMethodMixinOk = false;
    }

    @Mixins( NoMethodMixin.class )
    public interface NoMethod {}

    public static class NoMethodMixin implements Initializable
    {
        @Override
        public void initialize()
        {
            noMethodMixinOk = true;
        }
    }

    public static class TestObject implements Initializable
    {
        boolean ok = false;

        @Override
        public void initialize()
        {
            ok = true;
        }

        public boolean ok()
        {
            return ok;
        }
    }
}
