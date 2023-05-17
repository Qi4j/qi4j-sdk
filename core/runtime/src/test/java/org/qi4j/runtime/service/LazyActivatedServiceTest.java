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

package org.qi4j.runtime.service;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test of lazily activated services
 */
public class LazyActivatedServiceTest
{
    @Service
    ServiceReference<MyService> service;

    public static boolean isActive;

    @Test
    public void testActivatable()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( LazyActivatedServiceTest.class );
                module.services( LazyActivatedServiceTest.MyServiceComposite.class ).withActivators( TestActivator.class );
            }
        };

        assertThat( isActive, is( false ) );

        assembly.module().injectTo( this );

        assertThat( isActive, is( false ) );

        service.get();

        assertThat( isActive, is( false ) );

        service.get().doStuff();

        assertThat( isActive, is( true ) );

        assembly.application().passivate();

        assertThat( isActive, is( false ) );
    }

    @Mixins( { MyServiceMixin.class } )
    public static interface MyServiceComposite
        extends MyService, ServiceComposite
    {
    }

    public static interface MyService
    {
        String doStuff();
    }

    public static class MyServiceMixin
        implements MyService
    {

        public String doStuff()
        {
            return "X";
        }
    }

    public static class TestActivator
            extends ActivatorAdapter<Object>
    {

        @Override
        public void afterActivation( Object activated )
        {
            isActive = true;
        }

        @Override
        public void afterPassivation( Object passivated )
                throws Exception
        {
            if ( !isActive ) {
                throw new Exception( "Not active!" );
            }

            isActive = false;
        }

    }
}
