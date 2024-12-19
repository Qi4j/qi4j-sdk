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

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.command.KestrelCommandFactory;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import net.rubyeye.xmemcached.utils.Protocol;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.spi.cache.Cache;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Memcache CachePool Mixin.
 */
public class MemcachePoolMixin
    implements MemcachePoolService
{
    private final Map<String, MemcacheImpl<?>> caches = new HashMap<>();

    @This
    private Configuration<MemcacheConfiguration> configuration;

    private MemcachedClient client;
    private int expiration;

    @Override
    public void activateService()
        throws Exception
    {
        MemcacheConfiguration config = configuration.get();
        expiration = config.expiration().get();
        String addresses = config.addresses().get();
        Protocol protocol = Protocol.valueOf(config.protocol().get());
        String username = config.username().get();
        String password = config.password().get();
        String authMech = config.authMechanism().get();

        long opTimeout;
        try
        {
            opTimeout = (config.opTimeout().get() == null)
                ? 2500
                : Long.parseLong(config.opTimeout().get());
        }
        catch (NumberFormatException e)
        {
            opTimeout = 2500;
        }

        List<InetSocketAddress> addressList = AddrUtil.getAddresses(addresses);
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(addressList);
        builder.setOpTimeout(opTimeout);
        if (username != null && !username.isEmpty())
        {
            switch (authMech)
            {
                case "PLAIN":
                    addressList.forEach(addr -> builder.addAuthInfo(addr, AuthInfo.plain(username, password)));
                    break;
                case "CRAM-MD5":
                    addressList.forEach(addr -> builder.addAuthInfo(addr, AuthInfo.cramMD5(username, password)));
                    break;
            }
        }
        if(protocol.equals(Protocol.Binary))
        {
            builder.setCommandFactory(new BinaryCommandFactory());
        }
        if(protocol.equals(Protocol.Text))
        {
            builder.setCommandFactory(new TextCommandFactory());
        }
        if(protocol.equals(Protocol.Kestrel))
        {
            builder.setCommandFactory(new KestrelCommandFactory());
        }
        MemcachedClient client = builder.build();
        client.flushAll();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        if (client != null)
        {
            client.shutdown();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Cache<T> fetchCache(String cacheId, Class<T> valueType)
    {
        Objects.requireNonNull(cacheId, "cacheId");
        if (cacheId.isEmpty())
        {
            throw new IllegalArgumentException("cacheId was empty string");
        }
        synchronized (caches)
        {
            MemcacheImpl<?> cache = caches.computeIfAbsent(cacheId, identity -> new MemcacheImpl<>(client, identity, valueType, expiration));
            cache.incRefCount();
            return (Cache<T>) cache;
        }
    }

    @Override
    public void returnCache(Cache<?> cache)
    {
        MemcacheImpl<?> memcache = (MemcacheImpl<?>) cache;
        memcache.decRefCount();
        synchronized (caches)
        {
            if (memcache.isNotUsed())
            {
                caches.remove(memcache.cacheId());
            }
        }
    }
}
