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
package org.qi4j.api.unitofwork;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class ToValueConversionTest
        extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( SomeType.class );
        module.values( SomeType.class );
    }

    @Test
    public void testConversionToValue()
        throws Exception
    {
        Usecase usecase = UsecaseBuilder.buildUsecase( "test case" )
                                        .withMetaInfo( new SomeValueConverter() )
                                        .newUsecase();
        SomeType value;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork(usecase) )
        {
            SomeType entity1 = createEntity( uow, StringIdentity.identityOf( "Niclas" ) );
            SomeType entity2 = createEntity( uow, StringIdentity.identityOf( "Paul" ) );
            SomeType entity3 = createEntity( uow, StringIdentity.identityOf( "Jiri" ) );
            SomeType entity4 = createEntity( uow, StringIdentity.identityOf( "Kent" ) );
            SomeType entity5 = createEntity( uow, StringIdentity.identityOf( "Stan" ) );
            entity1.assoc().set( entity2 );
            entity1.many().add( entity3 );
            entity1.named().put( "kent", entity4 );
            entity1.named().put( "stan", entity5 );

            value = uow.toValue( SomeType.class, entity1 );
            uow.complete();

        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork(usecase) )
        {
            assertThat( value.identity().get(), equalTo( StringIdentity.identityOf( "Niclas" ) ) );
            assertThat( value.name().get(), equalTo( "[Niclas]" ) );

            assertThat( uow.toValue( SomeType.class, value.assoc().get()).name().get(), equalTo( "[Paul]" ));
            assertThat( uow.toValueList( value.many() ).get(0).name().get(), equalTo( "[Jiri]" ));
            assertThat( uow.toValueSet( value.many() ).iterator().next().name().get(), equalTo( "[Jiri]" ));
            Set<Map.Entry<String, SomeType>> actual = uow.toValueMap( value.named() ).entrySet();
            assertThat( actual.iterator().next().getKey(), anyOf(equalTo( "stan" ), equalTo( "kent" )) );
            assertThat( actual.iterator().next().getValue().name().get(), anyOf(equalTo( "[Stan]" ), equalTo( "[Kent]" )) );
        }

    }

    private SomeType createEntity( UnitOfWork uow, Identity identity )
    {
        EntityBuilder<SomeType> builder = uow.newEntityBuilder( SomeType.class, identity );
        builder.instance().name().set( identity.toString() );
        return builder.newInstance();
    }

    interface SomeType extends HasIdentity
    {
        Property<String> name();

        @Optional
        Association<SomeType> assoc();

        @UseDefaults
        ManyAssociation<SomeType> many();

        @UseDefaults
        NamedAssociation<SomeType> named();
    }

    private static class SomeValueConverter
        implements ToValueConverter
    {
        @Structure
        private Qi4jSPI spi;

        @Override
        public Function<PropertyDescriptor, Object> properties( Object entityComposite, Function<PropertyDescriptor, Object> defaultFn )
        {
            return descriptor ->
            {
                Object value = defaultFn.apply( descriptor );
                QualifiedName name = QualifiedName.fromClass( SomeType.class, "name" );
                if( name.equals( descriptor.qualifiedName() ) )
                {
                    return "[" + value + "]";
                }
                return value;
            };
        }

        @Override
        public Function<AssociationDescriptor, EntityReference> associations( Object entityComposite, Function<AssociationDescriptor, EntityReference> defaultFn )
        {
            return defaultFn;
        }

        @Override
        public Function<AssociationDescriptor, Stream<EntityReference>> manyAssociations( Object entityComposite, Function<AssociationDescriptor, Stream<EntityReference>> defaultFn )
        {
            return defaultFn;
        }

        @Override
        public Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociations( Object entityComposite, Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> defaultFn )
        {
            return defaultFn;
        }
    }
}
