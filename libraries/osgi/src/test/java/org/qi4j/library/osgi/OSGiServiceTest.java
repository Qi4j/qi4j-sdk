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
package org.qi4j.library.osgi;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OSGiServiceTest
    extends AbstractQi4jTest
{

    private BundleContext bundleContext;

    public OSGiServiceTest()
    {
        bundleContext = mock( BundleContext.class );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MyService.class ).setMetaInfo( bundleContext ).instantiateOnStartup();
    }

    @Test
    public void givenFelixFrameworkWhenStartingQi4jApplicationExpectServiceToBeRegisteredToOsgiBundleContext()
    {
        MyService service = serviceFinder.findService( MyService.class ).get();
        service.value().set( 15 );
        assertThat( service.value().get(), equalTo( 15 ) );
        String[] expectedClasses = new String[]
        {
            "org.qi4j.library.osgi.OSGiServiceTest$MyService",
            "org.qi4j.library.osgi.OSGiEnabledService",
            "org.qi4j.api.identity.HasIdentity",
            "org.qi4j.api.service.ServiceComposite",
            "org.qi4j.api.identity.HasIdentity",
            "org.qi4j.api.composite.Composite"
        };
        verify( bundleContext ).registerService( expectedClasses, service, null );

    }

    public interface MyService
        extends OSGiEnabledService
    {

        @UseDefaults
        Property<Integer> value();

    }

}
