/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.association;

/**
 * Association to a single EntityComposite.
 */
public interface Association<T> extends AbstractAssociation
{
    /**
     * Get the associated entity.
     *
     * @return the associated entity
     */
    T get();

    /**
     * Set the associated entity.
     *
     * @param associated the entity
     *
     * @throws IllegalArgumentException thrown if the entity is not a valid reference for this association
     * @throws IllegalStateException    thrown if association is immutable
     */
    void set( T associated )
        throws IllegalArgumentException, IllegalStateException;
}
