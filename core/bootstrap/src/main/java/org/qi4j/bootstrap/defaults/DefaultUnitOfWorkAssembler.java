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

package org.qi4j.bootstrap.defaults;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class DefaultUnitOfWorkAssembler
    implements Assembler
{
    public static final String IDENTITY = "default-uow-factory";

    @Override
    public void assemble( ModuleAssembly module )
    {
        Class factoryMixin = loadMixinClass( "org.qi4j.runtime.unitofwork.UnitOfWorkFactoryMixin" );
        module.services( UnitOfWorkFactory.class )
              .withMixins( factoryMixin )
              .identifiedBy( IDENTITY );

        Class uowMixin = loadMixinClass( "org.qi4j.runtime.unitofwork.ModuleUnitOfWork" );
        module.transients( UnitOfWork.class )
              .withMixins( uowMixin );
    }

    private Class<?> loadMixinClass( String name )
    {
        try
        {
            return getClass().getClassLoader().loadClass( name );
        }
        catch( ClassNotFoundException e )
        {
            throw new AssemblyException( "Default UnitOfWorkFactory mixin is not present in the system." );
        }
    }
}
