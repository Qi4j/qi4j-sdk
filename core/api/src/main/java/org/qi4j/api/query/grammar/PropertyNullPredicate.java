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
package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Property null Specification.
 */
public class PropertyNullPredicate<T>
    extends ExpressionPredicate
{
    private PropertyFunction<T> property;

    public PropertyNullPredicate( PropertyFunction<T> property )
    {
        this.property = property;
    }

    public PropertyFunction<T> property()
    {
        return property;
    }

    @Override
    public boolean test( Composite item )
    {
        Property<T> prop = property.apply( item );

        if( prop == null )
        {
            return true;
        }

        return prop.get() == null;
    }

    @Override
    public String toString()
    {
        return property.toString() + "is null";
    }
}
