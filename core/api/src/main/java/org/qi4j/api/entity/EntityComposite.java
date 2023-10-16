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
package org.qi4j.api.entity;

import org.qi4j.api.association.AssociationMixin;
import org.qi4j.api.association.ManyAssociationMixin;
import org.qi4j.api.association.NamedAssociationMixin;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.association.AssociationMixin;
import org.qi4j.api.association.ManyAssociationMixin;
import org.qi4j.api.association.NamedAssociationMixin;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.identity.HasIdentity;

/**
 * EntityComposites are Composites that has mutable state persisted in EntityStores and equality defined from its
 * reference.
 */
@Mixins( { AssociationMixin.class, ManyAssociationMixin.class, NamedAssociationMixin.class } )
public interface EntityComposite extends HasIdentity, Composite
{
}