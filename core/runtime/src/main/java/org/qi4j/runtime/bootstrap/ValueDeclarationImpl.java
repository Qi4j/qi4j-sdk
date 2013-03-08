/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ValueDeclaration;

import static java.util.Arrays.asList;

/**
 * Declaration of a ValueComposite.
 */
public final class ValueDeclarationImpl
    implements ValueDeclaration
{
    private Iterable<ValueAssemblyImpl> assemblies;

    public ValueDeclarationImpl( Iterable<ValueAssemblyImpl> assemblies )
    {
        this.assemblies = assemblies;
    }

    @Override
    public ValueDeclaration setMetaInfo( Object info )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.metaInfo.set( info );
        }
        return this;
    }

    @Override
    public ValueDeclaration visibleIn( Visibility visibility )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.visibility = visibility;
        }
        return this;
    }

    @Override
    public ValueDeclaration withConcerns( Class<?>... concerns )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.concerns.addAll( asList( concerns ) );
        }
        return this;
    }

    @Override
    public ValueDeclaration withSideEffects( Class<?>... sideEffects )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.sideEffects.addAll( asList( sideEffects ) );
        }
        return this;
    }

    @Override
    public ValueDeclaration withMixins( Class<?>... mixins )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.mixins.addAll( asList( mixins ) );
        }
        return this;
    }

    @Override
    public ValueDeclaration withTypes( Class<?>... types )
    {
        for( ValueAssemblyImpl assembly : assemblies )
        {
            assembly.types.addAll( asList( types ) );
        }
        return this;
    }
}