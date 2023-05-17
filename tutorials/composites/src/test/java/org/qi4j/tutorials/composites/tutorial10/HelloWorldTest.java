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
package org.qi4j.tutorials.composites.tutorial10;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class HelloWorldTest
{
    private SingletonAssembler assembly;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        assembly = new SingletonAssembler( module -> module.transients( HelloWorldComposite.class ));
    }

    @Test
    public void givenAssemblyWhenBuildInstanceAndSayThenReturnCorrectResult()
    {
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "Hello" );
            builder.prototypeFor( HelloWorldState.class ).name().set( "World" );
            HelloWorldComposite helloWorld = builder.newInstance();
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hello World" ) );
        }

        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "Hey" );
            builder.prototypeFor( HelloWorldState.class ).name().set( "Universe" );
            HelloWorldComposite helloWorld = builder.newInstance();
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hey Universe" ) );
        }
    }

    @Test
    public void givenAssemblyWhenSetInvalidPhraseThenThrowException()
    {
        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( null );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "" );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }

    @Test
    public void givenAssemblyWhenSetInvalidNameThenThrowException()
    {
        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).name().set( null );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).name().set( "" );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }
}
