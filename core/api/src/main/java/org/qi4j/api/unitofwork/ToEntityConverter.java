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
package org.qi4j.api.unitofwork;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.association.AssociationDescriptor;

/**
 * MetaInfo holder for value-to-entity conversion in {@link UnitOfWork#toEntity(Class, HasIdentity)}.
 * <p>
 * The implementation of this interface should be registered as metaInfo on the {@link Usecase}
 * of the {@link UnitOfWork} where the conversion should take place. It is also possible to register
 * the implementation to the {@link UnitOfWork}'s metaInfo.
 * </p>
 * <p>Example;</p>
 * <pre><code>
 *     private static final Usecase USECASE_GET_USER_DETAILS = UseCaseBuilder
 *                                                                 .buildUseCase("get user details")
 *                                                                 .withMetaInfo( new UserToEntityConverter() )
 *                                                                 .newUsecase();
 *
 *     &#64;Structure
 *     private UnitOfWorkFactory uowf;
 *     :
 *     :
 *     try( UnitOfWork uow = uowf.newUnitOfWork( USECASE_GET_USER_DETAILS ) )
 *     {
 *         :
 *         User value = ...;
 *         User entity = uow.toEntity( User.class, value );
 *         :
 *     }
 *     :
 *     :
 * </code></pre>
 */
public interface ToEntityConverter
{
    /**
     * Returns the Function to convert each of the properties of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @param defaultFn       The default converter function. This can be used to delegate non-special cases, or simply
     *                        return to do all the conversions
     * @return The function to do the conversion. It MUST NOT return null, and if no conversion is wanted, return the defaultFn.
     */
    Function<PropertyDescriptor, Object> properties( Object entityComposite,
                                                     Function<PropertyDescriptor, Object> defaultFn );

    /**
     * Returns the Function to convert each of the associations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @param defaultFn       The default converter function. This can be used to delegate non-special cases, or simply
     *                        return to do all the conversions
     * @return The function to do the conversion. It MUST NOT return null, and if no conversion is wanted, return the defaultFn.
     */
    Function<AssociationDescriptor, EntityReference> associations(Object entityComposite,
                                                                  Function<AssociationDescriptor, EntityReference> defaultFn );

    /**
     * Returns the Function to convert each of the manyAssociations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @param defaultFn       The default converter function. This can be used to delegate non-special cases, or simply
     *                        return to do all the conversions
     * @return The function to do the conversion. It MUST NOT return null, and if no conversion is wanted, return the defaultFn.
     */
    Function<AssociationDescriptor, Stream<EntityReference>> manyAssociations( Object entityComposite,
                                                                               Function<AssociationDescriptor, Stream<EntityReference>> defaultFn );

    /**
     * Returns the Function to convert each of the NamedAssociations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @param defaultFn       The default converter function. This can be used to delegate non-special cases, or simply
     *                        return to do all the conversions
     * @return The function to do the conversion. It MUST NOT return null, and if no conversion is wanted, return the defaultFn.
     */
    Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociations( Object entityComposite,
                                                                                                   Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> defaultFn );
}
