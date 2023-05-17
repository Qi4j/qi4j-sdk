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
package org.qi4j.library.constraints;

import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.constraints.annotation.HostPort;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HostPortConstraintTest extends AbstractQi4jTest
{

    @Test
    public void givenValidHostWithoutPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com" );
    }

    @Test
    public void givenValidHostPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com:1234" );
    }

    @Test
    public void givenLocalHostWithoutPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "localhost" );
    }

    @Test
    public void givenLocalHostWithSmallPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "localhost:9" );
    }

    @Test
    public void givenLocalHostWithLargePortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "localhost:1234" );
    }

    @Test
    public void givenInvalidHostNameWhenSettingPropertyExpectConstrainViolation()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
            someValue.hostPort().set( "1:2:3_i:1234" );
        } );
    }

    @Test
    public void givenInvalidHostNameWhenConstructingExpectConstrainViolation()
        throws Exception
    {
        TransientBuilder<SomeValue> builder = transientBuilderFactory.newTransientBuilder( SomeValue.class );
        assertThrows( ConstraintViolationException.class, () -> {
            builder.prototype().hostPort().set( "1:2:3_i:1234" );
            builder.newInstance();
        } );
    }

    @Test
    public void givenInvalidHostNameWhenCallingServiceExpectConstrainViolation()
        throws Exception
    {
        SomeService someService = serviceFinder.findService( SomeService.class ).get();
        assertThrows( ConstraintViolationException.class, () -> {
            someService.doSomething( "1:2:3_i:1234" );
        } );
    }

    @Test
    public void givenInvalidPortNumberWhenSettingPropertyExpectConstrainViolation()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
            someValue.hostPort().set( "1.2.3.4:123456" );
        } );
    }

    @Test
    public void givenValidIp4NumberPortNumberWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "1.2.3.4:1234" );
    }

    @Test
    public void givenValidIp6NumberPortNumberWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "[::1]:1234" );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SomeValue.class );
        module.services( SomeService.class );
    }

    public interface SomeValue
    {
        @HostPort
        @Optional
        Property<String> hostPort();
    }

    @Mixins( NoopMixin.class )
    public interface SomeService
    {
        void doSomething(@HostPort String hostPort);
    }
}
