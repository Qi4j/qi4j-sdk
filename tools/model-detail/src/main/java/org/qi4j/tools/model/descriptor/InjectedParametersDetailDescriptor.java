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
import org.qi4j.api.composite.InjectedParametersDescriptor;

public class InjectedParametersDetailDescriptor
{
    private final InjectedParametersDescriptor descriptor;
    private ConstructorDetailDescriptor constructor;
    private InjectedMethodDetailDescriptor method;

    InjectedParametersDetailDescriptor( InjectedParametersDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code InjectedParametersDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final InjectedParametersDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructor that owns this {@code InjectedParametersDetailDescriptor}.
     *         If {@code null}, this {@code InjectedParametersDetailDescriptor} is owned by a method.
     *
     * @see #method()
     * @since 0.5
     */
    public final ConstructorDetailDescriptor constructor()
    {
        return constructor;
    }

    /**
     * @return Method that owns this {@code InjectedParametersDetailDescriptor}.
     *         If {@code null}, this {@code InjectedParametersDetailDescriptor} is owned by a constructor.
     *
     * @see #constructor() ()
     * @since 0.5
     */
    public final InjectedMethodDetailDescriptor method()
    {
        return method;
    }

    final void setConstructor( ConstructorDetailDescriptor constructorDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( constructorDescriptor, "constructorDescriptor" );
        constructor = constructorDescriptor;
    }

    final void setMethod( InjectedMethodDetailDescriptor methodDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( methodDescriptor, "methodDescriptor" );
        method = methodDescriptor;
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if( method != null )
        {
            builder.add( "method", method().toJson() );
        }
        if( constructor != null )
        {
            builder.add( "constructor", constructor().toJson() );
        }
        return builder;
    }

}
