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
package org.qi4j.regression.qi78;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.util.HierarchicalVisitorAdapter;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class IssueTest
{
    @Test
    public void testLayersCanBeCreatedInOrderDifferentFromTheirDependency()
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();

        Application app = qi4j.newApplication( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();

            LayerAssembly domainLayer = assembly.layer( null );
            domainLayer.setName( "Domain" );

            LayerAssembly infrastructureLayer = assembly.layer( null );
            infrastructureLayer.setName( "Infrastructure" );

            domainLayer.uses( infrastructureLayer );

            return assembly;
        } );
        ApplicationDescriptor model = app.descriptor();
        model.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                return visited instanceof ApplicationDescriptor;
            }

            @Override
            public boolean visitLeave( Object visited )
                throws RuntimeException
            {
                return visited instanceof LayerDescriptor;
            }

            @Override
            public boolean visit( Object visited )
                throws RuntimeException
            {
                if( visited instanceof LayerDescriptor )
                {
                    ( (LayerDescriptor) visited ).usedLayers().layers().forEach( usedLayerModel -> {
                        assertThat( "Used layer model is null", usedLayerModel, notNullValue() );
                    } );
                }

                return false;
            }
        } );
    }
}
