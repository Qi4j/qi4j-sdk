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

package org.qi4j.api.entity;

import java.util.Objects;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;

/**
 * An EntityReference is reference of a specific Entity instance.
 * <p>When stringified, the reference is used as-is. Example:</p>
 * <pre>123456-abcde</pre>
 */
public final class EntityReference
{
    /**
     * Parse an Entity reference string to an EntityReference.
     *
     * @param identityString the EntityReference reference
     * @return the EntityReference represented by the given reference
     */
    public static EntityReference parseEntityReference(String identityString)
    {
        Objects.requireNonNull( identityString, "identityString must not be null" );
        return new EntityReference( StringIdentity.identityOf( identityString ) );
    }

    /**
     * @param object an EntityComposite
     * @return the EntityReference for the given EntityComposite
     */
    public static EntityReference entityReferenceFor(Object object)
    {
        Objects.requireNonNull( object );
        if( object instanceof Identity)
        {
            return new EntityReference( ((Identity) object) );
        }
        if( object instanceof HasIdentity)
        {
            return new EntityReference( ((HasIdentity) object).identity().get() );
        }
        throw new IllegalArgumentException( "Can not get an entity reference for " + object.getClass() );
    }

    public static EntityReference create(Identity identity)
    {
        if (identity == null)
        {
            return null;
        }
        return new EntityReference(identity);
    }

    private Identity identity;

    /**
     * @param identity reference reference
     * @throws NullPointerException if reference is null
     */
    private EntityReference( Identity identity )
    {
        Objects.requireNonNull(identity,"reference must not be null");
        this.identity = identity;
    }

    /**
     *
     * @return The reference of the Entity that this EntityReference.is referring to
     */
    public Identity identity()
    {
        return identity;
    }

    /**
     * @return An URI representation of this EntityReference.
     */
    public String toURI()
    {
        return "urn:qi4j:entity:" + identity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        EntityReference that = (EntityReference) o;
        return identity.equals(that.identity);
    }

    @Override
    public int hashCode()
    {
        return identity.hashCode();
    }

    @Override
    public String toString()
    {
        return identity.toString();
    }
}
