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

import java.util.Collections;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblyReportException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.Qi4jSPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractQi4jBaseTest
{
    protected Qi4jAPI api;
    protected Qi4jSPI spi;

    protected Energy4Java qi4j;
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        qi4j = new Energy4Java();
        applicationModel = newApplicationModel();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = newApplicationInstance( applicationModel );
        initApplication( application );
        api = spi = qi4j.spi();
        application.activate();
    }

    /**
     * Called by the superclass for the test to define the entire application, every layer, every module and all
     * the contents of each module.
     *
     * @param applicationAssembly the {@link ApplicationAssembly} to be populated.
     * @throws AssemblyException on invalid assembly
     */
    protected abstract void defineApplication( ApplicationAssembly applicationAssembly )
        throws Exception;

    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        return applicationModel.newInstance( qi4j.api() );
    }

    protected ApplicationDescriptor newApplicationModel()
        throws AssemblyException
    {
        ApplicationAssembler assembler = applicationFactory ->
        {
            ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
            applicationAssembly.setMode( Application.Mode.test );
            try
            {
                defineApplication( applicationAssembly );
            }
            catch( Exception e )
            {
                throw new AssemblyReportException( Collections.singleton( e ) );
            }
            return applicationAssembly;
        };

        try
        {
            return qi4j.newApplicationModel( assembler );
        }
        catch( AssemblyException e )
        {
            assemblyException( e );
            return null;
        }
    }

    /**
     * This method is called when there was an AssemblyException in the creation of the Qi4j application model.
     * <p>
     * Override this method to catch valid failures to place into satisfiedBy suites.
     * </p>
     *
     * @param exception the exception thrown.
     * @throws AssemblyException The default implementation of this method will simply re-throw the exception.
     */
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        throw exception;
    }

    protected void initApplication( Application app )
        throws Exception
    {
    }

    @AfterEach
    public void tearDown()
    {
        if( application != null )
        {
            try
            {
                application.passivate();
            }
            catch( Exception e )
            {
                throw new RuntimeException( "Unable to shut down test harness cleanly.", e );
            }
        }
    }
}
