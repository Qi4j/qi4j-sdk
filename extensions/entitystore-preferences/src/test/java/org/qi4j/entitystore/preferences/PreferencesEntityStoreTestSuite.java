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
package org.qi4j.entitystore.preferences;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.preferences.assembly.PreferencesEntityStoreAssembler;
import org.qi4j.test.TemporaryFolder;
import org.qi4j.test.entity.model.EntityStoreTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith( TemporaryFolder.class )
public class PreferencesEntityStoreTestSuite extends EntityStoreTestSuite
{
    private TemporaryFolder tmpDir;
    private Preferences rootNode;

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        String applicationName = module.layer().application().name();
        rootNode = Preferences.userRoot().node( applicationName );

        new PreferencesEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .assemble( module );
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        try
        {
            rootNode.removeNode();
        }
        catch( BackingStoreException e )
        {
            throw new RuntimeException( e );
        }
    }
}
