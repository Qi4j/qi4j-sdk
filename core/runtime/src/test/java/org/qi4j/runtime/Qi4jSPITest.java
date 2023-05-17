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

package org.qi4j.runtime;

import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * JAVADOC
 */
public class Qi4jSPITest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class, TestEntity2.class );
    }

    @Test
    public void givenEntityWhenGettingStateThenGetCorrectState()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity testEntity;
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();

            AssociationStateHolder state = spi.stateOf( testEntity );

            validateState( state, spi.entityDescriptorFor( testEntity ) );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            testEntity = uow.get( testEntity );
            validateState( spi.stateOf( testEntity ), spi.entityDescriptorFor( testEntity ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    private void validateState( AssociationStateHolder state, EntityDescriptor entityDescriptor )
    {
        entityDescriptor.state().properties().forEach( propertyDescriptor -> {
            Property<?> prop = state.propertyFor( propertyDescriptor.accessor() );
            assertThat( "Properties could be listed", prop, CoreMatchers.notNullValue() );
        } );

        AssociationStateDescriptor descriptor = entityDescriptor.state();
        descriptor.associations().forEach( associationDescriptor -> {
            AbstractAssociation assoc = state.associationFor( associationDescriptor.accessor() );
            assertThat( "Assocs could be listed", assoc, CoreMatchers.notNullValue() );
        } );
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Optional
        Association<TestEntity> association();

        ManyAssociation<TestEntity> manyAssociation();
    }

    public interface TestEntity2
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Optional
        Association<TestEntity> association();

        ManyAssociation<TestEntity> manyAssociation();
    }
}
