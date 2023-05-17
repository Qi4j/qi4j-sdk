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

import java.lang.annotation.Retention;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.SingletonAssembler;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test the @Invocation annotation
 */
public class InvocationInjectionTest
{
    @Test
    public void whenInvocationInjectionWithMethodWhenInjectedThenInjectMethod()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler(module -> module.transients( MyComposite.class ) );

        MyComposite composite = assembly.module().newTransient( MyComposite.class );

        composite.doStuff();
        composite.doStuff();
        composite.doStuff2();
        composite.doStuff3();
    }

    @Mixins( MyMixin.class )
    @Concerns( MyConcern.class )
// START SNIPPET: declaration
    public interface MyComposite
        extends TransientComposite
    {
        @Foo( "1" )
        void doStuff();

        // END SNIPPET: declaration
        void doStuff2();

        @Foo( "X" )
        void doStuff3();
    }

    // START SNIPPET: use1
    public abstract static class MyConcern
        extends ConcernOf<MyComposite>
        implements MyComposite
    {
        @Invocation
        Foo foo;
        // END SNIPPET: use1
        @Invocation
        Method method;

        @Invocation
        AnnotatedElement ae;

        public void doStuff()
        {
            assertThat( "interface has been injected", foo.value(), equalTo( "1" ) );
            assertThat( "annotations have been injected", ae.getAnnotation( Foo.class ).value(), equalTo( "1" ) );
            assertThat( "Method has been injected", method.getName(), equalTo( "doStuff" ) );
            next.doStuff();
        }

        public void doStuff2()
        {
            assertThat( "mixin has been injected", foo.value(), equalTo( "2" ) );
            assertThat( "annotations have been injected", ae.getAnnotation( Foo.class ).value(), equalTo( "2" ) );
            assertThat( "Method has been injected", method.getName(), equalTo( "doStuff2" ) );
            next.doStuff2();
        }

        public void doStuff3()
        {
            assertThat( "mixin has overridden interface", foo.value(), equalTo( "3" ) );
            assertThat( "annotations have been injected", ae.getAnnotation( Foo.class ).value(), equalTo( "3" ) );
            assertThat( "Method has been injected", method.getName(), equalTo( "doStuff3" ) );
            next.doStuff3();
        }
    }

    public abstract static class MyMixin
        implements MyComposite
    {
        public void doStuff()
        {
        }

        @Foo( "2" )
        public void doStuff2()
        {
        }

        @Foo( "3" )
        public void doStuff3()
        {
        }
    }

    // START SNIPPET: annotation
    @Retention( RUNTIME )
    @interface Foo
    {
        String value();
    }
// END SNIPPET: annotation
}
