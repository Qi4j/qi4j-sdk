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

package org.qi4j.runtime.association;

import org.qi4j.api.association.Association;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class AssociationAssignmentTest extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( TheAssociatedType.class );
        module.entities( TheMainType.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenAssignmentOfAssociationAtCreationWhenDereferencingAssocationExpectCorrectValue()
        throws Exception
    {
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        TheAssociatedType entity1 = work.newEntity( TheAssociatedType.class );
        EntityBuilder<TheMainType> builder = work.newEntityBuilder( TheMainType.class );
        builder.instance().assoc().set( entity1 );
        TheMainType entity2 = builder.newInstance();
        Identity id1 = entity1.identity().get();
        Identity id2 = entity2.identity().get();
        work.complete();
        assertThat(id1, notNullValue());
        assertThat(id2, notNullValue());

        work = unitOfWorkFactory.newUnitOfWork();
        TheMainType entity3 = work.get(TheMainType.class, id2 );
        TheAssociatedType entity4 = entity3.assoc().get();
        assertThat( entity4.identity().get(), equalTo(id1));
        work.discard();
    }

    interface TheAssociatedType extends EntityComposite
    {
    }

    interface TheMainType extends EntityComposite
    {
        Association<TheAssociatedType> assoc();
    }
}
