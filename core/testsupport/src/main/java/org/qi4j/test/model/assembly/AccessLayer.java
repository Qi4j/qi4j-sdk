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

package org.qi4j.test.model.assembly;

import java.lang.reflect.InvocationTargetException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.layered.LayeredLayerAssembler;
import org.qi4j.bootstrap.layered.ModuleAssembler;

class AccessLayer extends LayeredLayerAssembler
{

    private final Class<?> testClass;

    AccessLayer( Class<?> testClass )
    {
        this.testClass = testClass;
    }

    @Override
    public LayerAssembly assemble( LayerAssembly layer )
    {
        createModule( layer, TestExecutionModule.class );
        createModule( layer, TestSuite1Module.class );
        createModule( layer, TestSuite2Module.class );
        createModule( layer, TestSuite3Module.class );
        return layer;
    }

    @Override
    protected ModuleAssembler instantiateModuleAssembler( Class<? extends ModuleAssembler> moduleAssemblerClass, ModuleAssembly constructorArgument
                                                        )
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        if( moduleAssemblerClass.equals(TestExecutionModule.class))
        {
            return new TestExecutionModule( testClass );
        }
        else
        {
            return super.instantiateModuleAssembler( moduleAssemblerClass, constructorArgument );
        }
    }
}
