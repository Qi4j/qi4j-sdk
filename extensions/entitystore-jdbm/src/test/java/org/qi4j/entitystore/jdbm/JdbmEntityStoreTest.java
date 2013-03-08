/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.jdbm;

import java.io.File;
import org.junit.After;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class JdbmEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.services( FileConfigurationService.class ).instantiateOnStartup();
        new JdbmEntityStoreAssembler( Visibility.module ).assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        config.entities( JdbmConfiguration.class ).visibleIn( Visibility.layer );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
    }

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        File dbFile = new File( "qi4j.data.db" );
        boolean success = true;
        if( dbFile.exists() )
        {
            success = dbFile.delete();
        }

        File logFile = new File( "qi4j.data.lg" );
        if( logFile.exists() )
        {
            success = success & logFile.delete();
        }
        if( !success )
        {
            throw new Exception( "Could not delete test data" );
        }
    }
}