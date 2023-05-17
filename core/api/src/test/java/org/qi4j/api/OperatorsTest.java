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
package org.qi4j.api;

import java.util.Collections;
import java.util.function.Predicate;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * TODO
 */
public class OperatorsTest
{
    @Test
    public void testOperators()
        throws UnitOfWorkCompletionException, ActivationException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                new EntityTestAssembler().assemble( module );

                module.entities( TestEntity.class );
                module.values( TestValue.class );
                module.forMixin( TestEntity.class ).declareDefaults().foo().set( "Bar" );
                module.forMixin( TestValue.class ).declareDefaults().bar().set( "Xyz" );
            }
        );

        UnitOfWorkFactory uowf = assembler.module().unitOfWorkFactory();
        UnitOfWork uow = uowf.newUnitOfWork();

        try
        {
            EntityBuilder<TestEntity> entityBuilder = uow.newEntityBuilder( TestEntity.class, StringIdentity.identityOf( "123" ) );
            entityBuilder.instance().value().set( assembler.module().newValue( TestValue.class ) );
            TestEntity testEntity = entityBuilder.newInstance();

            uow.complete();
            uow = uowf.newUnitOfWork();

            Iterable<TestEntity> entities = Collections.singleton( testEntity = uow.get( testEntity ) );

            QueryBuilder<TestEntity> builder = assembler.module().newQueryBuilder( TestEntity.class );

            {
                Predicate<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .foo(), "Bar" );
                assertThat( where.test( testEntity ), is( true ) );
                System.out.println( where );
            }
            {
                Predicate<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .value()
                                                                          .get()
                                                                          .bar(), "Xyz" );
                assertThat( where.test( testEntity ), is( true ) );
                System.out.println( where );

                assertThat( builder.where( where ).newQuery( entities ).find().equals( testEntity ), is( true ) );
            }
        }
        finally
        {
            uow.discard();
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        Property<String> foo();

        Property<TestValue> value();
    }

    public interface TestValue
        extends ValueComposite
    {
        Property<String> bar();
    }
}
