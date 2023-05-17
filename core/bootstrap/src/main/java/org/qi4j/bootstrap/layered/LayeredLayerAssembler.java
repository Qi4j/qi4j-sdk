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
package org.qi4j.bootstrap.layered;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import static org.qi4j.api.util.AccessibleObjects.accessible;

public abstract class LayeredLayerAssembler
    implements LayerAssembler
{
    private HashMap<Class<? extends ModuleAssembler>, ModuleAssembler> assemblers = new HashMap<>();

    protected ModuleAssembly createModule( LayerAssembly layer, Class<? extends ModuleAssembler> moduleAssemblerClass )
    {
        return createModule( layer, moduleAssemblerClass, null );
    }

    protected ModuleAssembly createModule( LayerAssembly layer, Class<? extends ModuleAssembler> moduleAssemblerClass, ModuleAssembly constructorArgumentModule )
    {
        try
        {
            String moduleName = createModuleName( moduleAssemblerClass );
            ModuleAssembly moduleAssembly = layer.module( moduleName );
            ModuleAssembler moduleAssembler = instantiateModuleAssembler( moduleAssemblerClass, constructorArgumentModule );
            LayeredApplicationAssembler.setNameIfPresent( moduleAssemblerClass, moduleName );
            ModuleAssembly module = layer.module( moduleName );
            assemblers.put( moduleAssemblerClass, moduleAssembler );
            ModuleAssembly assembly = moduleAssembler.assemble( layer, module );
            if( assembly == null )
            {
                return module;
            }
            return assembly;
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Unable to instantiate module with " + moduleAssemblerClass.getSimpleName(), e );
        }
    }

    protected String createModuleName( Class<? extends ModuleAssembler> modulerAssemblerClass )
    {
        String moduleName = modulerAssemblerClass.getSimpleName();
        if( moduleName.endsWith( "Module" ) )
        {
            moduleName = moduleName.substring( 0, moduleName.length() - 6 ) + " Module";
        }
        return moduleName;
    }

    protected ModuleAssembler instantiateModuleAssembler( Class<? extends ModuleAssembler> modulerAssemblerClass,
                                                          ModuleAssembly constructorArgument
                                                        )
        throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException
    {
        ModuleAssembler moduleAssembler;
        try
        {
            Constructor<? extends ModuleAssembler> assemblyConstructor = modulerAssemblerClass.getDeclaredConstructor( ModuleAssembly.class );
            moduleAssembler = accessible( assemblyConstructor ).newInstance( constructorArgument );
        }
        catch( NoSuchMethodException e )
        {
            Constructor<? extends ModuleAssembler> assemblyConstructor = modulerAssemblerClass.getDeclaredConstructor();
            moduleAssembler = accessible( assemblyConstructor ).newInstance();
        }
        return moduleAssembler;
    }

    @SuppressWarnings( "unchecked" )
    protected <T extends ModuleAssembler> T assemblerOf( Class<T> moduleAssemblerType )
    {
        return (T) assemblers.get( moduleAssemblerType );
    }
}
