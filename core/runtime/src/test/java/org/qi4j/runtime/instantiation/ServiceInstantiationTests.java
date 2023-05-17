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

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ServiceInstantiationTests
        extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( MyConfiguration.class );
        module.services( My.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void whenCreatingServiceCompositeGivenAServiceCompositeThenSucceed()
        throws Exception
    {
        ServiceReference<My> service = serviceFinder.findService( My.class );
        assertThat( service.get().doSomething(), equalTo( "HabbaZout" ) );
    }

    @Mixins( MyMixin.class )
    public interface My
    {
        String doSomething();
    }

    public interface MyConfiguration
    {
        Property<String> data();
    }

    public static class MyMixin
        implements My
    {
        @This
        Configuration<MyConfiguration> config;

        @Override
        public String doSomething()
        {
            return config.get().data().get();
        }
    }
}
