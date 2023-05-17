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
package org.qi4j.api.composite;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.injection.scope.Uses;

/**
 * TransientBuilders are used to instantiate TransientComposites. They can be acquired from
 * {@link TransientBuilderFactory#newTransientBuilder(Class)} and allows the client
 * to provide additional settings before instantiating the TransientComposite.
 *
 * @param <T> Transient type
 */
public interface TransientBuilder<T>
{
    /**
     * Provide objects that can be injected into mixins that has the @Uses
     * dependency injection annotation.
     *
     * @param usedObjects The objects that can be injected into mixins.
     *
     * @return the transient builder instance
     *
     * @see Uses
     */
    TransientBuilder<T> use( Object... usedObjects );

    /**
     * Get a representation of the state for the new Composite.
     * It is possible to access and update properties and associations,
     * even immutable ones since the builder represents the initial state.
     *
     * @return a proxy implementing the Composite type
     */
    T prototype();

    /**
     * Get a representation of the state of the given type for the new Composite.
     * This is primarily used if you want to provide state for a private mixin type.
     *
     * @param <K> Mixin type
     * @param mixinType the mixin which you want to provide state for
     *
     * @return a proxy implementing the given mixin type
     */
    <K> K prototypeFor( Class<K> mixinType );

    /**
     * Create a new Composite instance.
     *
     * @return a new Composite instance
     *
     * @throws ConstructionException thrown if it was not possible to instantiate the Composite
     */
    T newInstance()
        throws ConstructionException;
}
