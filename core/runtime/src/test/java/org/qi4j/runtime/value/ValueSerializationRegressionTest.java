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
package org.qi4j.runtime.value;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

public class ValueSerializationRegressionTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( SimpleEntity.class );
        module.entities( DualFaced.class );
        module.values( DualFaced.class );
        module.services( MemoryEntityStoreService.class );
    }

    @Test
    public void givenNewValueWhenConvertingToEntityExpectNewEntityInStore()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<DualFaced> builder = valueBuilderFactory.newValueBuilder( DualFaced.class );
        builder.prototype().identity().set( StringIdentity.identityOf( "1234" ) );
        builder.prototype().name().set( "Hedhman" );
        DualFaced value = builder.newInstance();
    }

    public interface SimpleEntity extends HasIdentity
    {
        Property<String> name();
    }

    public interface DualFaced extends HasIdentity
    {
        Property<String> name();

        Association<SimpleEntity> simple();

        ManyAssociation<SimpleEntity> simples();

        NamedAssociation<SimpleEntity> namedSimples();
    }
}

