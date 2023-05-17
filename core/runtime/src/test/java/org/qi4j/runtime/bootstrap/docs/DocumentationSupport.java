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

package org.qi4j.runtime.bootstrap.docs;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.composite.FragmentClassLoader;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Energy4Java;

public class DocumentationSupport
{
    // START SNIPPET: customAssemblyHelper
    private static Energy4Java qi4j;

    private static Application application;

    public static void main( String[] args )
        throws Exception
    {
        // Create a Qi4j Runtime
        qi4j = new Energy4Java();
        // Create the application
        application = qi4j.newApplication( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();
            assembly.setMetaInfo( new DalvikAssemblyHelper() );
            // END SNIPPET: customAssemblyHelper
            // START SNIPPET: customAssemblyHelper
            return assembly;
        } );
        // Activate the application
        application.activate();
    }

    public static class DalvikAssemblyHelper extends AssemblyHelper
    {
        @Override
        protected FragmentClassLoader instantiateFragmentClassLoader( ClassLoader parent )
        {
            return new DalvikFragmentClassLoader( parent );
        }
    }

    public static class DalvikFragmentClassLoader extends FragmentClassLoader
    {

        public DalvikFragmentClassLoader( ClassLoader parent )
        {
            super( parent );
        }
    }
    // END SNIPPET: customAssemblyHelper
}
