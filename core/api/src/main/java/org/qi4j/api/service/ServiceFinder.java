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

package org.qi4j.api.service;

import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * Interface used to query for ServiceReferences.
 * <p>
 * Each ServiceFinder is
 * obtained from a specific Module, and the lookup rules are the following:
 * </p>
 * <ol>
 * <li>First look in the same Module as the ServiceFinder</li>
 * <li>Then look in the same Layer as the ServiceFinder. Any Services declared
 * with Visibility Layer and Application should be included</li>
 * <li>Then look in the used Layers. Any Services declared with Visibility Application
 * should be included</li>
 * </ol>
 * <p>
 * Both native Qi4j services and imported services are considered, with preference to native services.
 * </p>
 */
public interface ServiceFinder
{
    /**
     * Find a ServiceReference that implements the given type.
     *
     * @param <T> Service type
     * @param serviceType the type that the Service must implement
     *
     * @return a ServiceReference if one is found
     *
     * @throws NoSuchServiceTypeException if no service of serviceType is found
     */
    <T> ServiceReference<T> findService( Class<T> serviceType )
        throws NoSuchServiceTypeException;

    /**
     * Find a ServiceReference that implements the given type.
     *
     * @param <T> Service type
     * @param serviceType the type that the Service must implement
     *
     * @return a ServiceReference if one is found
     *
     * @throws NoSuchServiceTypeException if no service of serviceType is found
     */
    <T> ServiceReference<T> findService( Type serviceType )
        throws NoSuchServiceTypeException;

    /**
     * Find ServiceReferences that implements the given type.
     * <p>
     * The order of the references is such that Services more local to the querying
     * Module is earlier in the list.
     * </p>
     *
     * @param <T> Service type
     * @param serviceType the type that the Services must implement
     *
     * @return a stream of ServiceReferences for the given type. It is empty if none exist
     */
    <T> Stream<ServiceReference<T>> findServices( Class<T> serviceType );

    /**
     * Find ServiceReferences that implements the given type.
     * <p>
     * The order of the references is such that Services more local to the querying
     * Module is earlier in the list.
     * </p>
     *
     * @param <T> Service type
     * @param serviceType the type that the Services must implement
     *
     * @return a stream of ServiceReferences for the given type. It is empty if none exist
     */
    <T> Stream<ServiceReference<T>> findServices( Type serviceType );
}
