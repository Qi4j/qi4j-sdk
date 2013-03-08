/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.common.table;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * JAVADOC
 */
public interface TableResponse
   extends ValueComposite
{
   @Optional
   Property<String> version();

   @Optional
   Property<String> reqId();

   Property<String> status();

   @Optional
   Property<List<Problem>> warnings();

   @Optional
   Property<List<Problem>> errors();

   @Optional
   Property<String> sig();

   @Optional
   Property<Table> table();
}
