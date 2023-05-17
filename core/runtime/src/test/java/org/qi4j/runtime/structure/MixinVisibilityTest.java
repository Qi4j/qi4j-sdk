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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.NoSuchTransientTypeException;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JAVADOC
 */
public class MixinVisibilityTest
{
    @Test
    public void testMixinInModuleIsVisible()
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     module -> {
                         module.setName( "Module A" );
                         module.transients( B1Composite.class );
                         module.objects( ObjectA.class );
                     }
                  }
                }
            };

        Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertThat( object.test1(), equalTo( "ok" ) );
        assertThat( object.test2(), equalTo( "abc" ) );
    }

    @Test
    public void testMultipleMixinsInModuleWillFail()
        throws Exception
    {
        assertThrows( AmbiguousTypeException.class, () -> {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
                {
                    { // Layer
                      {  // Module 1
                         module -> {
                             module.setName( "Module A" );
                             module.transients( B1Composite.class, B2Composite.class );
                             module.objects( ObjectA.class );
                         }
                      }
                    }
                };

            Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            app.activate();
            ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
            assertThat( object.test1(), equalTo( "ok" ) );
            assertThat( object.test2(), equalTo( "abc" ) );
        } );
    }

    @Test
    public void testMixinInLayerIsNotVisible()
        throws Exception
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
                {
                    { // Layer
                      {
                          module -> {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      },
                      {
                          module -> {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class );
                          }
                      }
                    }
                };

            Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            app.activate();
            ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
            assertThat( object.test1(), equalTo( "ok" ) );
            assertThat( object.test2(), equalTo( "abc" ) );
        } );
    }

    @Test
    public void testMixinInLayerIsVisible()
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  },
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                      }
                  }
                }
            };

        Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertThat( object.test1(), equalTo( "ok" ) );
        assertThat( object.test2(), equalTo( "abc" ) );
    }

    @Test
    public void testMultipleMixinsInLayerWillFailSameModule()
        throws Exception
    {
        assertThrows( AmbiguousTypeException.class, () -> {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
                {
                    { // Layer
                      {
                          module -> {
                              module.setName( "Module A" );
                              module.objects( ObjectA.class );
                          }
                      },
                      {
                          module -> {
                              module.setName( "Module B" );
                              module.transients( B1Composite.class, B2Composite.class )
                                  .visibleIn( Visibility.layer );
                          }
                      }
                    }
                };

            Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            app.activate();
            ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
            assertThat( object.test1(), equalTo( "ok" ) );
            assertThat( object.test2(), equalTo( "abc" ) );
        } );
    }

    @Test
    public void testMultipleMixinsInLayerWillFailDiffModule()
        throws Exception
    {
        assertThrows( AmbiguousTypeException.class, () -> {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
                {
                    { // Layer
                      { // Module 1
                        module -> {
                            module.setName( "Module A" );
                            module.objects( ObjectA.class );
                        }
                      },
                      { // Module 2
                        module -> {
                            module.setName( "Module B" );
                            module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                        }
                      },
                      { // Module 3
                        module -> {
                            module.setName( "Module C" );
                            module.transients( B2Composite.class ).visibleIn( Visibility.layer );
                        }
                      }
                    }
                };

            Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            app.activate();
            ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
            assertThat( object.test1(), equalTo( "ok" ) );
            assertThat( object.test2(), equalTo( "abc" ) );
        } );
    }

    // @Test( expected= MixinTypeNotAvailableException.class )

    public void testMixinInLowerLayerIsNotVisible()
        throws Exception
    {

        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  }
                },
                { // Layer 2
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                      }
                  }
                }
            };

        Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module " ).newObject( ObjectA.class );
        assertThat( object.test1(), equalTo( "ok" ) );
        assertThat( object.test2(), equalTo( "abc" ) );
    }

    @Test
    public void testMixinInLowerLayerIsVisible()
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  }
                },
                { // Layer 2
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.application );
                      }
                  }
                }
            };

        Application app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertThat( object.test1(), equalTo( "ok" ) );
        assertThat( object.test2(), equalTo( "abc" ) );
    }

    class AssemblerB
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
        {
            module.setName( "Module B" );
            module.transients( B1Composite.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure
        TransientBuilderFactory cbf;

        String test1()
        {
            B1 instance = cbf.newTransient( B1.class );
            return instance.test();
        }

        String test2()
        {
            TransientBuilder<B2> builder = cbf.newTransientBuilder( B2.class );
            builder.prototypeFor( B2.class ).b2().set( "abc" );
            B2 instance = builder.newInstance();
            return instance.b2().get();
        }
    }

    @Mixins( { MixinB.class } )
    public interface B1Composite
        extends B1
    {
    }

    public interface B2Composite
        extends B2
    {
    }

    public interface B2
    {
        @Optional
        Property<String> b2();
    }

    public interface B1
        extends B2
    {
        String test();
    }

    public abstract static class MixinB
        implements B1
    {
        public String test()
        {
            return "ok";
        }
    }
}
