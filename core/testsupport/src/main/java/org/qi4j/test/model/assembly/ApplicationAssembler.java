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
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.layered.IllegalLayerAssemblerException;
import org.qi4j.bootstrap.layered.LayerAssembler;
import org.qi4j.bootstrap.layered.LayeredApplicationAssembler;

public class ApplicationAssembler extends LayeredApplicationAssembler
{

    private final Class<?> testClass;

    public ApplicationAssembler( String name, String version, Application.Mode mode, Class<?> testClass )
        throws AssemblyException
    {
        super( name, version, mode );
        this.testClass = testClass;
    }

    @Override
    protected void assembleLayers( ApplicationAssembly assembly )
    {
        LayerAssembly accessLayer = createLayer( AccessLayer.class );
        LayerAssembly domainLayer = createLayer( DomainLayer.class );
        LayerAssembly persistenceLayer = createLayer( PersistenceLayer.class );
        LayerAssembly indexingLayer = createLayer( IndexingLayer.class );
        LayerAssembly configLayer = createLayer( ConfigLayer.class );
        accessLayer.uses( domainLayer );
        domainLayer.uses( persistenceLayer, indexingLayer );
        persistenceLayer.uses( configLayer );
        indexingLayer.uses( configLayer );
    }

    @Override
    protected <T extends LayerAssembler> LayerAssembler instantiateLayerAssembler( Class<T> layerAssemblerClass,
                                                                                   LayerAssembly layer
    )
        throws InstantiationException, IllegalAccessException, InvocationTargetException, IllegalLayerAssemblerException
    {
        if( layerAssemblerClass.equals( AccessLayer.class ))
        {
            return new AccessLayer( testClass );
        }
        return super.instantiateLayerAssembler( layerAssemblerClass, layer );
    }
}
