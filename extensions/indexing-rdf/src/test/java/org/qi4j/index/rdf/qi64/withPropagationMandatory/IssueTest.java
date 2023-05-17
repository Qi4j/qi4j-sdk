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
package org.qi4j.index.rdf.qi64.withPropagationMandatory;

import org.qi4j.api.identity.Identity;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.qi64.AbstractIssueTest;
import org.qi4j.index.rdf.qi64.AccountComposite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class IssueTest
    extends AbstractIssueTest
{
    private AccountService accountService;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();

        accountService = serviceFinder.findService( AccountService.class ).get();
    }

    @Test
    public final void testUnitOfWorkWithUnitOfWorkNotInitialized()
        throws Throwable
    {
        assertThrows( IllegalStateException.class, () -> {
            // Bootstrap the account
            Identity id = newQi4jAccount();

            // Make sure there's no unit of work
            assertThat( unitOfWorkFactory.currentUnitOfWork(), nullValue() );

            accountService.getAccountById( id );
        } );
    }

    @Test
    public final void testUnitOfWorkWithUnitOfWorkInitialized()
        throws Throwable
    {
        // Bootstrap the account
        Identity id = newQi4jAccount();

        // Make sure there's no unit of work
        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );

        UnitOfWork parentUnitOfWork = unitOfWorkFactory.newUnitOfWork();

        AccountComposite account = accountService.getAccountById( id );
        assertThat( account, notNullValue() );

        UnitOfWork currentUnitOfWork = unitOfWorkFactory.currentUnitOfWork();
        assertThat( currentUnitOfWork, equalTo( parentUnitOfWork ) );

        assertThat( currentUnitOfWork.isOpen(), is( true ) );

        // Close the parent unit of work
        parentUnitOfWork.complete();
    }

    protected final void onAssemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.services( AccountServiceComposite.class );
    }
}
