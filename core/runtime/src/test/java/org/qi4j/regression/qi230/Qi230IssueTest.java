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

package org.qi4j.regression.qi230;

import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * JAVADOC
 */
public class Qi230IssueTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( Some.class ).withMixins( NoopMixin.class ).withConcerns( OtherConcern.class );
//        module.services( Some.class );
        module.services( Result.class );
    }

    @Test
    public void whenDerefencingInsideConcernThisExpectItToWork()
        throws Exception
    {
        Result result = serviceFinder.findService( Result.class ).get();
        Some some = serviceFinder.findService( Some.class ).get();
        assertThat( some.method(), equalTo( "method()" ) );
        assertThat( result.some().identity(), equalTo( some.identity() ) );
        assertThat( result.some().identity().get(), equalTo( some.identity().get() ) );
    }

    @Mixins( ResultMixin.class )
    public interface Result
        extends ServiceComposite
    {
        void execute( Some value );

        Some some();
    }

    public static abstract class ResultMixin
        implements Result
    {

        private Some value;

        public void execute( Some value )
        {
            this.value = value;
        }

        public Some some()
        {
            return value;
        }
    }

    @Concerns( OtherConcern.class )
    @Mixins( NoopMixin.class )
    public interface Other
    {
        void other();
    }

    @Mixins( SomeMixin.class )
    public interface Some
        extends ServiceComposite
//        extends ServiceComposite, Other
    {
        String method();
    }

    public abstract static class SomeMixin
        implements Some
    {
        @This
        private Other other;

        public String method()
        {
            other.other();
            return "method()";
        }
    }

    public static class OtherConcern
        extends ConcernOf<Other>
        implements Other
    {
        @Structure
        private Qi4jAPI api;

        @This
        private Composite me;

        @Service
        private Result result;

        public void other()
        {
            Composite value = api.dereference( me );
            result.execute( (Some) value );
            next.other();
        }
    }
}
