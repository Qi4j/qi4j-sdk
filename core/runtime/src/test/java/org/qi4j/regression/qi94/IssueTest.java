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
package org.qi4j.regression.qi94;

import org.qi4j.api.association.Association;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class IssueTest
        extends AbstractQi4jTest
{
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.entities( Item.class, ItemType.class );
        new EntityTestAssembler().assemble( aModule );
    }

    @Test
    public void entityBuilderAssociationTypeIsNotNull()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Item> builder = uow.newEntityBuilder( Item.class );
            assertThat( qi4j.api()
                            .entityDescriptorFor( builder.instance() )
                            .state()
                            .getAssociationByName( "typeOfItem" )
                            .type(),
                        equalTo( ItemType.class )
            );
        }
        finally
        {
            uow.discard();
        }
    }

    interface Item
        extends EntityComposite
    {
        Association<ItemType> typeOfItem();
    }

    interface ItemType
        extends EntityComposite
    {
        Property<String> name();
    }
}
