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
package org.qi4j.index.sql.postgresql;

import org.junit.jupiter.api.BeforeEach;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.indexing.AbstractEntityFinderTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class PostgreSQLEntityFinderTest
    extends AbstractEntityFinderTest
{
    @Container
    public static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres:17-alpine")
        .withDatabaseName("jdbc_test_db")
        .withReuse(true);

    @Override
    public void assemble( ModuleAssembly mainModule )
        throws AssemblyException
    {
        SQLTestHelper.sleep();
        super.assemble( mainModule );
        SQLTestHelper.assembleWithMemoryEntityStore( mainModule, postgres.getHost(), postgres.getFirstMappedPort(), postgres.getUsername(), postgres.getPassword());
    }

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        try
        {
            super.setUp();
        }
        catch( Exception e )
        {
            // Let's check if exception was because database was not available
            if( this.module != null )
            {
                SQLTestHelper.setUpTest( this.serviceFinder );
            }

            // If we got this far, the database must have been available, and exception must have
            // had other reason!
            throw e;
        }
    }
}
