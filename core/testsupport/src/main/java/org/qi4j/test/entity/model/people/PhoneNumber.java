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
package org.qi4j.test.entity.model.people;

import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

public interface PhoneNumber extends HasIdentity
{
    Property<Integer> countryCode();

    Property<Integer> areaCode();

    Property<String> number();

    class Builder
    {
        private final ValueBuilder<PhoneNumber> valueBuilder;
        private final PhoneNumber prototype;

        public Builder( @Structure ValueBuilderFactory vbf )
        {
            valueBuilder = vbf.newValueBuilder( PhoneNumber.class );
            prototype = valueBuilder.prototype();
        }

        PhoneNumber create( int countryCode, int areaCode, String number )
        {
            prototype.countryCode().set( countryCode );
            prototype.areaCode().set( areaCode );
            prototype.number().set( number );
            return valueBuilder.newInstance();
        }
    }
}
