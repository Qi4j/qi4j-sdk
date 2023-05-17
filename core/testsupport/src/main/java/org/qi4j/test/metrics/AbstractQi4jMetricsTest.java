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
package org.qi4j.test.metrics;

import java.util.Collection;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.metrics.TimingCapture;
import org.qi4j.api.metrics.TimingCaptureAllConcern;
import org.qi4j.api.metrics.TimingCaptureConcern;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jBaseTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.JmxFixture;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.MANDATORY;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

// TODO Test errors
public abstract class AbstractQi4jMetricsTest extends AbstractQi4jBaseTest
{
    public interface Person
    {
        Property<String> name();
    }

    public interface PersonList
    {
        Identity LIST_ID = StringIdentity.identityOf( "person-list" );

        ManyAssociation<Person> all();
    }

    @Concerns( { TimingCaptureAllConcern.class, UnitOfWorkConcern.class} )
    @Mixins( CommandsMixin.class )
    public interface Commands extends ServiceActivation
    {
        @UnitOfWorkPropagation( MANDATORY )
        Person create( Identity id, String name );

        @UnitOfWorkPropagation( MANDATORY )
        void rename( Identity id, String newName );

        @UnitOfWorkPropagation( MANDATORY )
        void delete( Identity id );
    }

    public static class CommandsMixin implements Commands
    {
        @Structure
        private Module module;

        @Override
        public void activateService() throws Exception
        {
            try (UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( newUsecase( "Init Person List" ) ) )
            {
                try
                {
                    uow.get( PersonList.class, PersonList.LIST_ID );
                }
                catch( NoSuchEntityException ex )
                {
                    uow.newEntity( PersonList.class, PersonList.LIST_ID );
                    uow.complete();
                }
            }
        }

        @Override
        public void passivateService()
        {
        }

        @Override
        public Person create( Identity id, String name )
        {
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            PersonList list = uow.get( PersonList.class, PersonList.LIST_ID );
            EntityBuilder<Person> builder = uow.newEntityBuilder( Person.class, id );
            builder.instance().name().set( name );
            Person person = builder.newInstance();
            list.all().add( person );
            return person;
        }

        @Override
        public void rename( Identity id, String newName )
        {
            module.unitOfWorkFactory().currentUnitOfWork().get( Person.class, id ).name().set( newName );
        }

        @Override
        public void delete( Identity id )
        {
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            PersonList list = uow.get( PersonList.class, PersonList.LIST_ID );
            Person person = uow.get( Person.class, id );
            list.all().remove( person );
            uow.remove( person );
        }
    }

    @Concerns( { TimingCaptureConcern.class, UnitOfWorkConcern.class} )
    @Mixins( QueriesMixin.class )
    public interface Queries
    {
        @UnitOfWorkPropagation( MANDATORY )
        Person byId( Identity id );

        @TimingCapture
        @UnitOfWorkPropagation( MANDATORY )
        Iterable<Person> all();
    }

    public static class QueriesMixin implements Queries
    {
        @Structure
        private Module module;

        @Override
        public Person byId( Identity id )
        {
            return module.unitOfWorkFactory().currentUnitOfWork().get( Person.class, id );
        }

        @Override
        public Iterable<Person> all()
        {
            return module.unitOfWorkFactory().currentUnitOfWork()
                    .get( PersonList.class, PersonList.LIST_ID )
                    .all().toList();
        }
    }

    @Override
    protected final void defineApplication( ApplicationAssembly app )
        throws Exception
    {
        app.setName( "app" );

        LayerAssembly domain = app.layer( "domain" );
        ModuleAssembly model = domain.module( "model" );
        model.entities( Person.class, PersonList.class )
                .visibleIn( Visibility.layer );
        ModuleAssembly services = domain.module( "services" );
        services.services( Commands.class, Queries.class )
                .instantiateOnStartup()
                .visibleIn( Visibility.application );

        LayerAssembly config = app.layer( "config" );
        ModuleAssembly configModule = config.module( "config" );
        new EntityTestAssembler()
                .visibleIn( Visibility.module )
                .assemble( configModule );

        LayerAssembly infra = app.layer( "infra" );
        ModuleAssembly storage = infra.module( "storage" );
        entityStoreAssembler( configModule, Visibility.application )
                .visibleIn( Visibility.application )
                .assemble( storage );
        metricsAssembler()
                .visibleIn( Visibility.application )
                .assemble( infra.module( "metrics" ) );

        domain.uses( infra );
        infra.uses( config );
    }

    protected Assemblers.Visible<? extends Assembler> entityStoreAssembler( ModuleAssembly configModule, Visibility configVisibility ) throws AssemblyException
    {
        return new EntityTestAssembler().defaultServicesVisibleIn( Visibility.module );
    }

    protected abstract Assemblers.Visible<? extends Assembler> metricsAssembler();

    protected Module metricsModule()
    {
        return application.findModule( "infra", "metrics" );
    }

    protected static final String UOW_TIMER_NAME = "app.domain.services.UnitOfWork.timer";
    protected static final String ALL_NAME = "app.domain.services.AbstractQi4jMetricsTest.Queries.all";
    protected static final String CREATE_NAME = "app.domain.services.Abstractqi4jMetricsTest.Commands.create";
    protected static final String RENAME_NAME = "app.domain.services.Abstractqi4jMetricsTest.Commands.rename";
    protected static final String DELETE_NAME = "app.domain.services.Abstractqi4jMetricsTest.Commands.delete";

