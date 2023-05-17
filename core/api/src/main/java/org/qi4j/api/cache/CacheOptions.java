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
package org.qi4j.api.cache;

import org.qi4j.api.usecase.UsecaseBuilder;

/**
 * CacheOptions is a metaInfo class for the Cache system for Entity persistence.
 * <p>
 * CacheOptions should be assigned to the Usecase of the UnitOfWork, to give hint on caching to entity stores.
 * See {@link UsecaseBuilder} on how to set the metaInfo on Usecases.
 * </p>
 * <p>
 * Note that EntityStore implementations are not required to support caching.
 * </p>
 */
public final class CacheOptions
{
    public static final CacheOptions ALWAYS = new CacheOptions( true, true, true );
    public static final CacheOptions NEVER = new CacheOptions( false, false, false );

    private final boolean cacheOnRead;
    private final boolean cacheOnWrite;
    private final boolean cacheOnNew;

    /**
     * Constructor for CacheOptions.
     *
     * @param cacheOnRead  if true, give the hint to the Cache system that it may not be a good idea to cache the
     *                     read values. This is useful when it is known that the read will be over a large set and
     *                     shouldn't affect the existing cached entities. For instance, when traversing the EntityStore
     *                     this option is set to false.
     * @param cacheOnWrite if true, give the hint to the Cache system that it may not be a good idea to cache the
     *                     entity when the value is updated. If this is false, the cache should be emptied from any
     *                     cached entity instead of updated. There are few cases when this is useful, and if this is
     *                     false, it makes sense that the <i>cacheOnRead</i> is also false.
     * @param cacheOnNew   if true, give the hint to the Cache system that it may not be a good idea to cache a newly
     *                     created Entity, as it is not likely to be read in the near future. This is useful when
     *                     batch inserts are being made.
     */
    public CacheOptions( boolean cacheOnRead, boolean cacheOnWrite, boolean cacheOnNew )
    {
        this.cacheOnRead = cacheOnRead;
        this.cacheOnWrite = cacheOnWrite;
        this.cacheOnNew = cacheOnNew;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache the
     *         read values. This is useful when it is known that the read will be over a large set and
     *         shouldn't affect the existing cached entities. For instance, when traversing the EntityStore
     */
    public boolean cacheOnRead()
    {
        return cacheOnRead;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache the
     *         entity when the value is updated. If this is false, the cache should be emptied from any
     *         cached entity instead of updated. There are few cases when this is useful, and if this is
     *         false, it makes sense that the <i>cacheOnRead</i> is also false.
     */
    public boolean cacheOnWrite()
    {
        return cacheOnWrite;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache a newly
     *         created Entity, as it is not likely to be read in the near future. This is useful when
     *         batch inserts are being made.
     */
    public boolean cacheOnNew()
    {
        return cacheOnNew;
    }
}
