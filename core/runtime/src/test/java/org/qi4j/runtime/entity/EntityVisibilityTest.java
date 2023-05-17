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

package org.qi4j.runtime.entity;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityTypeException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.*;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityVisibilityTest
{

    public static final Identity TEST_IDENTITY = StringIdentity.identityOf( "123" );

    private Energy4Java qi4j;
    private Module module;
    private Application app;
    private UnitOfWorkFactory uowf;

    @BeforeEach
    public void setup()
        throws Exception
    {
        qi4j = new Energy4Java();

        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer Above
                  {
                      new AboveAssembler()
                  }
                },
                { // Layer From
                  { // From Module
                    new FromAssembler(),
                  },
                  { // Beside Module
                    new BesideAssembler()
                  }
                },
                { // Layer Below
                  {
                      new BelowAssembler()
                  }
                }
            };
        app = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        module = app.findModule( "From Layer", "From" );
        uowf = module.unitOfWorkFactory();
    }

    @AfterEach
    public void tearDown()
        throws Exception
    {
        app.passivate();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleLayerVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleModuleVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.besideApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.besideLayerVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.belowApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveModuleVisible();
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleModuleVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.besideApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.besideLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.besideModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.belowApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.belowLayerVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.belowModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveApplicationVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveLayerVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromValueWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleLayerVisible();
    }

    @Test
    public void givenFromValueWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleModuleVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.besideApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.besideLayerVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.belowApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleLayerVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleModuleVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.besideApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.besideLayerVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.belowApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleLayerVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleModuleVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.besideApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.besideLayerVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.belowApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchEntityTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveModuleVisible();
        } );
    }

    private static class FromAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "From Layer" );
            module.setName( "From" );
            module.services( FromService.class );
            module.entities( FromEntity.class );
            module.transients( FromTransient.class );
            module.values( FromValue.class );
            module.objects( FromObject.class );

            module.entities( ModuleApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( ModuleLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( ModuleModuleVisible.class ).visibleIn( Visibility.module );
        }
    }

    private static class BelowAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "Below Layer" );
            module.setName( "Below" );
            module.entities( BelowApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( BelowLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( BelowModuleVisible.class ).visibleIn( Visibility.module );

            new EntityTestAssembler().visibleIn( Visibility.application ).assemble( module );
        }
    }

    private static class AboveAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "Above Layer" );
            module.setName( "Above" );
            module.entities( AboveApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( AboveLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( AboveModuleVisible.class ).visibleIn( Visibility.module );
        }
    }

    private static class BesideAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Beside" );
            module.entities( BesideApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( BesideLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( BesideModuleVisible.class ).visibleIn( Visibility.module );
        }
    }

    @Mixins( Mixin.class )
    public interface From
    {
        void moduleApplicationVisible();

        void moduleLayerVisible();

        void moduleModuleVisible();

        void besideApplicationVisible();

        void besideLayerVisible();

        void besideModuleVisible();

        void belowApplicationVisible();

        void belowLayerVisible();

        void belowModuleVisible();

        void aboveApplicationVisible();

        void aboveLayerVisible();

        void aboveModuleVisible();
    }

    public interface FromValue extends From, ValueComposite
    {
    }

    public interface FromEntity extends From, EntityComposite
    {
    }

    public interface FromService extends From, ServiceComposite
    {
    }

    public interface FromTransient extends From, TransientComposite
    {
    }

    public static class FromObject extends Mixin
    {
    }

    public abstract static class Mixin
        implements From
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void moduleApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleApplicationVisible entity = uow.newEntity( ModuleApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void moduleLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleLayerVisible entity = uow.newEntity( ModuleLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void moduleModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleModuleVisible entity = uow.newEntity( ModuleModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideApplicationVisible entity = uow.newEntity( BesideApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideLayerVisible entity = uow.newEntity( BesideLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideModuleVisible entity = uow.newEntity( BesideModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowApplicationVisible entity = uow.newEntity( BelowApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowLayerVisible entity = uow.newEntity( BelowLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowModuleVisible entity = uow.newEntity( BelowModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveApplicationVisible entity = uow.newEntity( AboveApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveLayerVisible entity = uow.newEntity( AboveLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveModuleVisible entity = uow.newEntity( AboveModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }
    }

    public interface ModuleApplicationVisible extends EntityComposite
    {
    }

    public interface ModuleLayerVisible extends EntityComposite
    {
    }

    public interface ModuleModuleVisible extends EntityComposite
    {
    }

    public interface BesideApplicationVisible extends EntityComposite
    {
    }

    public interface BesideLayerVisible extends EntityComposite
    {
    }

    public interface BesideModuleVisible extends EntityComposite
    {
    }

    public interface BelowApplicationVisible extends EntityComposite
    {
    }

    public interface BelowLayerVisible extends EntityComposite
    {
    }

    public interface BelowModuleVisible extends EntityComposite
    {
    }

    public interface AboveApplicationVisible extends EntityComposite
    {
    }

    public interface AboveLayerVisible extends EntityComposite
    {
    }

    public interface AboveModuleVisible extends EntityComposite
    {
    }
}
