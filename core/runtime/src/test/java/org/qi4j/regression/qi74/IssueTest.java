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
package org.qi4j.regression.qi74;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class IssueTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( ValueHolder.class );
    }

    @Test
    public void testConstraintCheckedOnCompsiteCreation()
    {
        try
        {
            TransientBuilder<ValueHolder> builder = transientBuilderFactory.newTransientBuilder( ValueHolder.class );
            builder.newInstance();
            fail( "NotNull constraint violated but no exception is raised" );
        }
        catch( ConstraintViolationException e )
        {
            // expected
        }
    }

    static interface ValueHolder
        extends TransientComposite
    {
        Property<String> val();
    }
}
