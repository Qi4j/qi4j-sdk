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
package org.apache.polygene.runtime.unitofwork;

import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class UnitOfWorkFactoryTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( AccountComposite.class,
                         OrderComposite.class,
                         ProductEntity.class,
                         CustomerComposite.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testUnitOfWork()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        // Create product
        EntityBuilder<ProductEntity> cb = unitOfWork.newEntityBuilder( ProductEntity.class );
        cb.instance().name().set( "Chair" );
        cb.instance().price().set( 57 );
        Product chair = cb.newInstance();

        String actual = chair.name().get();
        assertThat( "Chair.name()", actual, equalTo( "Chair" ) );
        assertThat( "Chair.price()", chair.price().get(), equalTo( 57 ) );

        unitOfWork.complete();
    }

    @Mixins( { AccountMixin.class } )
    public interface AccountComposite
        extends Account, EntityComposite
    {
    }

    public interface Account
    {
        Property<Integer> balance();

        void add( int amount );

        void remove( int amount );
    }

    public static abstract class AccountMixin
        implements Account
    {
        public void add( int amount )
        {
            balance().set( balance().get() + amount );
        }

        public void remove( int amount )
        {
            balance().set( balance().get() - amount );
        }
    }

    public interface Customer
    {
        Association<Account> account();

        Property<String> name();
    }

    public interface CustomerComposite
        extends Customer, EntityComposite
    {
    }

    public interface LineItem
    {
        Association<Product> product();
    }

    public interface LineItemComposite
        extends LineItem, EntityComposite
    {
    }

    public interface Order
    {
        Association<Customer> customer();

        ManyAssociation<LineItem> lineItems();
    }

    public interface OrderComposite
        extends Order, EntityComposite
    {
    }

    public interface Product
    {
        @UseDefaults
        Property<String> name();

        @UseDefaults
        Property<Integer> price();
    }

    public interface ProductEntity
        extends Product, EntityComposite
    {
    }
}
