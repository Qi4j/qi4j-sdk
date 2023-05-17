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

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;

/**
 * ManyAssociation Contains Specification.
 */
public class ManyAssociationContainsPredicate<T>
    extends ExpressionPredicate
{
    private final ManyAssociationFunction<T> manyAssociationFunction;
    private final T value;

    public ManyAssociationContainsPredicate( ManyAssociationFunction<T> manyAssociationFunction, T value )
    {
        this.manyAssociationFunction = manyAssociationFunction;
        this.value = value;
    }

    public ManyAssociationFunction<T> manyAssociation()
    {
        return manyAssociationFunction;
    }

    public T value()
    {
        return value;
    }

    @Override
    public boolean test( Composite item )
    {
        ManyAssociation<T> collection = manyAssociationFunction.apply( item );
        if( collection == null )
        {
            return false;
        }
        return collection.contains( value );
    }

    @Override
    public String toString()
    {
        return manyAssociationFunction + " contains:" + value;
    }
}
