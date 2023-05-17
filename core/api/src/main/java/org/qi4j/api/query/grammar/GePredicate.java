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

/**
 * Greater or equals Specification.
 */
public class GePredicate<T>
    extends ComparisonPredicate<T>
{
    public GePredicate( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected boolean compare( T value )
    {
        return ( (Comparable) value ).compareTo( this.value ) >= 0;
    }

    @Override
    public String toString()
    {
        return property.toString() + ">=" + value.toString();
    }
}
