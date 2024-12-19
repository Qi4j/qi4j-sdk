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
package org.qi4j.cache.memcache;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import org.qi4j.spi.cache.Cache;

/**
 * Memcache Implementation.
 * Use Java Serialization under the hood.
 * @param <T> Parameterized Type of cached entries
 */
/* package */ class MemcacheImpl<T>
    implements Cache<T>
{
    private static final AtomicInteger INSTANCES = new AtomicInteger();
    private final MemcachedClient client;
    private final String cacheId;
    private final String cachePrefix;
    private final Class<T> valueType;
    private final int expiration;
    private int refCount;

    /* package */ MemcacheImpl( MemcachedClient client, String cacheId, Class<T> valueType, int expiration )
    {
        this.client = client;
        this.cacheId = cacheId;
        this.cachePrefix = cacheId + "." + INSTANCES.incrementAndGet() + ".";
        this.valueType = valueType;
        this.expiration = expiration;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    public T get( String key )
    {
        try
        {
            Object value = client.get( prefix( key ), new SerializingTranscoder() );
            client.touch( prefix( key ), expiration );
            if( value == null )
            {
                return null;
            }
            return valueType.cast( value );
        }
        // TODO: Work out how to handle this gracefully. Is returning null enough? Should probably disable caching for a while and try again later.
        catch (TimeoutException e)
        {
            return null;
        }
        catch (InterruptedException e)
        {
            return null;
        }
        catch (MemcachedException e)
        {
            return null;
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    public T remove( String key )
    {
        try
        {
            String prefixedKey = prefix( key );
            Object old = client.get( prefixedKey, new SerializingTranscoder() );
            if( old != null )
            {
                client.delete( prefixedKey );
            }
            return valueType.cast( old );
        }
        // TODO: Work out how to handle this gracefully. Is returning null enough? Should probably disable caching for a while and try again later.
        catch (TimeoutException e)
        {
            return null;
        }
        catch (InterruptedException e)
        {
            return null;
        }
        catch (MemcachedException e)
        {
            return null;
        }
    }

    @SuppressWarnings({"TryWithIdenticalCatches", "CatchMayIgnoreException"})
    @Override
    public void put( String key, T value )
    {
        try
        {
            client.set( prefix( key ), expiration, value, new SerializingTranscoder() );
        }
        // TODO: Work out how to handle this gracefully.
        catch (TimeoutException e)
        {
        }
        catch (InterruptedException e)
        {
        }
        catch (MemcachedException e)
        {
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    public boolean exists( String key )
    {
        try
        {
            return client.get( prefix( key ) ) != null;
        }
        // TODO: Work out how to handle this gracefully.
        catch (TimeoutException e)
        {
            return false;
        }
        catch (InterruptedException e)
        {
            return false;
        }
        catch (MemcachedException e)
        {
            return false;
        }
    }

    private String prefix( String key )
    {
        return cachePrefix + key;
    }

    synchronized void decRefCount()
    {
        refCount--;
    }

    synchronized void incRefCount()
    {
        refCount++;
    }

    synchronized boolean isNotUsed()
    {
        return refCount == 0;
    }

    public String cacheId()
    {
        return cacheId;
    }
}
