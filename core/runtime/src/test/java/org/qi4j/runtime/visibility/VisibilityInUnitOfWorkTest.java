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
package org.qi4j.runtime.visibility;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

public class VisibilityInUnitOfWorkTest
{

    public static final Identity TEST_IDENTITY1 = StringIdentity.identityOf( "123" );
    public static final Identity TEST_IDENTITY2 = StringIdentity.identityOf( "345" );

    @Test
    public void givenTwoModulesWithServiceAndEntityInOneAndEntityInOtherWhenOtherEntityAccessServiceWhichUsesItsEntityExpectServiceToHaveVisibility()
        throws Exception
    {
        Application underTest = createApplication();
        Module module = underTest.findModule( "layer1", "My Module" );
        ServiceReference<MyService> service = module.findService( MyService.class );
        service.get().create();
    }

    @Mixins( YourService.Mixin.class )
    public interface YourService
    {
        void create();

        YourEntity get();

        class Mixin
            implements YourService
        {
            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                YourEntity entity = uow.newEntity( YourEntity.class, TEST_IDENTITY2);
            }

            @Override
            public YourEntity get()
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                return uow.get( YourEntity.class, TEST_IDENTITY2);
            }
        }
    }

    public interface YourEntity
    {
    }

    @Mixins( MyEntity.Mixin.class )
    public interface MyEntity
    {
        void logic();

        class Mixin
            implements MyEntity
        {
            @Service
            private YourService service;

            @Override
            public void logic()
            {
                YourEntity result = service.get();
            }
        }
    }

    @Mixins( MyService.Mixin.class )
    public interface MyService
    {
        void create();

        class Mixin
            implements MyService
        {

            @Service
            private YourService service;

            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
            {
                try (UnitOfWork uow = uowf.newUnitOfWork())
                {
                    uow.newEntity( MyEntity.class, TEST_IDENTITY1 );
                    MyEntity entity1 = uow.get( MyEntity.class, TEST_IDENTITY1 );
                    service.create();
                    YourEntity entity2 = service.get();
                }
            }
        }
    }

    private Application createApplication()
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();
        return qi4j.newApplication( appFactory -> {
            ApplicationAssembly appAssembly = appFactory.newApplicationAssembly();
            LayerAssembly layer1 = appAssembly.layer( "layer1" );
            ModuleAssembly myModule = layer1.module( "My Module" );
            ModuleAssembly yourModule = layer1.module( "Your Module" );
            ModuleAssembly infraModule = layer1.module( "Infra Module" );
            myModule.services( MyService.class );
            myModule.entities( MyEntity.class );
            yourModule.entities( YourEntity.class );
            yourModule.services( YourService.class ).visibleIn( Visibility.layer );
            new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( infraModule );
            return appAssembly;
        } );
    }
}
