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
package org.qi4j.regression.qi383;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Qi383Test extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Car.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenUnitOfWorkInProgressWhenAddingSameEntityTwiceExpectException()
        throws UnitOfWorkCompletionException
    {
        assertThrows( EntityCompositeAlreadyExistsException.class, () -> {
            try (UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork())
            {
                unitOfWork.newEntity( Car.class, StringIdentity.identityOf( "Ferrari" ) );
                unitOfWork.newEntity( Car.class, StringIdentity.identityOf( "Ford" ) );
                unitOfWork.newEntity( Car.class, StringIdentity.identityOf( "Ferrari" ) );
                unitOfWork.complete();
            }
        } );
    }

    public interface Car extends EntityComposite
    {
    }
}
