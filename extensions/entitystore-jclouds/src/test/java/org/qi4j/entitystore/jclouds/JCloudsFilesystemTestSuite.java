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

import java.util.Collections;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jclouds.assembly.JCloudsEntityStoreAssembler;
import org.qi4j.test.TemporaryFolder;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith( TemporaryFolder.class )
public class JCloudsFilesystemTestSuite extends EntityStoreTestSuite
{
    private TemporaryFolder tmpDir;

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new JCloudsEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        JCloudsEntityStoreConfiguration defaults = configModule.forMixin( JCloudsEntityStoreConfiguration.class )
                                                               .declareDefaults();
        defaults.provider().set( "filesystem" );
        defaults.properties().set( Collections.singletonMap( FilesystemConstants.PROPERTY_BASEDIR,
                                                             tmpDir.getRoot().getAbsolutePath() ) );
    }
}
