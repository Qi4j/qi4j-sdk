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

import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Base class for Assembler that creates an Application
 * with one Layer and one Module. Create a subclass of this
 * and implement the {@link Assembler#assemble(ModuleAssembly)} method.
 * Once the SingletonAssembler is instantiated it will have created and activated
 * an Application which can be accessed from {@link SingletonAssembler#application()}.
 * You can also easily access any resources specific for the single Module, such as the TransientBuilderFactory.
 */
public class SingletonAssembler
    implements Assembler
{
    private final Energy4Java qi4j;
    private final Application applicationInstance;
    private final Module moduleInstance;
    private Assembler assemble;

    /**
     * Creates a Qi4j Runtime instance containing one Layer with one Module.
     * The Layer will be named "Layer 1" and the Module will be named "Module 1". It is possible to add
     * additional layers and modules via the Assembler interface that must be implemented in the subclass of this
     * class.
     *
     * @param assemble An Assembler lambda containing the module assembly.
     * @throws AssemblyException   Either if the model can not be created from the disk, or some inconsistency in
     *                             the programming model makes it impossible to create it.
     * @throws ActivationException If the automatic {@code activate()} method is throwing this Exception..
     */
    public SingletonAssembler( Assembler assemble )
        throws ActivationException
    {
        this.assemble = assemble;
        qi4j = new Energy4Java();
        applicationInstance = createApplicationInstance();
        activateApplication();
        moduleInstance = applicationInstance.findModule( layerName(), moduleName() );
    }

    /**
     * Creates a Qi4j Runtime instance containing one Layer with one Module.
     * The Layer will be named "Layer 1" and the Module will be named "Module 1". It is possible to add
     * additional layers and modules via the Assembler interface that must be implemented in the subclass of this
     * class.
     *
     * @throws AssemblyException   Either if the model can not be created from the disk, or some inconsistency in
     *                             the programming model makes it impossible to create it.
     * @throws ActivationException If the automatic {@code activate()} method is throwing this Exception..
     */
    public SingletonAssembler()
        throws AssemblyException, ActivationException
    {
// START SNIPPET: actual
        qi4j = new Energy4Java();
        applicationInstance = createApplicationInstance();
        activateApplication();
// END SNIPPET: actual
        moduleInstance = applicationInstance.findModule( layerName(), moduleName() );
    }

    // START SNIPPET: actual
    private Application createApplicationInstance()
    {
        return qi4j.newApplication(
            applicationFactory -> applicationFactory.newApplicationAssembly( SingletonAssembler.this )
                                  );
    }

    private void activateApplication()
        throws ActivationException
    {
        try
        {
            beforeActivation( applicationInstance );
            applicationInstance.activate();
        }
        catch( Exception e )
        {
            if( e instanceof ActivationException )
            {
                throw ( (ActivationException) e );
            }
            throw new ActivationException( "Could not activate application", e );
        }
    }
// END SNIPPET: actual

    public final Qi4jAPI runtime()
    {
        return qi4j.spi();
    }

    public final Application application()
    {
        return applicationInstance;
    }

    public final Module module()
    {
        return moduleInstance;
    }

    protected String layerName()
    {
        return "Layer 1";
    }

    protected String moduleName()
    {
        return "Module 1";
    }

    protected void beforeActivation( Application application )
        throws Exception
    {
    }

    protected UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance.unitOfWorkFactory();
    }

    protected ServiceFinder serviceFinder()
    {
        return moduleInstance.serviceFinder();
    }

    protected ValueBuilderFactory valueBuilderFactory()
    {
        return moduleInstance.valueBuilderFactory();
    }

    protected TransientBuilderFactory transientBuilderFactory()
    {
        return moduleInstance.transientBuilderFactory();
    }

    protected ObjectFactory objectFactory()
    {
        return moduleInstance.objectFactory();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        if( assemble != null )
        {
            assemble.assemble( module );
        }
    }
}
