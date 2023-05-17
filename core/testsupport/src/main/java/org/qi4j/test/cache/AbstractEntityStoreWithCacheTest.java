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
package org.qi4j.test.cache;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.junit.jupiter.api.Test;
import org.qi4j.test.entity.AbstractEntityStoreTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert Cache behaviour when used by an EntityStore.
 * <p>
 * Use an in-memory CachePool by default, implement the <code>assembleCachePool</code> method to override.
 */
public abstract class AbstractEntityStoreWithCacheTest
    extends AbstractEntityStoreTest
{
    @Optional @Service MemoryCachePoolService cachePool;

    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        super.assemble( module );
        assembleCachePool( module );
    }

    protected void assembleCachePool( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MemoryCachePoolService.class );
    }

    @Test
    public void whenNewEntityThenCanFindEntityAndCorrectValues()
        throws Exception
    {
        super.whenNewEntityThenCanFindEntityAndCorrectValues();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 1 ) );
            assertThat( cache.puts(), is( 1 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        super.whenRemovedEntityThenCannotFindEntity();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 0 ) );
            assertThat( cache.gets(), is( 2 ) );
            assertThat( cache.puts(), is( 1 ) );
            assertThat( cache.removes(), is( 1 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState()
        throws UnitOfWorkCompletionException
    {
        super.givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 2 ) );
            assertThat( cache.puts(), is( 1 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        super.givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 2 ) );
            assertThat( cache.puts(), is( 2 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void givenManyAssociationIsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        super.givenAssociationsModifiedWhenUnitOfWorkCompletesThenStoreState();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 3 ) );
            assertThat( cache.puts(), is( 3 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 4 ) );
            assertThat( cache.puts(), is( 2 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }

    @Test
    public void givenEntityStoredLoadedChangedWhenUnitOfWorkDiscardsThenDontStoreState()
        throws UnitOfWorkCompletionException
    {
        super.givenEntityStoredLoadedChangedWhenUnitOfWorkDiscardsThenDontStoreState();
        if( cachePool != null )
        {
            MemoryCacheImpl<?> cache = cachePool.singleCache();
            assertThat( cache.size(), is( 1 ) );
            assertThat( cache.gets(), is( 2 ) );
            assertThat( cache.puts(), is( 1 ) );
            assertThat( cache.removes(), is( 0 ) );
            assertThat( cache.exists(), is( 0 ) );
        }
    }
}
