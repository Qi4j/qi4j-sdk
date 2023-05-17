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

package org.qi4j.runtime.property;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for string arrays as properties (QI-132)
 */
public class PropertyStringArrayTest
        extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    @Test
    public void testProperty()
    {
        TestComposite instance;
        {
            TransientBuilder<TestComposite> builder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
            builder.prototype().array().set( new String[]{ "Foo", "Bar" } );
            instance = builder.newInstance();
        }

        assertThat( "property has correct value", instance.array().get()[ 0 ], equalTo( "Foo" ) );

        instance.array().set( new String[]{ "Hello", "World" } );

        assertThat( "property has correct value", instance.array().get()[ 0 ], equalTo( "Hello" ) );
    }

    public interface TestComposite
        extends TransientComposite
    {
        Property<String[]> array();
    }
}
