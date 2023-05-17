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
package org.qi4j.runtime.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

public class ValueNestedBuilderTest
    extends AbstractQi4jTest
{
    private interface InnerValue
        extends ValueComposite
    {
        Property<List<String>> listProp();

        Property<Map<String, String>> mapProp();
    }

    private interface InnerDefaultedValue
        extends ValueComposite
    {

        @UseDefaults
        Property<List<String>> listPropDefault();

        @UseDefaults
        Property<Map<String, String>> mapPropDefault();
    }

    private interface OuterValue
        extends ValueComposite
    {

        Property<List<InnerValue>> innerListProp();
    }

    private interface OuterDefaultedValue
        extends ValueComposite
    {

        @UseDefaults
        Property<List<InnerDefaultedValue>> innerListPropDefault();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( InnerValue.class, InnerDefaultedValue.class, OuterValue.class, OuterDefaultedValue.class );

        module.defaultServices();
    }

    @Test
    public void testInner()
    {
        ValueBuilder<InnerValue> innerBuilder = valueBuilderFactory.newValueBuilder( InnerValue.class );
        InnerValue inner = innerBuilder.prototype();
        inner.listProp().set( new ArrayList<String>() );
        inner.mapProp().set( new HashMap<String, String>() );
        inner = innerBuilder.newInstance();
        // If we reach this point, value creation went well
    }

    @Test
    public void testOuter()
    {
        ValueBuilder<InnerValue> innerBuilder = valueBuilderFactory.newValueBuilder( InnerValue.class );
        InnerValue inner = innerBuilder.prototype();
        inner.listProp().set( new ArrayList<String>() );
        inner.mapProp().set( new HashMap<String, String>() );
        inner = innerBuilder.newInstance();
        ValueBuilder<OuterValue> outerBuilder = valueBuilderFactory.newValueBuilder( OuterValue.class );
        OuterValue outer = outerBuilder.prototype();
        List<InnerValue> inners = new ArrayList<InnerValue>();
        inners.add( inner );
        outer.innerListProp().set( inners );
        outer = outerBuilder.newInstance();
        System.out.println( outer.toString() );
        // If we reach this point, value creation went well
    }

    @Test
    public void testDefaultedInner()
    {
        ValueBuilder<InnerDefaultedValue> innerBuilder = valueBuilderFactory.newValueBuilder( InnerDefaultedValue.class );
        InnerDefaultedValue inner = innerBuilder.newInstance();
        // If we reach this point, value creation went well
    }

    @Test
    public void testDefaultedOuter()
    {
        ValueBuilder<InnerDefaultedValue> innerBuilder = valueBuilderFactory.newValueBuilder( InnerDefaultedValue.class );
        InnerDefaultedValue inner = innerBuilder.newInstance();
        ValueBuilder<OuterDefaultedValue> outerBuilder = valueBuilderFactory.newValueBuilder( OuterDefaultedValue.class );
        OuterDefaultedValue outer = outerBuilder.prototype();
        List<InnerDefaultedValue> inners = new ArrayList<InnerDefaultedValue>();
        inners.add( inner );
        outer.innerListPropDefault().set( inners );
        outer = outerBuilder.newInstance();
        System.out.println( outer.toString() );
        // If we reach this point, value creation went well
    }
}
