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
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.constraints.annotation.HostPortList;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HostPortListConstraintTest extends AbstractQi4jTest
{

    @Test
    public void givenValidHostWithoutPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com" );
    }

    @Test
    public void givenValidHostListWithoutPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com, localhost google.com" );
    }

    @Test
    public void givenValidHostListWithSomePortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com, localhost:8080, google.com" );
    }

    @Test
    public void givenValidHostPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com:1234" );
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

    @Test
    public void givenValidListOfOneHostPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "localhost:32775" );
    }

    @Test
    public void givenValidListHostPortWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "habba.zout.com:1234,12.34.56.78:1234" );
    }

    @Test
    public void givenInvalidListHostNameWhenSettingPropertyExpectConstrainViolation()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
            someValue.hostPort().set( "1.2.3.4:12,1:2:3_i:1234" );
        } );
    }

    @Test
    public void givenInvalidListPortNumberWhenSettingPropertyExpectConstrainViolation()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
            someValue.hostPort().set( "1.2.3.4:1234 1.2.3.4:123456" );
        } );
    }

    @Test
    public void givenValidListIp4NumberPortNumberWhenSettingPropertyExpectSuccess()
        throws Exception
    {
        SomeValue someValue = transientBuilderFactory.newTransient( SomeValue.class );
        someValue.hostPort().set( "1.2.3.4:1234 google.com" );
    }

    @Test
    public void givenValidListIp6NumberPortNumberWhenSettingPropertyExpectSuccess()
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
    }

    public interface SomeValue
    {
        @HostPortList
        @Optional
        Property<String> hostPort();
    }
}
