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
package org.qi4j.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/** JUNIT 5 Extension for running Qi4j unit tests.
 *
 * This will create a Singleton Application only, i.e. one layer with one module.
 */
public class Qi4jUnitExtension
    implements Extension, BeforeTestExecutionCallback, AfterTestExecutionCallback
{
    private final Assembler assembler;
    private Application application;

    public static Qi4jUnitExtensionBuilder forModule( Assembler assembler )
    {
        return new Qi4jUnitExtensionBuilder( assembler);
    }

    private Qi4jUnitExtension( Assembler assembler )
    {

        this.assembler = assembler;
    }

    static void setField( Field f, Object injectable, ExtensionContext context )
    {
        try
        {
            f.setAccessible( true );
            Optional<Object> possibleInstance = context.getTestInstance();
            if( possibleInstance.isPresent() )
            {
                f.set( possibleInstance.get(), injectable );
            }
            else
            {
                if( Modifier.isStatic( f.getModifiers() ) )
                {
                    f.set( null, injectable );
                }
            }
        }
        catch( IllegalAccessException e )
        {
            throw new UndeclaredThrowableException( e );
        }
    }

    @Override
    public void beforeTestExecution( ExtensionContext context )
        throws Exception
    {
        SingletonAssembler app = new SingletonAssembler( assembler )
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws Exception
            {
                super.assemble( module );
                module.objects( context.getRequiredTestClass() );
            }
        };
        app.module().objectFactory().injectTo( context.getRequiredTestInstance() );
        application = app.application();
    }

    @Override
    public void afterTestExecution( ExtensionContext context )
        throws Exception
    {
        application.passivate();
    }

    public static class Qi4jUnitExtensionBuilder
    {
        private final Assembler assembler;

        public Qi4jUnitExtensionBuilder( Assembler assembler )
        {
            this.assembler = assembler;
        }

        public Qi4jUnitExtension build()
        {
            return new Qi4jUnitExtension( assembler );
        }
    }
}
