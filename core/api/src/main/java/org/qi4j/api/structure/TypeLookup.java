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

package org.qi4j.api.structure;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.value.ValueDescriptor;

public interface TypeLookup
{
    /**
     * Lookup first Object Model matching the given Type.
     *
     * <p>First, if Object Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Object Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Object Model
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    ObjectDescriptor lookupObjectModel( Class<?> type ) throws AmbiguousTypeException;

    /**
     * Lookup first Transient Model matching the given Type.
     *
     * <p>First, if Transient Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Transient Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Transient Model
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    TransientDescriptor lookupTransientModel( Class<?> type ) throws AmbiguousTypeException;

    /**
     * Lookup first Value Model matching the given Type.
     *
     * <p>First, if Value Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Value Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Value Model
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    ValueDescriptor lookupValueModel( Class<?> type ) throws AmbiguousTypeException;

    /**
     * Lookup first Entity Model matching the given Type.
     *
     * <p>First, if Entity Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Entity Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for creational use cases only.</b> For non-creational use cases see
     * {@link #lookupEntityModels(Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Entity Model
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    EntityDescriptor lookupEntityModel( Class<?> type ) throws AmbiguousTypeException;

    /**
     * Lookup all Entity Models matching the given Type.
     *
     * <p>Returned List contains, in order, Entity Models that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     * <p>Multiple <b>assignable</b> matches are <b>allowed</b> to enable polymorphic fetches and queries.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for non-creational use cases only.</b> For creational use cases see
     * {@link #lookupEntityModel(Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return All matching Entity Models
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    List<EntityDescriptor> lookupEntityModels( Class<?> type ) throws AmbiguousTypeException;

    /**
     * Lookup first ServiceDescriptor/ImportedServiceDescriptor matching the given Type.
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p>See {@link #lookupServiceModels(Type)}.</p>
     *
     * @param serviceType Looked up Type
     *
     * @return First matching Service
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    ModelDescriptor lookupServiceModel( Type serviceType ) throws AmbiguousTypeException;

    /**
     * Lookup all ServiceDescriptors matching the given Type.
     *
     * <p>Returned List contains, in order, ServiceReferences that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>allowed</b> to enable polymorphic lookup/injection.</p>
     * <p>Multiple <b>assignable</b> matches with the same Visibility are <b>allowed</b> for the very same reason.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return All matching ServiceReferences
     * @throws AmbiguousTypeException when a type ambiguity is found
     */
    List<? extends ModelDescriptor> lookupServiceModels( Type type ) throws AmbiguousTypeException;

    /**
     * @return All visible Objects, in visibility order
     */
    Stream<ObjectDescriptor> allObjects();

    /**
     * @return All visible Transients, in visibility order
     */
    Stream<TransientDescriptor> allTransients();

    /**
     * @return All visible Values, in visibility order
     */
    Stream<ValueDescriptor> allValues();

    /**
     * @return All visible Entities, in visibility order
     */
    Stream<EntityDescriptor> allEntities();

    /**
     * @return All visible Services, in visibility order
     */
    Stream<? extends ModelDescriptor> allServices();
}
