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
package org.qi4j.regression.qi53;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class IssueTest
        extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CostPerUnitComposite.class );
    }

    @Test
    public void genericPropertiesAndParameters()
        throws SecurityException, NoSuchMethodException
    {
        TransientBuilder<CostPerUnitComposite> builder = transientBuilderFactory.newTransientBuilder( CostPerUnitComposite.class );
        builder.prototype().unit().set( new Unit<>( 10 ) );
        CostPerUnitComposite test = builder.newInstance();
        assertThat( test.unit().get().value, equalTo( 10 ) );
        assertThat( test.toCostPer( new Unit<>( 50 ) ).unit().get().value, equalTo( 50 ) );
    }

    public interface CostPerUnit
    {
        @Immutable
        Property<Unit<?>> unit();

        CostPerUnit toCostPer( Unit<?> unit );
    }

    public static class Unit<T>
    {
        private T value;

        public Unit( T value )
        {
            this.value = value;
        }

        T get()
        {
            return value;
        }
    }

    public static abstract class CostPerUnitMixin
        implements CostPerUnit
    {

        @This
        CostPerUnit costPerUnit;
        @Structure
        TransientBuilderFactory builderFactory;

        public CostPerUnit toCostPer( Unit<?> unit )
        {
            TransientBuilder<CostPerUnitComposite> builder =
                builderFactory.newTransientBuilder( CostPerUnitComposite.class );

            builder.prototype().unit().set( unit );
            return builder.newInstance();
        }
    }

    @Mixins( { CostPerUnitMixin.class } )
    public interface CostPerUnitComposite
        extends CostPerUnit, TransientComposite
    {
    }
}
