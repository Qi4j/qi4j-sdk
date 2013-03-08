/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Method instance pool that keeps a linked list. Uses atomic reference
 * to ensure that instances are acquired and returned in a thread-safe
 * manner.
 */
public final class AtomicInstancePool
    implements InstancePool<CompositeMethodInstance>
{
    private final AtomicReference<CompositeMethodInstance> first = new AtomicReference<CompositeMethodInstance>();

    @Override
    public CompositeMethodInstance obtainInstance()
    {
        CompositeMethodInstance firstInstance;
        do
        {
            firstInstance = first.get();
        }
        while( firstInstance != null && !first.compareAndSet( firstInstance, firstInstance.getNext() ) );

        return firstInstance;
    }

    @Override
    public void releaseInstance( CompositeMethodInstance compositeMethodInstance )
    {
        CompositeMethodInstance firstInstance;
        do
        {
            firstInstance = first.get();
            compositeMethodInstance.setNext( firstInstance );
        }
        while( !first.compareAndSet( firstInstance, compositeMethodInstance ) );
    }
}
