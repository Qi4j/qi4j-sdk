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
package org.qi4j.api.value;

import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

/**
 * TODO
 */
public class ValueBuilderTemplateTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( TestValue.class );
    }

    @Test
    public void testTemplate()
    {
        new TestBuilder( "Rickard" ).newInstance( module );
    }

    @Test
    public void testAnonymousTemplate()
    {
        new ValueBuilderTemplate<TestValue>( TestValue.class )
        {
            @Override
            protected void build( TestValue prototype )
            {
                prototype.name().set( "Rickard" );
            }
        }.newInstance( module );
    }

    interface TestValue
        extends ValueComposite
    {
        Property<String> name();
    }

    class TestBuilder
        extends ValueBuilderTemplate<TestValue>
    {
        String name;

        TestBuilder( String name )
        {
            super( TestValue.class );
            this.name = name;
        }

        @Override
        protected void build( TestValue prototype )
        {
            prototype.name().set( name );
        }
    }

    ;
}
