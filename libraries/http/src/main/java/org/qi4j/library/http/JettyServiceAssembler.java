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
package org.qi4j.library.http;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.eclipse.jetty.server.Server;

public class JettyServiceAssembler
    extends Assemblers.VisibilityIdentityConfig<JettyServiceAssembler>
{
    protected ModuleAssembly serverModule;
    protected Visibility serverVisibility = Visibility.module;
    protected String serverIdentity;

    /**
     * @param serverModule Defaults to assembled module
     * @param serverVisibility Defaults to {@link Visibility#module}
     */
    public JettyServiceAssembler withServer( ModuleAssembly serverModule, Visibility serverVisibility )
    {
        this.serverModule = serverModule;
        this.serverVisibility = serverVisibility;
        return this;
    }

    public JettyServiceAssembler serverIdentifiedBy( String serverIdentity )
    {
        this.serverIdentity = serverIdentity;
        return this;
    }

    @Override
    public final void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ServiceDeclaration service = module.services( httpService() ).
            visibleIn( visibility() ).
            instantiateOnStartup();
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        if( hasConfig() )
        {
            configModule().entities( configurationEntity() ).visibleIn( configVisibility() );
        }
        assembleServer( module );
    }

    protected final void assembleServer( ModuleAssembly module )
    {
        serverModule = serverModule != null ? serverModule : module;
        ImportedServiceDeclaration server = serverModule.importedServices( Server.class ).
            importedBy( InstanceImporter.class ).
            setMetaInfo( new Server() ).
            visibleIn( serverVisibility );
        if( serverIdentity != null )
        {
            server.identifiedBy( serverIdentity );
        }
    }

    protected Class<? extends JettyConfiguration> configurationEntity()
    {
        return JettyConfiguration.class;
    }

    protected Class<? extends JettyService> httpService()
    {
        return JettyService.class;
    }
}
