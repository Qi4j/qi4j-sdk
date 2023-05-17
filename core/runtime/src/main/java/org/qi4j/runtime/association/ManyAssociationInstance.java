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
package org.qi4j.runtime.association;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.ManyAssociationWrapper;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.spi.entity.ManyAssociationState;

/**
 * JAVADOC
 */
public class ManyAssociationInstance<T> extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private ManyAssociationState manyAssociationState;

    public ManyAssociationInstance( AssociationInfo associationInfo,
                                    BiFunction<EntityReference, Type, Object> associationFunction,
                                    ManyAssociationState manyAssociationState
    )
    {
        super( associationInfo, associationFunction );
        this.manyAssociationState = manyAssociationState;
    }

    @Override
    public int count()
    {
        return manyAssociationState.count();
    }

    @Override
    public boolean contains( T entity )
    {
        return manyAssociationState.contains( getEntityReference( entity ) );
    }

    @Override
    public boolean add( int i, T entity )
    {
        Objects.requireNonNull( entity, "entity" );
        checkImmutable();
        checkType( entity );
        associationInfo.checkConstraints( entity );
        return manyAssociationState.add( i, EntityReference.create( ((HasIdentity) entity ).identity().get()) );
    }

    @Override
    public boolean add( T entity )
    {
        return add( manyAssociationState.count(), entity );
    }

    @Override
    public boolean remove( T entity )
    {
        Objects.requireNonNull( entity, "entity" );
        checkImmutable();
        checkType( entity );

        return manyAssociationState.remove( EntityReference.create( ((HasIdentity) entity).identity().get() ) );
    }

    @Override
    public boolean clear()
    {
        checkImmutable();
        return manyAssociationState.clear();
    }

    @Override
    public T get( int i )
    {
        return getEntity( manyAssociationState.get( i ) );
    }

    @Override
    public List<T> toList()
    {
        ArrayList<T> list = new ArrayList<>();
        for( EntityReference entityReference : manyAssociationState )
        {
            list.add( getEntity( entityReference ) );
        }

        return list;
    }

    @Override
    public Set<T> toSet()
    {
        Set<T> set = new HashSet<>();
        for( EntityReference entityReference : manyAssociationState )
        {
            set.add( getEntity( entityReference ) );
        }

        return set;
    }

    @Override
    public Stream<EntityReference> references()
    {
        return manyAssociationState.stream();
    }

    @Override
    public String toString()
    {
        return manyAssociationState.toString();
    }

    @Override
    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( manyAssociationState.iterator() );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ManyAssociation<?> that = (ManyAssociation) o;
        // Unwrap if needed
        while( that instanceof ManyAssociationWrapper )
        {
            that = ( (ManyAssociationWrapper) that ).next();
        }
        // Descriptor equality
        ManyAssociationInstance<?> thatInstance = (ManyAssociationInstance) that;
        AssociationDescriptor thatDescriptor = (AssociationDescriptor) thatInstance.associationInfo();
        if( !associationInfo.equals( thatDescriptor ) )
        {
            return false;
        }
        // State equality
        if( manyAssociationState.count() != thatInstance.manyAssociationState.count() )
        {
            return false;
        }
        for( EntityReference ref : manyAssociationState )
        {
            if( !thatInstance.manyAssociationState.contains( ref ) )
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = associationInfo.hashCode() * 31; // Descriptor
        for( EntityReference ref : manyAssociationState )
        {
            hash += ref.hashCode() * 7; // State
        }
        return hash;
    }

    public ManyAssociationState getManyAssociationState()
    {
        return manyAssociationState;
    }

    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private final Iterator<EntityReference> idIterator;

        ManyAssociationIterator( Iterator<EntityReference> idIterator )
        {
            this.idIterator = idIterator;
        }

        @Override
        public boolean hasNext()
        {
            return idIterator.hasNext();
        }

        @Override
        public T next()
        {
            return getEntity( idIterator.next() );
        }

        @Override
        public void remove()
        {
            checkImmutable();
            idIterator.remove();
        }
    }
}
