/*
 * Copyright 2009 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.indexing.model;

import java.util.Collection;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * JAVADOC Add JavaDoc.
 */
public interface URL
    extends ValueComposite
{
    Property<Protocol> protocol();

    @Optional
    @Queryable( false )
    Property<Host> host();

    @Optional
    Property<Port> port();

    @Optional
    Property<File> file();

    @Optional
    Property<Collection<QueryParam>> queryParams();
}
