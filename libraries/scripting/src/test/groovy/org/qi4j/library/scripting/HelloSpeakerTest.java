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
package org.qi4j.library.scripting;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HelloSpeakerTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Test
    public void testGroovyScriptResourceMixin()
        throws Exception
    {
        // START SNIPPET: script
        SingletonAssembler assembler = new SingletonAssembler(
            assembly -> assembly.values( HelloSpeaker.class )
                                .setMetaInfo( Scripting.GROOVY )
                                .withMixins( ScriptMixin.class )
        );
        HelloSpeaker speaker = assembler.module().newValue( HelloSpeaker.class );
        assertThat( speaker.sayHello(), equalTo("Hello, Groovy") );
        // END SNIPPET: script
    }

    @Test
    public void testGroovyClassMixin()
        throws Exception
    {
        // START SNIPPET: direct
        SingletonAssembler assembler = new SingletonAssembler(
            assembly -> assembly.transients( HelloSpeaker.class )
                                .withMixins( HelloSpeakerMixin.class )
        );
        HelloSpeaker speaker = assembler.module().newTransient( HelloSpeaker.class );
        assertThat( speaker.sayHello(), equalTo("Hello there, Groovy") );
        // END SNIPPET: direct
    }
}