    protected final void assertUowTimer( MetricValuesProvider metrics ) throws PassivationException, ActivationException
    {
        Long initialUowCount = metrics.timerCount( UOW_TIMER_NAME );
        runScenario1();
        assertThat( UOW_TIMER_NAME + " count incremented by 3", metrics.timerCount( UOW_TIMER_NAME ), is( initialUowCount + 3L ) );
        application.passivate();
        application.activate();
        assertThat( UOW_TIMER_NAME + " count reset on passivation", metrics.timerCount( UOW_TIMER_NAME ), equalTo( initialUowCount ) );
    }

    protected final void assertTimingCapture( MetricValuesProvider metrics ) throws PassivationException, ActivationException
    {
        // Initial state
        assertThat( ALL_NAME + " count is 0 at start", metrics.timerCount( ALL_NAME ), is( 0L ) );
        assertThat( CREATE_NAME + " count is 0 at start", metrics.timerCount( CREATE_NAME ), is( 0L ) );
        assertThat( RENAME_NAME + " count is 0 at start", metrics.timerCount( RENAME_NAME ), is( 0L ) );
        assertThat( DELETE_NAME+ " count is 0 at start", metrics.timerCount( DELETE_NAME ), is( 0L ) );

        // Run scenario
        runScenario1();

        // Queries.byId() timings are not captured
        assertThat( "Queries.byId() has no timer", metrics.registeredMetricNames(), not( contains( containsString( "byId" ) ) ) );

        // Captured timings
        assertThat( ALL_NAME + " count is 4 after scenario", metrics.timerCount( ALL_NAME ), is( 4L ) );
        assertThat( CREATE_NAME + " count is 1 after scenario", metrics.timerCount( CREATE_NAME ), is( 1L ) );
        assertThat( RENAME_NAME + " count is 1 after scenario", metrics.timerCount( RENAME_NAME ), is( 1L ) );
        assertThat( DELETE_NAME + " count is 1 after scenario", metrics.timerCount( DELETE_NAME ), is( 1L ) );

        // Reset on passivation
        application.passivate();
        application.activate();
        assertThat( ALL_NAME + " count is 0 after restart", metrics.timerCount( ALL_NAME ), is( 0L ) );
        assertThat( CREATE_NAME + " count is 0 after restart", metrics.timerCount( CREATE_NAME ), is( 0L ) );
        assertThat( RENAME_NAME + " count is 0 after restart", metrics.timerCount( RENAME_NAME ), is( 0L ) );
        assertThat( DELETE_NAME + " count is 0 after restart", metrics.timerCount( DELETE_NAME ), is( 0L ) );
    }

    protected final void runScenario1()
    {
        Module services = application.findModule( "domain", "services" );
        Commands commands = services.findService( Commands.class ).get();
        Queries queries = services.findService( Queries.class ).get();

        Identity identity = StringIdentity.identityOf( "1" );

        try (UnitOfWork uow = services.unitOfWorkFactory().newUnitOfWork( newUsecase( "Step 1" ) ) )
        {
            assertThat( queries.all().iterator().hasNext(), is( false ) );
            assertThat( commands.create( identity, "Bob Geldof" ).name().get(), equalTo( "Bob Geldof" ) );
            assertThat( queries.byId( identity ).name().get(), equalTo( "Bob Geldof" ) );
            uow.complete();
        }

        try (UnitOfWork uow = services.unitOfWorkFactory().newUnitOfWork(newUsecase("Step 2")))
        {
            assertThat( queries.all().iterator().next().name().get(), equalTo( "Bob Geldof" ) );
            assertThat( queries.byId( identity ).name().get(), equalTo( "Bob Geldof" ) );
            commands.rename( identity, "Nina Hagen" );
            assertThat( queries.all().iterator().next().name().get(), equalTo( "Nina Hagen" ) );
            uow.complete();
        }

        try (UnitOfWork uow = services.unitOfWorkFactory().newUnitOfWork(newUsecase("Step 3")))
        {
            commands.delete( identity );
            assertThat( queries.all().iterator().hasNext(), is( false ) );
            uow.complete();
        }
    }

    protected static class JmxMetricTestAdapter implements MetricValuesProvider
    {
        private final JmxFixture jmx = new JmxFixture( "metrics:name=" );

        @Override
        public long timerCount( String name )
        {
            if( jmx.objectExists( name ) ) {
                return jmx.attributeValue( name, "Count", Long.class );
            }
            return 0L;
        }

        @Override
        public Collection<String> registeredMetricNames()
        {
            return jmx.allObjectNames().stream()
                    .filter( objName -> objName.startsWith( jmx.prefix() ) )
                    .map( objName -> objName.substring( jmx.prefix().length() ) )
                    .collect( toList() );
        }
    }

    @Test
    public void uowTimerJmx() throws PassivationException, ActivationException
    {
        assertUowTimer( new JmxMetricTestAdapter() );
    }

    @Test
    public void timingCaptureJmx() throws PassivationException, ActivationException
    {
        assertTimingCapture( new JmxMetricTestAdapter() );
    }
}
