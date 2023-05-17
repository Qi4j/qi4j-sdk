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

package org.qi4j.bootstrap;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ServiceAssemblyTest extends AbstractQi4jTest
{
    @Test
    public void givenMyServiceWithTwoDeclarationsWhenActivatingServiceExpectServiceActivatedOnce()
        throws Exception
    {
        ServiceReference<MyService> ref = serviceFinder.findService( MyService.class );
        MyService underTest = ref.get();
        assertThat(underTest.activated(), equalTo(1));
        underTest.passivateService();
        assertThat(underTest.passivated(), equalTo(1));
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MyService.class ).instantiateOnStartup();
        module.services( MyService.class ).setMetaInfo( "Hello" );
    }

    @Mixins( MyServiceMixin.class )
    public static interface MyService extends ServiceActivation
    {
        int activated();
        int passivated();
    }

    public static class MyServiceMixin implements MyService, ServiceActivation
    {

        private int activated;
        private int passivated;

        @Override
        public int activated()
        {
            return activated;
        }

        @Override
        public int passivated()
        {
            return passivated;
        }

        @Override
        public void activateService()
            throws Exception
        {
            activated++;
        }

        @Override
        public void passivateService()
            throws Exception
        {
            passivated++;

        }
    }
}
