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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of configuration for services that Constraints are respected.
 */
public class ConfigurationConstraintTest
{
    @Test
    public void givenConstrainedConfigurationWhenCorrectValueExpectNoFailure()
        throws Exception
    {
        SingletonAssembler underTest = new SingletonAssembler(
            module ->
            {
                module.defaultServices();
                module.services( MemoryEntityStoreService.class );
                module.services( TestService.class ).identifiedBy( "TestService1" );
                module.configurations( TestConfiguration.class );
            }
        );
        ServiceReference<TestService> service = underTest.module().findService( TestService.class );
        service.get().test();
    }

    @Test
    public void givenConstrainedConfigurationWhenIncorrectValueExpectConstraintViolationFailure()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            SingletonAssembler underTest = new SingletonAssembler(
                module ->
                {
                    module.defaultServices();
                    module.services( MemoryEntityStoreService.class );
                    module.services( TestService.class ).identifiedBy( "TestService2" );
                    module.configurations( TestConfiguration.class );
                }
            );
            ServiceReference<TestService> service = underTest.module().findService( TestService.class );
            service.get().test();
        } );
    }

    @Mixins( TestMixin.class )
    public interface TestService
    {
        void test();
    }

    public interface TestConfiguration
        extends ConfigurationComposite
    {
        @Constrained
        Property<String> constrained();
    }

    public static class TestMixin
        implements TestService
    {
        @This
        Configuration<TestConfiguration> config;

        @Override
        public void test()
        {
            assertThat( config.get().constrained().get(), equalTo( "constrained" ) );
        }
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Constraints( ConstrainedConstraint.class )
    public @interface Constrained
    {
    }

    public static class ConstrainedConstraint
        implements Constraint<Constrained, String>
    {
        @Override
        public boolean isValid( Constrained annotation, String value )
        {
            return value.equals( "constrained" );
        }
    }
}
