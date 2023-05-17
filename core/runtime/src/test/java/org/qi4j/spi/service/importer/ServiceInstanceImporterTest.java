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

package org.qi4j.spi.service.importer;

import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * JAVADOC
 */
public class ServiceInstanceImporterTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importedServices( TestService.class ).
            identifiedBy( "test" ).
            setMetaInfo( StringIdentity.identityOf( "testimporter" ) ).
            importedBy( ImportedServiceDeclaration.SERVICE_IMPORTER );
        module.services( TestImporterService.class ).identifiedBy( "testimporter" );

        module.objects( ServiceInstanceImporterTest.class );
    }

    @Service
    TestService service;

    @Test
    public void testImportServiceFromService()
    {
        assertThat( service.helloWorld(), equalTo( "Hello World" ) );
    }

    public static class TestService
    {
        String helloWorld()
        {
            return "Hello World";
        }
    }

    @Mixins( TestImporterService.Mixin.class )
    interface TestImporterService
        extends ServiceComposite, ServiceImporter<TestService>
    {
        class Mixin
            implements ServiceImporter<TestService>
        {
            public TestService importService( ImportedServiceDescriptor serviceDescriptor )
                throws ServiceImporterException
            {
                return new TestService();
            }

            public boolean isAvailable( TestService instance )
            {
                return true;
            }
        }
    }
}
