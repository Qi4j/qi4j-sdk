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

package org.qi4j.runtime.unitofwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;

/**
 * Default implementation of ManyAssociationState that also
 * keeps a list of changes that can be extracted at any time.
 */
public final class BuilderManyAssociationState
    implements ManyAssociationState
{
    private List<EntityReference> references = new ArrayList<>();

    @Override
    public int count()
    {
        return references.size();
    }

    @Override
    public boolean contains( EntityReference entityReference )
    {
        return references.contains( entityReference );
    }

    @Override
    public boolean add( int i, EntityReference entityReference )
    {
        if( references.contains( entityReference ) )
        {
            return false;
        }

        references.add( i, entityReference );
        return true;
    }

    @Override
    public boolean remove( EntityReference entityReference )
    {
        return references.remove( entityReference );
    }

    @Override
    public boolean clear()
    {
        if( !references.isEmpty() )
        {
            references.clear();
            return true;
        }
        return false;
    }

    @Override
    public EntityReference get( int i )
    {
        return references.get( i );
    }

    @Override
    public Iterator<EntityReference> iterator()
    {
        return references.iterator();
    }
}
