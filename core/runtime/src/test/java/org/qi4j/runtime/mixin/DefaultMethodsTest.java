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
 */
package org.qi4j.runtime.mixin;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.qi4j.test.util.Assume;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Initial tests for interface default methods support.
 */
public class DefaultMethodsTest extends AbstractQi4jTest
{
    @BeforeAll
    public static void assumeJavaVersionIs8() { Assume.assumeJavaVersion( 8 ); }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( Hello.class );
        module.transients( Hello.class ).withMixins( SpeakMixin.class );
    }

    @Test
    public void givenInterfaceWithDefaultMethodWhenCallingExpectSuccess()
    {
        ValueBuilder<Hello> builder = valueBuilderFactory.newValueBuilder( Hello.class );
        Hello prototype = builder.prototype();
        Property<String> phrase = prototype.phrase();
        phrase.set( "Hello" );
        Hello hello = builder.newInstance();
        assertThat( hello.speak(), equalTo( "Hello" ) );
        assertThat( Hello.noise(), equalTo( "Good Bye" ) );
    }

    @Test
    public void givenInterfaceWithDefaultMethodAndMixinImplementationWhenCallingExpectMixinValueReturned()
    {
        TransientBuilder<Hello> builder = transientBuilderFactory.newTransientBuilder( Hello.class );
        Hello prototype = builder.prototype();
        Property<String> phrase = prototype.phrase();
        phrase.set( "Hello" );
        Hello hello = builder.newInstance();
        assertThat( hello.speak(), equalTo( "Hello, Mixin!" ) );
        assertThat( Hello.noise(), equalTo( "Good Bye" ) );
    }

    public interface Hello
    {
        Property<String> phrase();

        default String speak()
        {
            return phrase().get();
        }

        static String noise()
        {
            return "Good Bye";
        }
    }

    public static abstract class SpeakMixin
        implements Hello
    {
        @Override
        public String speak()
        {
            return phrase().get() + ", Mixin!";
        }
    }
}
