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
package org.qi4j.runtime.structure;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.NoSuchTransientTypeException;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.IdentityGenerator;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.object.NoSuchObjectTypeException;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.SerializationException;
import org.qi4j.api.service.NoSuchServiceTypeException;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.structure.TypeLookup;
import org.qi4j.api.type.HasTypes;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.NoSuchValueTypeException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.composite.FunctionStateResolver;
import org.qi4j.runtime.composite.StateResolver;
import org.qi4j.runtime.composite.TransientBuilderInstance;
import org.qi4j.runtime.composite.TransientStateInstance;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.service.ImportedServicesInstance;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesInstance;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.type.ValueTypeFactoryInstance;
import org.qi4j.runtime.value.ValueBuilderInstance;
import org.qi4j.runtime.value.ValueBuilderWithPrototype;
import org.qi4j.runtime.value.ValueBuilderWithState;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.module.ModuleSpi;
import org.qi4j.runtime.activation.ActivationDelegate;

import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static org.qi4j.api.composite.CompositeInstance.compositeInstanceOf;

/**
 * Instance of a Qi4j Module. Contains the various composites for this Module.
 */
public class ModuleInstance
    implements Module, ModuleSpi, Activation
{
    // Constructor parameters
    private final ModuleModel model;
    private final LayerDescriptor layer;
    private final TypeLookup typeLookup;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final QueryBuilderFactory queryBuilderFactory;
    // Lazy assigned on accessors
    private EntityStore store;
    private IdentityGenerator generator;
    private Serialization serialization;
    private MetricsProvider metrics;
    private UnitOfWorkFactory uowf;

    @SuppressWarnings( "LeakingThisInConstructor" )
    ModuleInstance( ModuleModel moduleModel, LayerDescriptor layer, TypeLookup typeLookup,
                    ServicesModel servicesModel, ImportedServicesModel importedServicesModel
                  )
    {
        // Constructor parameters
        model = moduleModel;
        this.layer = layer;
        this.typeLookup = typeLookup;
        services = servicesModel.newInstance( moduleModel );
        importedServices = importedServicesModel.newInstance( moduleModel );

        // Eager instance objects
        activation = new ActivationDelegate( this );
        queryBuilderFactory = new QueryBuilderFactoryImpl( this );

        // Activation
        services.registerActivationEventListener( activation );
        importedServices.registerActivationEventListener( activation );
    }

    @Override
    public String toString()
    {
        return model.toString();
    }

    @Override
    public ModuleDescriptor descriptor()
    {
        return model;
    }

    // Implementation of Module
    @Override
    public String name()
    {
        return model.name();
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    // Implementation of ObjectFactory
    @Override
    public <T> T newObject( Class<T> mixinType, Object... uses )
        throws NoSuchObjectTypeException
    {
        Objects.requireNonNull( mixinType, "mixinType" );
        ObjectDescriptor model = typeLookup.lookupObjectModel( mixinType );

        if( model == null )
        {
            throw new NoSuchObjectTypeException( mixinType.getName(), name(),
                                                 typeLookup.allObjects().flatMap( HasTypes::types ) );
        }

        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        return mixinType.cast( ( (ObjectModel) model ).newInstance( injectionContext ) );
    }

    @Override
    public void injectTo( Object instance, Object... uses )
        throws ConstructionException
    {
        Objects.requireNonNull( instance, "instance" );
        ObjectDescriptor model = typeLookup.lookupObjectModel( instance.getClass() );

        if( model == null )
        {
            throw new NoSuchObjectTypeException( instance.getClass().getName(), name(),
                                                 typeLookup.allObjects().flatMap( HasTypes::types ) );
        }

        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        ( (ObjectModel) model ).inject( injectionContext, instance );
    }

    // Implementation of TransientBuilderFactory
    @Override
    public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientTypeException
    {
        Objects.requireNonNull( mixinType, "mixinType" );
        TransientDescriptor model = typeLookup.lookupTransientModel( mixinType );

        if( model == null )
        {
            throw new NoSuchTransientTypeException( mixinType.getName(), descriptor() );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<>();
        model.state().properties().forEach(
            propertyModel ->
            {
                Object initialValue = propertyModel.resolveInitialValue( model.module() );
                Property<?> property = new PropertyInstance<>( ( (PropertyModel) propertyModel ).getBuilderInfo(),
                                                               initialValue );
                properties.put( propertyModel.accessor(), property );
            } );

        TransientStateInstance state = new TransientStateInstance( properties );

        return new TransientBuilderInstance<>( model, state, UsesInstance.EMPTY_USES );
    }

    @Override
    public <T> T newTransient( final Class<T> mixinType, Object... uses )
        throws NoSuchTransientTypeException, ConstructionException
    {
        return newTransientBuilder( mixinType ).use( uses ).newInstance();
    }

    // Implementation of ValueBuilderFactory
    @Override
    public <T> T newValue( Class<T> mixinType )
        throws NoSuchValueTypeException, ConstructionException
    {
        return newValueBuilder( mixinType ).newInstance();
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
        throws NoSuchValueTypeException
    {
        Objects.requireNonNull( mixinType, "mixinType" );
        ValueDescriptor compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueTypeException( mixinType.getName(), descriptor() );
        }

        StateResolver stateResolver = new InitialStateResolver( compositeModelModule.module() );
        return new ValueBuilderInstance<>( compositeModelModule, this, stateResolver );
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                         Function<PropertyDescriptor, Object> propertyFunction,
                                                         Function<AssociationDescriptor, EntityReference> associationFunction,
                                                         Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
                                                         Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
                                                       )
    {
        Objects.requireNonNull( propertyFunction, "propertyFunction" );
        Objects.requireNonNull( associationFunction, "associationFunction" );
        Objects.requireNonNull( manyAssociationFunction, "manyAssociationFunction" );
        Objects.requireNonNull( namedAssociationFunction, "namedAssociationFunction" );

        ValueDescriptor compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueTypeException( mixinType.getName(), descriptor() );
        }

        StateResolver stateResolver = new FunctionStateResolver(
            propertyFunction, associationFunction, manyAssociationFunction, namedAssociationFunction
        );
        return new ValueBuilderWithState<>( compositeModelModule, this, stateResolver );
    }

    private static class InitialStateResolver
        implements StateResolver
    {
        private final ModuleDescriptor module;

        private InitialStateResolver( ModuleDescriptor module )
        {
            this.module = module;
        }

        @Override
        public Object getPropertyState( PropertyDescriptor propertyDescriptor )
        {
            return propertyDescriptor.resolveInitialValue( module );
        }

        @Override
        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
        {
            return null;
        }

        @Override
        public Stream<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new ArrayList<EntityReference>().stream();
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>>
        getNamedAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new HashMap<String, EntityReference>().entrySet().stream();
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
    {
        Objects.requireNonNull( prototype, "prototype" );

        ValueInstance valueInstance = (ValueInstance) compositeInstanceOf( (Composite) prototype );
        Class<Composite> valueType = (Class<Composite>) valueInstance.types().findFirst().orElse( null );

        ValueDescriptor model = typeLookup.lookupValueModel( valueType );

        if( model == null )
        {
            throw new NoSuchValueTypeException( valueType.getName(), descriptor() );
        }

        return new ValueBuilderWithPrototype<>( model, this, prototype );
    }

    @Override
    public <T> T newValueFromSerializedState( Class<T> mixinType, String serializedState )
        throws NoSuchValueTypeException, ConstructionException
    {
        Objects.requireNonNull( mixinType, "mixinType" );
        ValueDescriptor model = typeLookup.lookupValueModel( mixinType );

        if( model == null )
        {
            throw new NoSuchValueTypeException( mixinType.getName(), descriptor() );
        }

        try
        {
            return serialization().deserialize( model.module(), model.valueType(), serializedState );
        }
        catch( SerializationException ex )
        {
            throw new ConstructionException( "Could not create value from serialized state", ex );
        }
    }

    // Implementation of QueryBuilderFactory
    @Override
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        return queryBuilderFactory.newQueryBuilder( resultType );
    }

    @Override
    public <T> ServiceReference<T> findService( Class<T> serviceType )
        throws NoSuchServiceTypeException
    {
        return findService( (Type) serviceType );
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        ModelDescriptor serviceModel = typeLookup.lookupServiceModel( serviceType );
        if( serviceModel == null )
        {
            throw new NoSuchServiceTypeException( serviceType.getTypeName(), descriptor() );
        }
        return findServiceReferenceInstance( serviceModel );
    }

    @Override
    public <T> Stream<ServiceReference<T>> findServices( final Class<T> serviceType )
    {
        return findServices( (Type) serviceType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Stream<ServiceReference<T>> findServices( final Type serviceType )
    {
        List<? extends ModelDescriptor> serviceModels = typeLookup.lookupServiceModels( serviceType );
        if( serviceModels == null )
        {
            return Stream.empty();
        }
        //noinspection unchecked
        return serviceModels.stream()
                            .map( this::findServiceReferenceInstance )
                            .filter( Objects::nonNull )
                            .filter( ref -> ref.hasType( serviceType ) )
                            .map( ref -> (ServiceReference<T>) ref );
    }

    @SuppressWarnings( "unchecked" )
    private <T> ServiceReference<T> findServiceReferenceInstance( ModelDescriptor model )
    {
        ModuleInstance moduleInstanceOfModel = (ModuleInstance) model.module().instance();
        Optional<ServiceReference<?>> candidate =
            concat( moduleInstanceOfModel.services.references(), moduleInstanceOfModel.importedServices.references() )
                .filter( ref -> ref.model().equals( model ) )
                .findAny();
        if( candidate.isPresent() )
        {
            ServiceReference<?> serviceReference = candidate.get();
            return (ServiceReference<T>) serviceReference;
        }
        return null;
    }

    // Implementation of Activation
    @Override
    public void activate()
        throws ActivationException
    {
        activation.activate( model.newActivatorsInstance(), asList( services, importedServices ) );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    // Other methods
    ModuleModel model()
    {
        return model;
    }

    @Override
    public LayerDescriptor layer()
    {
        return layer;
    }

    @Override
    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    @Override
    public EntityStore entityStore()
    {
        if( store == null )
        {
            synchronized( this )
            {
                if( store == null )
                {
                    try
                    {
                        store = findService( EntityStore.class ).get();
                    }
                    catch( NoSuchServiceTypeException e )
                    {
                        throw new UnitOfWorkException( "No EntityStore service available in module " + name() );
                    }
                }
            }
        }
        return store;
    }

    @Override
    public UnitOfWorkFactory unitOfWorkFactory()
    {
        if( uowf == null )
        {
            synchronized( this )
            {
                if( uowf == null )
                {
                    try
                    {
                        uowf = findService( UnitOfWorkFactory.class ).get();
                    }
                    catch( NoSuchServiceTypeException e )
                    {
                        throw new UnitOfWorkException( "No UnitOfWorkFactory service available in module " + name() );
                    }
                }
            }
        }
        return uowf;
    }

    @Override
    public ServiceFinder serviceFinder()
    {
        return this;
    }

    @Override
    public ValueBuilderFactory valueBuilderFactory()
    {
        return this;
    }

    @Override
    public TransientBuilderFactory transientBuilderFactory()
    {
        return this;
    }

    @Override
    public ObjectFactory objectFactory()
    {
        return this;
    }

    @Override
    public IdentityGenerator identityGenerator()
    {
        if( generator == null )
        {
            synchronized( this )
            {
                if( generator == null )
                {
                    generator = findService( IdentityGenerator.class ).get();
                }
            }
        }
        return generator;
    }

    @Override
    public Serialization serialization()
    {
        if( serialization == null )
        {
            synchronized( this )
            {
                if( serialization == null )
                {
                    serialization = findService( Serialization.class ).get();
                }
            }
        }
        return serialization;
    }

    @Override
    public MetricsProvider metricsProvider()
    {
        if( metrics == null )
        {
            synchronized( this )
            {
                if( metrics == null )
                {
                    metrics = findService( MetricsProvider.class ).get();
                }
            }
        }
        return metrics;
    }

    @Override
    public ValueTypeFactoryInstance valueTypeFactory()
    {
        return ValueTypeFactoryInstance.instance();
    }
}
