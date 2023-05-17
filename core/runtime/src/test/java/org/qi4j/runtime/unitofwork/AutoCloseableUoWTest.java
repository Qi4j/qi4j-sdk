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
package org.qi4j.runtime.unitofwork;

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Assert that Automatic Resource Management (ie. Java 7 try-with-resources) work on UoWs.
 */
public class AutoCloseableUoWTest
    extends AbstractQi4jTest
{

    public interface TestEntity
    {

        Property<String> mandatory();

    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
    }

    @Test
    public void givenGoodAutoCloseableUoWWhenTryWithResourceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = uow.newEntityBuilder( TestEntity.class );
            builder.instance().mandatory().set( "Mandatory property" );
            builder.newInstance();
            uow.complete();
        }
    }

    @Test
    public void givenWrongAutoCloseableUoWWhenTryWithResourceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        assertThrows( ConstraintViolationException.class, () -> {
            try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
            {
                uow.newEntity( TestEntity.class );
                uow.complete();
            }
        } );
    }

    @AfterEach
    public void afterEachTest()
    {
        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );
    }

}
