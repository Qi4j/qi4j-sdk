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
package org.qi4j.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.identity.IdentityGenerator;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.util.HierarchicalVisitorAdapter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * TODO
 */
public class ApplicationAssemblerTest
{
    @Test
    public void testApplicationAssembler()
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();

        ApplicationDescriptor model = qi4j.newApplicationModel( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();

            LayerAssembly layer1 = assembly.layer( "Layer1" );

            ModuleAssembly module = layer1.module( "Module1" );

            module.services( TestService.class );

            module.entities( TestEntity.class );

            layer1.services( AssemblySpecifications.ofAnyType( TestService.class ) ).instantiateOnStartup();

            layer1.services( s -> true ).visibleIn( Visibility.layer );

            layer1.entities( s -> true ).visibleIn( Visibility.application );

            return assembly;
        } );

        model.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof ServiceDescriptor )
                {
                    ServiceDescriptor serviceDescriptor = (ServiceDescriptor) visited;
                    if( serviceDescriptor.hasType( UnitOfWorkFactory.class )
                        || serviceDescriptor.hasType( IdentityGenerator.class )
                        || serviceDescriptor.hasType( Serialization.class ) )
                    {
                        return false;
                    }
                    assertThat( serviceDescriptor.isInstantiateOnStartup(), is( true ) );
                    assertThat( serviceDescriptor.visibility(), equalTo( Visibility.layer ) );
                    return false;
                }
                else if( visited instanceof EntityDescriptor )
                {
                    EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                    assertThat( entityDescriptor.visibility(), equalTo( Visibility.application ) );
                    return false;
                }

                return true;
            }
        } );
        model.newInstance( qi4j.spi() );
    }

    interface TestService
        extends ServiceComposite
    {

    }

    interface TestEntity
        extends EntityComposite
    {

    }
}
