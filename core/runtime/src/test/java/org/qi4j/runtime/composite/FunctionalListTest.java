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
package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class FunctionalListTest extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( List.class ).withTypes( FList.class ).withMixins( ArrayList.class );
    }

    @Test
    public void givenArrayListWithMapOpCapabilityWhenMappingIntegerToStringExpectCorrectResult()
    {
        List<Integer> integers = transientBuilderFactory.newTransient( List.class );
        integers.add( 5 );
        integers.add( 15 );
        integers.add( 45 );
        integers.add( 85 );
        FList<Integer> list = (FList<Integer>) integers;

        List<String> strings = list.translate( new Function<Integer, String>()
        {
            @Override
            public String apply( Integer x )
            {
                return x.toString();
            }
        } );

        String[] expected = new String[]
        {
            "5", "15", "45", "85"
        };
        assertThat( strings, hasItems( expected ) );
    }

    @Mixins( FListMixin.class )
    public interface FList<FROM>
    {
        <TO> List<TO> translate( Function<FROM, TO> function );
    }

    public static class FListMixin<FROM>
        implements FList<FROM>
    {
        @This
        private List<FROM> list;

        @Structure
        private Module module;

        @Override
        public <TO> List<TO> translate( Function<FROM, TO> function )
        {
            List<TO> result = module.newTransient( List.class );
            for( FROM data : list )
            {
                result.add( function.apply( data ) );
            }
            return result;
        }
    }
}
