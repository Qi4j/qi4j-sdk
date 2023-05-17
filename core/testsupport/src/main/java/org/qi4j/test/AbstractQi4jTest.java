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

package org.qi4j.test;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractQi4jTest extends AbstractQi4jBaseTest
    implements Assembler
{
    @Structure
    protected UnitOfWorkFactory unitOfWorkFactory;

    @Structure
    protected TransientBuilderFactory transientBuilderFactory;

    @Structure
    protected ValueBuilderFactory valueBuilderFactory;

    @Structure
    protected ServiceFinder serviceFinder;

    @Structure
    protected ObjectFactory objectFactory;

    @Structure
    protected QueryBuilderFactory queryBuilderFactory;

    @Structure
    protected ModuleDescriptor module;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        if( application == null )
        {
            return; // failure in Assembly.
        }
        Module module = application.findModule( "Layer 1", "Module 1" );
        module.injectTo( this );
    }

    @Override
    protected void defineApplication( ApplicationAssembly applicationAssembly )
        throws Exception
    {
        LayerAssembly layer = applicationAssembly.layer( "Layer 1" );
        ModuleAssembly module = layer.module( "Module 1" );
        module.objects( AbstractQi4jTest.this.getClass() );
        assemble( module );
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        if( unitOfWorkFactory != null && unitOfWorkFactory.isUnitOfWorkActive() )
        {
            while( unitOfWorkFactory.isUnitOfWorkActive() )
            {
                UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
                if( uow.isOpen() )
                {
                    System.err.println( "UnitOfWork not cleaned up:" + uow.usecase().name() );
                    uow.discard();
                }
                else
                {
                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened. First is: " + uow
                        .usecase()
                        .name() );
                }
            }
            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }
        super.tearDown();
    }
}
