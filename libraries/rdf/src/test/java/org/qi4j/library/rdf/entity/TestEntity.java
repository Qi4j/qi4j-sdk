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

package org.qi4j.library.rdf.entity;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;
import org.qi4j.library.constraints.annotation.NotEmpty;

/**
 * JAVADOC
 */
interface TestEntity
    extends EntityComposite
{
    @NotEmpty
    Property<String> name();

    @NotEmpty Property<String> title();

    @Optional Association<TestEntity> association();

    Property<TestValue> value();

    ManyAssociation<TestEntity> manyAssoc();

    ManyAssociation<TestEntity> group();
}
