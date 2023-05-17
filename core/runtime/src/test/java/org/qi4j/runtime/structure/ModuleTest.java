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

package org.qi4j.runtime.structure;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of the Module interface. This should satisfiedBy both the general interface and all possible visibility
 * rules.
 */
public class ModuleTest
{
    public Application givenFixture1()
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();
        return qi4j.newApplication(
            factory -> factory.newApplicationAssembly(
                new Assembler[][][]
                    {
                        {
                            {
                                module -> module.transients( TestComposite1.class )
                            },
                            {
                                module -> module.transients( TestComposite2.class ).visibleIn( Visibility.layer )
                            }
                        }
                    } ) );
    }

    @Test
    public void givenFixture1WhenGetNameThenConfiguredNameIsReturned()
        throws AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        assertThat( "module name is correct", module.name(), equalTo( "Module 1" ) );
    }

    @Test
    public void givenFixture1WhenGetModuleForCompositeThenCorrectModuleIsReturned()
        throws AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        assertThat( "module for composite is correct", module, equalTo( module ) );
    }

    @Test
    public void givenFixture1WhenFindFromAnotherModuleThenCompositeTypeIsReturned()
        throws ClassNotFoundException, AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        module.descriptor().classLoader().loadClass( TestComposite2.class.getName() );
    }

    @Mixins( TestMixin1.class )
    public interface TestComposite1
        extends TransientComposite
    {
        Module getModule();
    }

    public abstract static class TestMixin1
        implements TestComposite1
    {
        @Structure
        Module module;

        public Module getModule()
        {
            return module;
        }
    }

    @Mixins( TestMixin2.class )
    public interface TestComposite2
        extends TransientComposite
    {
        Module getModule();
    }

    public abstract static class TestMixin2
        implements TestComposite2
    {
        @Structure
        Module module;

        public Module getModule()
        {
            return module;
        }
    }

    public interface TestComposite21
        extends TestComposite2
    {
    }

    public static class TestObject1
    {

    }

    public static class TestObject2
    {

    }
}
