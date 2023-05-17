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
package org.qi4j.tutorials.cargo.step2;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class Step2TestCase
    extends AbstractQi4jTest
{
    private Voyage voyage;
    private ShippingService shippingService;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        TransientBuilder<VoyageComposite> voyageBuilder = transientBuilderFactory.newTransientBuilder( VoyageComposite.class );
        voyageBuilder.prototypeFor( Voyage.class ).bookedCargoSize().set( 0.0 );
        voyageBuilder.prototypeFor( Voyage.class ).capacity().set( 100.0 );
        voyage = voyageBuilder.newInstance();

        TransientBuilder<ShippingServiceComposite> shippingBuilder =
            transientBuilderFactory.newTransientBuilder( ShippingServiceComposite.class );
        shippingService = shippingBuilder.newInstance();
    }

    @Test
    public void testOrdinaryBooking()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 20 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertThat( code, equalTo( 0 ) );
        code = shippingService.makeBooking( cargo2, voyage );
        assertThat( code, equalTo( 1 ) );
        code = shippingService.makeBooking( cargo3, voyage );
        assertThat( code, equalTo( 2 ) );
    }

    @Test
    public void testOverbooking()
    {
        Cargo cargo1 = newCargo( 100 );
        Cargo cargo2 = newCargo( 9 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertThat( code, equalTo( 0 ) );
        code = shippingService.makeBooking( cargo2, voyage );
        assertThat( code, equalTo( -1 ) );
    }

    @Test
    public void testTooMuch()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 31 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertThat( code, equalTo( 0 ) );
        code = shippingService.makeBooking( cargo2, voyage );
        assertThat( code, equalTo( 1 ) );
        code = shippingService.makeBooking( cargo3, voyage );
        assertThat( code, equalTo( -1 ) );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( VoyageComposite.class, CargoComposite.class, ShippingServiceComposite.class );
    }

    private Cargo newCargo( double size )
    {
        TransientBuilder<CargoComposite> builder = transientBuilderFactory.newTransientBuilder( CargoComposite.class );
        builder.prototypeFor( Cargo.class ).size().set( size );
        return builder.newInstance();
    }
}
