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
package org.qi4j.test.indexing;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.model.Account;
import org.qi4j.test.model.Address;
import org.qi4j.test.model.Cat;
import org.qi4j.test.model.City;
import org.qi4j.test.model.Domain;
import org.qi4j.test.model.Female;
import org.qi4j.test.model.File;
import org.qi4j.test.model.Host;
import org.qi4j.test.model.Male;
import org.qi4j.test.model.Port;
import org.qi4j.test.model.Protocol;
import org.qi4j.test.model.QueryParam;
import org.qi4j.test.model.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract satisfiedBy with tests for any queries against Index/Query engines.
 */
public class AbstractAnyQueryTest extends AbstractQi4jTest
{
    protected UnitOfWork unitOfWork;

    @Override
    public void assemble( ModuleAssembly module )
    {
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.module );
        new EntityTestAssembler().assemble( module );
    }

    protected void assembleEntities( ModuleAssembly module, Visibility visibility )
    {
        module.entities( Male.class,
                         Female.class,
                         City.class,
                         Domain.class,
                         Account.class,
                         Cat.class ). visibleIn( visibility );
    }

    protected void assembleValues( ModuleAssembly module, Visibility visibility )
    {
        module.values( URL.class,
                       Address.class,
                       Protocol.class,
                       Host.class,
                       Port.class,
                       File.class,
                       QueryParam.class ).visibleIn( visibility );
    }

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        TestData.populate( module.instance() );

        this.unitOfWork = this.module.instance().unitOfWorkFactory().newUnitOfWork();
    }


    @Override
    @AfterEach
    public void tearDown()
    {
        if( this.unitOfWork != null )
        {
            this.unitOfWork.discard();
            this.unitOfWork = null;
        }
        super.tearDown();
    }
}
