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
package org.qi4j.entitystore.hazelcast;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.hazelcast.assembly.HazelcastEntityStoreAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class HazelcastEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly configModule = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( configModule );
        // START SNIPPET: assembly
        new HazelcastEntityStoreAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
    }
    // END SNIPPET: assembly

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        super.tearDown();
        // TODO : delete test data
    }
}
