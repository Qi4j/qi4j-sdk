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
package org.qi4j.test.entity.model.monetary;

import java.math.BigDecimal;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

public interface Currency
{
    Property<BigDecimal> amount();
    Property<String> name();

    @Mixins( Currency.BuilderImpl.class)
    interface Builder
    {
        Currency create( int amount, String currencyName );
        Currency create( BigDecimal amount, String currencyName );
    }

    class BuilderImpl
        implements Builder
    {
        @Structure
        private ValueBuilderFactory vbf;

        public Currency create( int amount, String currencyName )
        {
            return create( new BigDecimal( amount ), currencyName );
        }

        public Currency create( BigDecimal amount, String currencyName )
        {
            ValueBuilder<Currency> builder = vbf.newValueBuilder( Currency.class );
            builder.prototype().name().set( currencyName );
            builder.prototype().amount().set( amount );
            return builder.newInstance();
        }
    }
}
