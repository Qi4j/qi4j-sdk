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
package org.qi4j.entitystore.jclouds;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Environment;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jclouds.assembly.JCloudsEntityStoreAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;

@Docker( image = "scality/s3server:mem-bb2a38c0",
         ports = @Port( exposed = 8801, inner = 8000 ),
         waitFor = @WaitFor( value = "server started", timeoutInMillis = 30000 ),
         environments = {
             @Environment( key = "SCALITY_ACCESS_KEY_ID", value = "dummyIdentifier" ),
             @Environment( key = "SCALITY_SECRET_ACCESS_KEY", value = "dummyCredential" )
         },
         newForEachCase = false
)
public class JCloudsS3Test extends AbstractEntityStoreTest
{
    static long time = 240000;

    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        Thread.sleep( time );
        time = 0;
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        new JCloudsEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        JCloudsEntityStoreConfiguration defaults =
            config.forMixin( JCloudsEntityStoreConfiguration.class ).declareDefaults();

        String host = "localhost";
        int port = 8801;
        defaults.provider().set( "s3" );
        defaults.endpoint().set( "http://" + host + ':' + port );
        defaults.identifier().set( "dummyIdentifier" );
        defaults.credential().set( "dummyCredential" );
    }
}
