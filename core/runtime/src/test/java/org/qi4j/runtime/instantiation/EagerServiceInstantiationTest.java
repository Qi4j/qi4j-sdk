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

package org.qi4j.runtime.instantiation;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class EagerServiceInstantiationTest
    extends AbstractQi4jTest
{
    private TestInfo testInfo;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        testInfo = new TestInfo();
        module.services( MyService.class ).setMetaInfo( testInfo ).instantiateOnStartup();
    }

    @Test
    public void givenServiceInstantiatedOnStartUpWhenTestIsRunExpectServiceToHaveRun()
    {
        assertThat( testInfo.test, equalTo( "123" ) );
    }

    @Mixins( MyMixin.class )
    public interface MyService
        extends My, ServiceComposite
    {
    }

    public interface My
    {
        void doSomething();
    }

    public static class MyMixin
        implements My
    {
        public MyMixin( @Uses ServiceDescriptor descriptor )
        {
            descriptor.metaInfo( TestInfo.class ).test = "123";
        }

        public MyMixin()
        {
            System.out.println( "Constructor" );
        }

        public void doSomething()
        {
            System.out.println( "Execute" );
        }
    }

    public class TestInfo
    {
        private String test = "abc";
    }
}