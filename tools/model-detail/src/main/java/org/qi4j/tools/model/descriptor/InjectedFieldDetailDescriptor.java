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
package org.qi4j.tools.model.descriptor;

import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.qi4j.api.composite.InjectedFieldDescriptor;

public final class InjectedFieldDetailDescriptor
{
    private final InjectedFieldDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;

    InjectedFieldDetailDescriptor( InjectedFieldDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "InjectedFieldDescriptor" );
        this.descriptor = descriptor;
    }

    /**
     * @return Descriptor of this {@code InjectedFieldDetailDescriptor}. Never returns {@code null}.
     */
    public final InjectedFieldDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Activator that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method concern that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    /**
     * @return Method side effect that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ObjectDetailDescriptor" );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MixinDetailDescriptor" );
        mixin = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MethodConcernDetailDescriptor" );
        methodConcern = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MethodSideEffectDetailDescriptor" );
        methodSideEffect = descriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.field().getName();
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "name", descriptor().field().getName() );
        builder.add( "type", descriptor().field().getType().getName() );
        return builder;
    }
}
