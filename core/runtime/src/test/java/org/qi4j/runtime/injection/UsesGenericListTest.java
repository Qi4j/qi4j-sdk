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

import java.util.ArrayList;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

/**
 * Test of generic list injection
 */
public class UsesGenericListTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestCase.class );
    }

    @Test
    public void givenMixinUsesGenericListWhenUseListThenInjectWorks()
    {
        TransientBuilder<TestCase> builder = transientBuilderFactory.newTransientBuilder( TestCase.class );

        ArrayList<String> list = new ArrayList<String>();
        list.add( "Hello" );
        list.add( "Bye" );
        builder.use( list );

        TestCase TestCase = builder.newInstance();
        TestCase.sayHello();
    }

    @Mixins( TestMixin.class )
    public interface TestCase
        extends TransientComposite
    {
        void sayHello();
    }

    public abstract static class TestMixin
        implements TestCase
    {
        @Uses
        ArrayList<String> messages;

        public void sayHello()
        {
            System.out.println( messages );
        }
    }
}
