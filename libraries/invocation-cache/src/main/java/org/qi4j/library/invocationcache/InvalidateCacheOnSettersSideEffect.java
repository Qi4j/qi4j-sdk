/*
 * Copyright 2007 Rickard Öberg.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.invocationcache;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;

/**
 * Invalidate cache on setters.
 */
@AppliesTo( InvalidateCacheOnSettersSideEffect.AppliesTo.class )
public class InvalidateCacheOnSettersSideEffect
    extends GenericSideEffect
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            return !( method.getDeclaringClass().equals( InvocationCache.class )
                      || method.getDeclaringClass().equals( InvocationCacheMixin.class ) );

        }
    }

    @This
    private InvocationCache cache;

    @Override
    protected void invoke( Method method, Object[] args )
    {
        cache.clearCachedValues();
    }
}
