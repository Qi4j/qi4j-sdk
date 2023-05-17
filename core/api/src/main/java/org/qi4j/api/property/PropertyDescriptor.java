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

package org.qi4j.api.property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.structure.ModuleDescriptor;

/**
 * Property Descriptor.
 */
public interface PropertyDescriptor extends MetaInfoHolder
{
    /**
     * Get the qualified name of the property which is equal to:
     * <pre><code>
     * &lt;interface name&gt;:&lt;method name&gt;
     * </code></pre>
     *
     * @return the qualified name of the property
     */
    QualifiedName qualifiedName();

    /**
     * Get the type of the property. If the property is declared
     * as Property&lt;X&gt; then X is returned.
     *
     * @return the property type
     */
    Type type();

    AccessibleObject accessor();

    boolean isImmutable();

    boolean queryable();

    ValueType valueType();

    Object resolveInitialValue(ModuleDescriptor moduleDescriptor);
}
