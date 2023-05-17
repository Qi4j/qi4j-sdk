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
package org.qi4j.regression.qi382;

import org.qi4j.api.association.Association;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class Qi382Test extends AbstractQi4jTest
{

    public static final Identity FERRARI = StringIdentity.identityOf( "Ferrari" );
    public static final Identity NICLAS = StringIdentity.identityOf( "Niclas" );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Car.class, Person.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenCreationOfTwoEntitiesWhenAssigningOneToOtherExpectCompletionToSucceed()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            Car car = unitOfWork.newEntity( Car.class, FERRARI);
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            Car car = unitOfWork.get( Car.class, FERRARI);
            assertThat( car, notNullValue() );
            Person p = unitOfWork.get( Person.class, NICLAS);
            assertThat( p, notNullValue() );
            assertThat( p.car().get(), equalTo( car ) );
        }
    }

    @Mixins( Car.CarMixin.class )
    public interface Car extends EntityComposite, Lifecycle
    {

        class CarMixin implements Lifecycle
        {
            @This
            private Car me;

            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
            {
                UnitOfWork unitOfWork = uowf.currentUnitOfWork();
                EntityBuilder<Person> builder = unitOfWork.newEntityBuilder( Person.class, NICLAS);
                builder.instance().car().set( me );
                builder.newInstance();
            }

            @Override
            public void remove()
            {

            }
        }
    }

    public interface Person extends EntityComposite
    {
        Association<Car> car();
    }
}
