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
package org.qi4j.test.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Abstract satisfiedBy with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{

    @Service
    private EntityStore store;

    @Structure
    protected Module moduleInstance;

    private ZonedDateTime refDate = ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 0, UTC );

    @Override
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        module.entities( TestEntity.class );
        module.values( TestValue.class, TestValue2.class, TjabbaValue.class );
        module.objects( getClass() );
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        super.tearDown();
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        builder.instance().instantValue().set( Instant.now() );
        TestEntity instance = builder.newInstance();

        instance.name().set( "Test" );
        instance.intValue().set( 42 );
        instance.longValue().set( 42L );
        instance.doubleValue().set( 42D );
        instance.floatValue().set( 42F );
        instance.booleanValue().set( Boolean.TRUE );
        instance.bigIntegerValue().set( new BigInteger( "42" ) );
        instance.bigDecimalValue().set( new BigDecimal( "42" ) );
        instance.instantValue().set( refDate.toInstant() );
        instance.dateTimeValue().set( refDate );
        instance.localDateTimeValue().set( LocalDateTime.of( 2020, 3, 4, 13, 23, 0 ) );
        instance.localDateValue().set( LocalDate.of( 2020, 3, 4 ) );
        instance.localTimeValue().set( LocalTime.of( 19, 20, 21 ) );

        instance.duractionValue().set( Duration.between( LocalDateTime.of( 2010, 1, 2, 19, 20, 21 ),
                                                         LocalDateTime.of( 2010, 1, 2, 20, 21, 22 ) ) );
        instance.periodValue().set( Period.between( LocalDate.of( 2005, 12, 21 ), LocalDate.of( 2007, 1, 23 ) ) );

        instance.association().set( instance );

        ValueBuilder<Tjabba> valueBuilder4 = moduleInstance.newValueBuilder( Tjabba.class );
        final Tjabba prototype4 = valueBuilder4.prototype();
        prototype4.bling().set( "BlinkLjus" );

        // Set value
        ValueBuilder<TestValue2> valueBuilder2 = moduleInstance.newValueBuilder( TestValue2.class );
        TestValue2 prototype2 = valueBuilder2.prototype();
        prototype2.stringValue().set( "Bar" );
        Tjabba newValue = valueBuilder4.newInstance();
        prototype2.anotherValue().set( newValue );
        prototype2.anotherValue().set( newValue );

        ValueBuilder<Tjabba> valueBuilder3 = moduleInstance.newValueBuilder( Tjabba.class );
        final Tjabba prototype3 = valueBuilder3.prototype();
        prototype3.bling().set( "Brakfis" );

        ValueBuilder<TestValue> valueBuilder1 = moduleInstance.newValueBuilder( TestValue.class );
        TestValue prototype = valueBuilder1.prototype();
        prototype.enumProperty().set( TestEnum.VALUE3 );
        prototype.listProperty().get().add( "Foo" );

        prototype.valueProperty().set( valueBuilder2.newInstance() );
        prototype.tjabbaProperty().set( valueBuilder3.newInstance() );
        Map<String, String> mapValue = new HashMap<>( 1 );
        mapValue.put( "foo", "bar" );
        prototype.mapStringStringProperty().set( mapValue );
        instance.valueProperty().set( valueBuilder1.newInstance() );

        instance.manyAssociation().add( 0, instance );

        instance.namedAssociation().put( "foo", instance );
        instance.namedAssociation().put( "bar", instance );

        return instance;
    }

    @Test
    public void whenNewEntityThenCanFindEntityAndCorrectValues()
        throws Exception
    {
        TestEntity instance;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            instance = createEntity( unitOfWork );
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            // Find entity
            instance = unitOfWork.get( instance );

            // Check state
            assertThat( "property 'intValue' has incorrect value",
                        instance.intValue().get(),
                        equalTo( 42 ) );

            assertThat( "property 'longValue' has incorrect value",
                        instance.longValue().get(),
                        equalTo( 42L ) );

            assertThat( "property 'doubleValue' has incorrect value",
                        instance.doubleValue().get(),
                        equalTo( 42.0 ) );

            assertThat( "property 'floatValue' has incorrect value",
                        instance.floatValue().get(),
                        equalTo( 42f ) );

            assertThat( "property 'booleanValue' has incorrect value",
                        instance.booleanValue().get(),
                        equalTo( Boolean.TRUE ) );

            assertThat( "property 'bigDecimal' has incorrect value",
                        instance.bigDecimalValue().get(),
                        equalTo( new BigDecimal( "42" ) ) );

            assertThat( "property 'bigInteger' has incorrect value",
                        instance.bigIntegerValue().get(),
                        equalTo( new BigInteger( "42" ) ) );

            assertThat( "property 'dateTimeValue' has incorrect value",
                        instance.dateTimeValue().get(),
                        equalTo( refDate ) );

            assertThat( "property 'instantValue' has incorrect value",
                        instance.instantValue().get(),
                        equalTo( refDate.toInstant() ) );

            assertThat( "property 'localDateTimeValue' has incorrect value",
                        instance.localDateTimeValue().get(),
                        equalTo( LocalDateTime.of( 2020, 3, 4, 13, 23, 0 ) ) );

            assertThat( "property 'localDateValue' has incorrect value",
                        instance.localDateValue().get(),
                        equalTo( LocalDate.of( 2020, 3, 4 ) ) );

            assertThat( "property 'localTimeValue' has incorrect value",
                        instance.localTimeValue().get(),
                        equalTo( LocalTime.of( 19, 20, 21 ) ) );

            assertThat( "property 'periodValue' has incorrect value",
                        instance.periodValue().get(),
                        equalTo( Period.of( 1, 1, 2 ) ) );

            assertThat( "property 'durationValue' has incorrect value",
                        instance.duractionValue().get(),
                        equalTo( Duration.ofSeconds( 3661 ) ) );

            assertThat( "property 'name' has incorrect value",
                        instance.name().get(),
                        equalTo( "Test" ) );

            assertThat( "property 'unsetName' has incorrect value",
                        instance.unsetName().get(),
                        equalTo( null ) );

            assertThat( "property 'emptyName' has incorrect value",
                        instance.emptyName().get(),
                        equalTo( "" ) );

            Property<TestValue> testValueProperty = instance.valueProperty();
            TestValue testValue = testValueProperty.get();
            Property<TestValue2> testValue2Property = testValue.valueProperty();
            TestValue2 testValue2 = testValue2Property.get();
            Property<String> stringProperty = testValue2.stringValue();
            String actual = stringProperty.get();
            assertThat( "property 'valueProperty.stringValue' has incorrect value",
                        actual,
                        equalTo( "Bar" ) );

            assertThat( "property 'valueProperty.listProperty' has incorrect value",
                        instance.valueProperty().get().listProperty().get().get( 0 ),
                        equalTo( "Foo" ) );

            assertThat( "property 'valueProperty.enumProperty' has incorrect value",
                        instance.valueProperty().get().enumProperty().get(),
                        equalTo( TestEnum.VALUE3 ) );

            assertThat( "property 'valueProperty.anotherValue.bling' has incorrect value",
                        instance.valueProperty().get().valueProperty().get().anotherValue().get().bling().get(),
                        equalTo( "BlinkLjus" ) );

            assertThat( "property 'valueProperty.tjabbaProperty.bling' has incorrect value",
                        instance.valueProperty().get().tjabbaProperty().get().bling().get(),
                        equalTo( "Brakfis" ) );

            Map<String, String> mapValue = new HashMap<>();
            mapValue.put( "foo", "bar" );
            assertThat( "property 'valueProperty.mapStringStringProperty' has incorrect value",
                        instance.valueProperty().get().mapStringStringProperty().get(),
                        equalTo( mapValue ) );

            assertThat( "association has incorrect value",
                        instance.association().get(),
                        equalTo( instance ) );

            assertThat( "manyAssociation has incorrect value",
                        instance.manyAssociation().iterator().next(),
                        equalTo( instance ) );

            assertThat( "namedAssociation has incorrect 'foo' value",
                        instance.namedAssociation().get( "foo" ),
                        equalTo( instance ) );

            assertThat( "namedAssociation has incorrect 'bar' value",
                        instance.namedAssociation().get( "bar" ),
                        equalTo( instance ) );
        }
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        TestEntity newInstance;
        Identity identity;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            newInstance = createEntity( unitOfWork );
            identity = newInstance.identity().get();
            unitOfWork.complete();
        }

        // Remove entity
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            TestEntity instance = unitOfWork.get( newInstance );
            unitOfWork.remove( instance );
            unitOfWork.complete();
        }

        // Find entity
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            unitOfWork.get( TestEntity.class, identity );
            fail( "Should not be able to find entity" );
        }
        catch( NoSuchEntityException e )
        {
            // Ok!
        }
    }

    @Test
    public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            version = spi.entityStateOf( testEntity ).version();

            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            testEntity.name().set( "Rickard" );
            version = spi.entityStateOf( testEntity ).version();
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
            assertThat( "version has not changed", newVersion, not( equalTo( version ) ) );
            unitOfWork.complete();
        }
    }

    @Test
    public void givenAssociationsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            testEntity.association().set( testEntity );
            testEntity.manyAssociation().add( 0, testEntity );
            testEntity.namedAssociation().put( "test", testEntity );
            version = spi.entityStateOf( testEntity ).version();

            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
            assertThat( "association persisted", testEntity.association().get(), equalTo( testEntity ) );
            assertThat( "many association persisted", testEntity.manyAssociation().get( 0 ), equalTo( testEntity ) );
            assertThat( "named association persisted", testEntity.namedAssociation().get( "test" ), equalTo( testEntity ) );
            assertThat( "version has not changed", newVersion, not( equalTo( version ) ) );

            testEntity.association().set( null );
            testEntity.manyAssociation().clear();
            testEntity.namedAssociation().clear();
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
            assertThat( "association cleared", testEntity.association().get(), nullValue() );
            assertThat( "many association cleared", testEntity.manyAssociation().count(), is( 0 ) );
            assertThat( "named association cleared", testEntity.namedAssociation().count(), is( 0 ) );
            assertThat( "version has not changed", newVersion, not( equalTo( version ) ) );
            unitOfWork.complete();
        }
    }

    @Test
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        UnitOfWork unitOfWork1;
        TestEntity testEntity1;
        String version;
        {
            // Start working with Entity in one UoW
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            version = spi.entityStateOf( testEntity1 ).version();
            if( version.isEmpty() )
            {
                unitOfWork1.discard();
                return; // Store doesn't track versions - no point in testing it
            }
            testEntity1.name().set( "A" );
            testEntity1.unsetName().set( "A" );
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            // Start working with same Entity in another UoW, and complete it
            TestEntity testEntity2 = unitOfWork.get( testEntity );
            assertThat( "version is correct", spi.entityStateOf( testEntity1 ).version(), equalTo( version ) );
            testEntity2.name().set( "B" );
            unitOfWork.complete();
        }
        {
            // Try to complete first UnitOfWork
            try
            {
                unitOfWork1.complete();
                fail( "Should have thrown concurrent modification exception" );
            }
            catch( ConcurrentEntityModificationException e )
            {
                unitOfWork1.discard();
            }
        }
        {
            // Check values
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            assertThat( "property name has not been set", testEntity1.name().get(), equalTo( "B" ) );
            assertThat( "version is incorrect", spi.entityStateOf( testEntity1 ).version(),
                        not( equalTo( version ) ) );
            unitOfWork1.discard();
        }
    }

    @Test
    public void givenEntityStoredLoadedChangedWhenUnitOfWorkDiscardsThenDontStoreState()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity1 = createEntity( unitOfWork );
            Identity identity = entity1.identity().get();
            unitOfWork.complete();

            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity entity = unitOfWork.get( TestEntity.class, identity );
            assertThat( entity.intValue().get(), is( 42 ) );
            entity.intValue().set( 23 );
            unitOfWork.discard();

            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            entity = unitOfWork.get( TestEntity.class, identity );
            assertThat( entity.intValue().get(), is( 42 ) );
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void entityStatesSPI()
    {
        EntityStore entityStore = serviceFinder.findService( EntityStore.class ).get();

        try( Stream<EntityState> states = entityStore.entityStates( module ) )
        {
            assertThat( states.count(), is( 0L ) );
        }

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity newInstance = createEntity( unitOfWork );
        unitOfWork.complete();

        try( Stream<EntityState> states = entityStore.entityStates( module ) )
        {
            assertThat( states.count(), is( 1L ) );
        }

        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity instance = unitOfWork.get( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        try( Stream<EntityState> states = entityStore.entityStates( module ) )
        {
            assertThat( states.count(), is( 0L ) );
        }
    }

    public interface TestEntity
        extends EntityComposite
    {

        @UseDefaults
        Property<Integer> intValue();

        @UseDefaults
        Property<Long> longValue();

        @UseDefaults
        Property<Double> doubleValue();

        @UseDefaults
        Property<Float> floatValue();

        @UseDefaults
        Property<Boolean> booleanValue();

        @Optional
        Property<BigInteger> bigIntegerValue();

        @Optional
        Property<BigDecimal> bigDecimalValue();

        @Optional
        Property<Instant> instantValue();

        @Optional
        Property<ZonedDateTime> dateTimeValue();

        @Optional
        Property<LocalDateTime> localDateTimeValue();

        @Optional
        Property<LocalDate> localDateValue();

        @Optional
        Property<LocalTime> localTimeValue();

        @Optional
        Property<Period> periodValue();

        @Optional
        Property<Duration> duractionValue();

        @Optional
        Property<String> name();

        @Optional
        Property<String> unsetName();

        @UseDefaults
        Property<String> emptyName();

        @Optional
        Property<TestValue> valueProperty();

        @Optional
        Association<TestEntity> association();

        @Optional
        Association<TestEntity> unsetAssociation();

        ManyAssociation<TestEntity> manyAssociation();

        NamedAssociation<TestEntity> namedAssociation();
    }

    public interface TjabbaValue
        extends Tjabba, ValueComposite
    {
    }

    public interface Tjabba
    {

        Property<String> bling();
    }

    public interface TestValue
        extends ValueComposite
    {

        @UseDefaults
        Property<String> stringProperty();

        @UseDefaults
        Property<Integer> intProperty();

        @UseDefaults
        Property<TestEnum> enumProperty();

        @UseDefaults
        Property<List<String>> listProperty();

        @UseDefaults
        Property<Map<String, Tjabba>> mapProperty();

        Property<TestValue2> valueProperty();

        Property<Tjabba> tjabbaProperty();

        Property<Map<String, String>> mapStringStringProperty();
    }

    public interface TestValue2
        extends ValueComposite
    {
        Property<String> stringValue();

        Property<Tjabba> anotherValue();
    }

    public enum TestEnum
    {
        VALUE1, VALUE2, VALUE3
    }
}
