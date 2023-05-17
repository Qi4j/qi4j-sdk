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

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityTypeException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.common.Visibility.application;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JAVADOC
 */
public class PrivateEntityUnitOfWorkTest
{
    private static final Identity TEST_IDENTITY = StringIdentity.identityOf( "1" );

    @Structure
    private UnitOfWorkFactory uowf;

    @Test
    public void givenAppWithPrivateEntityWhenUnitOfWorkCanSeeItThenCanCommit()
        throws Exception
    {
        System.setProperty( "qi4j.compacttrace", "off" );

        Energy4Java qi4j = new Energy4Java();
        Application app = qi4j.newApplication(
            applicationFactory ->
                applicationFactory.newApplicationAssembly( new Assembler[][][]{
                    {
                        {
                            module -> {
                                module.objects( PrivateEntityUnitOfWorkTest.class );
                            }
                        }
                    },
                    {
                        {
                            module -> {
                                module.entities( ProductEntity.class );
                                module.entities( ProductCatalogEntity.class ).visibleIn( application );
                                module.values( ProductInfo.class );

                                new EntityTestAssembler().visibleIn( Visibility.module )
                                                         .defaultServicesVisibleIn( Visibility.application )
                                                         .assemble( module );
                            }
                        }
                    }
                } ) );
        app.activate();

        Module module = app.findModule( "Layer 1", "Module 1" );
        module.injectTo( this );

        UnitOfWork unitOfWork = uowf.newUnitOfWork();

        try
        {
            unitOfWork.newEntity( ProductEntity.class );
            fail( "Should not be able to create product here" );
        }
        catch( NoSuchEntityTypeException e )
        {
            // Ok
            ProductCatalog catalog = unitOfWork.newEntity( ProductCatalog.class, TEST_IDENTITY);
            unitOfWork.complete();
        }
        unitOfWork = uowf.newUnitOfWork();

        Identity id;
        try
        {
            ProductCatalog catalog = unitOfWork.get( ProductCatalog.class, TEST_IDENTITY);
            id = catalog.newProduct().identity().get();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }

        unitOfWork = uowf.newUnitOfWork();
        try
        {
            ProductCatalog catalog = unitOfWork.get( ProductCatalog.class, TEST_IDENTITY);
            Product product = catalog.findProduct( id );
            product.price().set( 100 );
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    public interface ProductCatalog
    {
        Product newProduct();

        Product findProduct( Identity id );
    }

    @Mixins( ProductCatalogEntity.ProductRepositoryMixin.class )
    interface ProductCatalogEntity
        extends ProductCatalog, EntityComposite
    {
        abstract class ProductRepositoryMixin
            implements ProductCatalog
        {
            @Structure
            private UnitOfWorkFactory uowf;

            @Structure
            private ValueBuilderFactory vbf;

            public Product newProduct()
            {
                ValueBuilder<ProductInfo> vb = vbf.newValueBuilder( ProductInfo.class );
                vb.prototype().description().set( "Some mundane description" );
                vb.prototype().weight().set( 1.0f );
                ProductInfo info = vb.newInstance();

                UnitOfWork uow = uowf.currentUnitOfWork();
                EntityBuilder<Product> eb = uow.newEntityBuilder( Product.class );
                eb.instance().name().set( "Product Name" );
                eb.instance().price().set( 100 );
                eb.instance().productInfo().set( info );
                return eb.newInstance();
            }

            public Product findProduct( Identity id )
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                return uow.get( Product.class,  id );
            }
        }
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

    public interface ProductInfo
        extends ValueComposite
    {
        Property<String> description();

        Property<Float> weight();
    }

    public interface Product extends HasIdentity
    {
        Property<String> name();

        Property<Integer> price();

        Property<ProductInfo> productInfo();
    }

    public interface ProductEntity
        extends Product, EntityComposite
    {
    }
}
