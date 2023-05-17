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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.index.rdf.qi64.AccountComposite;

import static org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.MANDATORY;
import static org.qi4j.index.rdf.qi64.withPropagationMandatory.AccountService.AccountServiceMixin;

@Mixins( AccountServiceMixin.class )
public interface AccountService
{
    @UnitOfWorkPropagation( MANDATORY )
    AccountComposite getAccountById( Identity anId );

    public class AccountServiceMixin
        implements AccountService
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Module module;

        public AccountComposite getAccountById( Identity anId )
        {
            // Use current unit of work
            UnitOfWork work = uowf.currentUnitOfWork();

            AccountComposite account = work.get( AccountComposite.class, anId );

            if( account != null )
            {
                // Required to get around QI-66 bug
                account.name().get();
            }

            return account;
        }
    }
}
