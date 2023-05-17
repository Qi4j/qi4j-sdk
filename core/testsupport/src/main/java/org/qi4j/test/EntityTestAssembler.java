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
package org.qi4j.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;

/**
 * Helper assembler that adds an in-memory EntityStore and default services to the module.
 */
public class EntityTestAssembler
    extends Assemblers.VisibilityIdentity<EntityTestAssembler>
{
    private Visibility defaultServicesVisibility;

    public EntityTestAssembler defaultServicesVisibleIn( Visibility visibility )
    {
        defaultServicesVisibility = visibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ServiceDeclaration service = module.services( MemoryEntityStoreService.class ).visibleIn( visibility() );
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        module.defaultServices().visibleIn( defaultServicesVisibility() );
    }

    private Visibility defaultServicesVisibility()
    {
        return defaultServicesVisibility != null
               ? defaultServicesVisibility
               : visibility();
    }
}
