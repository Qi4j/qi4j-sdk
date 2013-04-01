/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.regression.qi377;

import org.junit.Test;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class InterfaceCollisionWithRelatedReturnTypesTest
    extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( Employee.class, Company.class );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheCompany()
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create new startup" ) );

        try
        {
            Company startUp = uow.newEntity( Company.class );
            Employee niclas = uow.newEntity( Employee.class );

            // To which team is Niclas added? Seems to be the interface listed first in the interface declaration?
            // This contrived example is probably just bad design...
            startUp.employees().add( niclas );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheSalesTeam()
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create new startup" ) );

        try
        {
            SalesTeam startUp = uow.newEntity( SalesTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.employees().add( niclas );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheResearchTeam()
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create new startup" ) );

        try
        {
            ResearchTeam startUp = uow.newEntity( ResearchTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.employees().add( niclas );
        }
        finally
        {
            uow.discard();
        }
    }

    public interface Employee {}

    public interface SalesTeam
    {
        ManyAssociation<Employee> employees();
    }

    public interface ResearchTeam
    {
        ManyAssociation<Employee> employees();
    }

    /**
     * This compiles, unlike the example in {@link InterfaceCollisionWithUnrelatedReturnTypesTest}.
     */
    public interface Company extends SalesTeam, ResearchTeam {}
}
