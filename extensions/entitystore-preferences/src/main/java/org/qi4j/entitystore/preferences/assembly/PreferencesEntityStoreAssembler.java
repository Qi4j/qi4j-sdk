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
package org.qi4j.entitystore.preferences.assembly;

import java.util.prefs.Preferences;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.entitystore.preferences.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.preferences.PreferencesEntityStoreService;
import org.qi4j.entitystore.preferences.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.preferences.PreferencesEntityStoreService;

public class PreferencesEntityStoreAssembler extends Assemblers.VisibilityIdentityConfig<PreferencesEntityStoreAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        String applicationName = module.layer().application().name();

        Preferences root = Preferences.userRoot();
        Preferences node = root.node( applicationName );
        PreferencesEntityStoreInfo info = new PreferencesEntityStoreInfo( node );
        ServiceDeclaration service = module.services( PreferencesEntityStoreService.class )
                                           .setMetaInfo( info )
                                           .visibleIn( visibility() )
                                           .instantiateOnStartup();
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
    }
}
