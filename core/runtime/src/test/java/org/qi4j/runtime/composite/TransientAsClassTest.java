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
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test for QI-298.
 */
public class TransientAsClassTest
    extends AbstractQi4jTest
{
    public static class UnderTestConcern extends ConcernOf<InvocationHandler>
        implements InvocationHandler
    {

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return next.invoke( proxy, method, args ) + " bar";
        }
    }

    @Concerns(UnderTestConcern.class)
    public static class UnderTest
    {
        public String foo()
        {
            return "foo";
        }
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( UnderTest.class );
    }

    @Test
    public void test()
    {
        UnderTest underTest = transientBuilderFactory.newTransient( UnderTest.class );
        assertThat( underTest.foo(), equalTo( "foo bar" ) );
    }
}
