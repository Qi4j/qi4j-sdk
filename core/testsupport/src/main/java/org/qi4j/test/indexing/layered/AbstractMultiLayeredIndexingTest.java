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

package org.qi4j.test.indexing.layered;

import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.layered.ModuleAssembler;
import org.qi4j.test.indexing.TestData;
import org.qi4j.test.model.assembly.ApplicationAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractMultiLayeredIndexingTest
{
    public static Class<? extends ModuleAssembler> indexingAssembler;

    protected Application application;

    @Structure
    private UnitOfWorkFactory uowf;

    @Optional
    @Service
    @Tagged( "Suite1Case1" )
    private ServiceReference<TestCase> suite1Case1;

    @Optional
    @Service
    @Tagged( "Suite1Case2" )
    private ServiceReference<TestCase> suite1Case2;

    @Optional
    @Service
    @Tagged( "Suite2Case1" )
    private ServiceReference<TestCase> suite2Case1;

    @Optional
    @Service
    @Tagged( "Suite3Case1" )
    private ServiceReference<TestCase> suite3Case1;

    public AbstractMultiLayeredIndexingTest( Class<? extends ModuleAssembler> indexingAssembler )
    {
        AbstractMultiLayeredIndexingTest.indexingAssembler = indexingAssembler;
    }

    @BeforeEach
    public void setup()
        throws ActivationException
    {
        ApplicationAssembler assembler =
            new ApplicationAssembler( "Multi Layered Indexing Test", "1.0", Application.Mode.development, getClass() );
        assembler.initialize();
        assembler.start();
        application = assembler.application();
        Module familyModule = application.findModule( "Domain Layer", "Family Module" );
        TestData.populate( familyModule );
        Module executionModule = application.findModule( "Access Layer", "TestExecution Module" );
        executionModule.injectTo( this );
    }

    @Test
    public void suite1Case1()
        throws Exception
    {
        runTest( suite1Case1, "suite1Case1" );
    }

    @Test
    public void suite1Case2()
        throws Exception
    {
        runTest( suite1Case2, "suite1Case2"  );
    }

    @Test
    public void suite2Case1()
        throws Exception
    {
        runTest( suite2Case1, "suite2Case1"  );
    }

    @Test
    public void suite3Case1()
        throws Exception
    {
        runTest( suite3Case1, "suite3Case1"  );
    }

    private void runTest( ServiceReference<TestCase> testCaseRef, String testName )
        throws Exception
    {
        if( testCaseRef == null )
        {
            System.err.println( "TestCase is not defined." );
        }
        else
        {
            TestCase testCase = testCaseRef.get();
            try(UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( testName ) ))
            {
                testCase.given();
                testCase.when();
                testCase.expect();
                uow.complete();
            }
        }
    }
}
