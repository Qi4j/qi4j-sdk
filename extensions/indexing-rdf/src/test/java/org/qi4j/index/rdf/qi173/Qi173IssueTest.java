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
package org.qi4j.index.rdf.qi173;

import java.util.Iterator;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class Qi173IssueTest
        extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( CarEntity.class );
        new RdfMemoryStoreAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testPersistence()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            createCar( "Volvo", "S80", 2007 );
            createCar( "Volvo", "C70", 2006 );
            createCar( "Ford", "Transit", 2007 );
            createCar( "Ford", "Mustang", 2007 );
            createCar( "Ford", "Mustang", 2006 );
            createCar( "Ford", "Mustang", 2005 );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // Can not happen.
            e.printStackTrace();
        }
        catch( UnitOfWorkCompletionException e )
        {
            e.printStackTrace();
        }

        uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<Car> qb = queryBuilderFactory.newQueryBuilder( Car.class );
        Car template = QueryExpressions.templateFor( Car.class );
        qb = qb.where( QueryExpressions.eq( template.year(), 2007 ) );

        Query<Car> query = uow.newQuery( qb );
        query.orderBy( orderBy( template.manufacturer() ), orderBy( template.model() ) );
        Iterator<Car> cars = query.iterator();
        assertThat( cars.hasNext(), is( true ) );
        Car car1 = cars.next();
        assertThat( "Ford", equalTo( car1.manufacturer().get() ) );
        assertThat( "Mustang", equalTo( car1.model().get() ) );
        assertThat( 2007, equalTo( (int) car1.year().get() ) );
        Car car2 = cars.next();
        assertThat( "Ford", equalTo( car2.manufacturer().get() ) );
        assertThat( "Transit", equalTo( car2.model().get() ) );
        assertThat( 2007, equalTo( (int) car2.year().get() ) );
        Car car3 = cars.next();
        assertThat( "Volvo", equalTo( car3.manufacturer().get() ) );
        assertThat( "S80", equalTo( car3.model().get() ) );
        assertThat( 2007, equalTo( (int) car3.year().get() ) );
        for( Car car : query )
        {
            System.out.println( car.manufacturer().get() + " " + car.model().get() + ", " + car.year().get() );
        }

        uow.discard();
    }

    private Identity createCar(String manufacturer, String model, int year )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );
        Car prototype = builder.instanceFor( Car.class );
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );
        prototype.year().set( year );
        CarEntity entity = (CarEntity) builder.newInstance();
        return entity.identity().get();
    }

    public interface CarEntity
        extends Car, EntityComposite
    {
    }

    public static interface Car
    {
        Property<String> manufacturer();

        Property<String> model();

        Property<Integer> year();
    }
}
