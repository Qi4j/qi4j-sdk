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

package org.qi4j.library.rdf.repository;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * JAVADOC
 */
public class MemoryRepositoryTest
    extends AbstractQi4jTest
{
    @Service
    Repository repository;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.services( MemoryRepositoryService.class ).instantiateOnStartup();
        module.objects( getClass() );
    }

    @Test
    public void testMemoryRepository() throws RepositoryException
    {
        RepositoryConnection conn = repository.getConnection();
        assertThat( "repository is open", conn.isOpen(), CoreMatchers.equalTo( true ) );
        conn.close();
    }
}
