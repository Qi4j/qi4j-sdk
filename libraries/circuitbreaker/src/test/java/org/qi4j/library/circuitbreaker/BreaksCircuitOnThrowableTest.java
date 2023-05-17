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
package org.qi4j.library.circuitbreaker;

import java.beans.PropertyVetoException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.circuitbreaker.service.AbstractBreakOnThrowable;
import org.qi4j.library.circuitbreaker.service.BreaksCircuitOnThrowable;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test @BreaksCircuitOnThrowable annotation
 */
public class BreaksCircuitOnThrowableTest
    extends AbstractQi4jTest
{

    // START SNIPPET: service
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestService.class ).setMetaInfo( new CircuitBreaker() );
    }
    // END SNIPPET: service

    @Test
    public void testSuccess()
    {
        TestService service = serviceFinder.findService( TestService.class ).get();
        service.successfulMethod();
        service.successfulMethod();
        service.successfulMethod();
    }

    @Test
    public void testThrowable()
    {
        ServiceReference<TestService> serviceReference = serviceFinder.findService( TestService.class );
        TestService service = serviceReference.get();
        assertThrows( Exception.class, () -> service.throwingMethod(), "Service should have thrown exception" );

        assertThrows( Exception.class, () -> service.successfulMethod(), "Circuit breaker should have tripped" );

        try
        {
            serviceReference.metaInfo( CircuitBreaker.class ).turnOn();
        }
        catch( PropertyVetoException e )
        {
            fail( "Should have been possible to turn on circuit breaker" );
        }

        try
        {
            service.successfulMethod();
        }
        catch( Exception e )
        {
            fail( "Circuit breaker should have been turned on" );
        }
    }

    @Mixins( TestService.Mixin.class )
    // START SNIPPET: service
    public interface TestService
        extends AbstractBreakOnThrowable, ServiceComposite
    {

        @BreaksCircuitOnThrowable
        int successfulMethod();

        @BreaksCircuitOnThrowable
        void throwingMethod();

        // END SNIPPET: service
        abstract class Mixin
            implements TestService
        {

            int count = 0;

            public void throwingMethod()
            {
                throw new IllegalArgumentException( "Failed" );
            }

            public int successfulMethod()
            {
                return count++;
            }
        }

        // START SNIPPET: service
    }
    // END SNIPPET: service
}
