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
package org.qi4j.api.sideeffect;

import org.qi4j.api.sideeffect.internal.SideEffectFor;
import org.qi4j.api.sideeffect.internal.SideEffectFor;

/**
 * Base class for SideEffects. It introduces a typed "next" pointer
 * that SideEffects can use to get the result of the original invocation.
 * <p>
 * Generic SideEffects should subclass {@link GenericSideEffect} instead.
 * </p>
 * <p>
 * SideEffects implementations must be thread-safe in their implementation,
 * as multiple threads may share instances.
 * </p>
 */
public abstract class SideEffectOf<T>
{
    final
    @SideEffectFor
    protected T result = null;
}
