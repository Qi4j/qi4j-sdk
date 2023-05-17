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

package org.qi4j.runtime.unitofwork;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.IdentityGenerator;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.NoSuchEntityTypeException;
import org.qi4j.api.unitofwork.ToEntityConverter;
import org.qi4j.api.unitofwork.ToValueConverter;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.composite.FunctionStateResolver;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.value.ValueStateInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.module.ModuleSpi;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.QueryBuilderSPI;
import org.qi4j.spi.query.QuerySource;

import static org.qi4j.api.composite.CompositeInstance.compositeInstanceOf;
import static org.qi4j.api.identity.HasIdentity.IDENTITY_STATE_NAME;

/**
 * JAVADOC
 */
public class ModuleUnitOfWork
    implements UnitOfWork
{
    @Uses
    private UnitOfWorkInstance uow;

    @Structure
    private Qi4jAPI api;

    @Structure
    private ModuleDescriptor module;

    @Service
    private UnitOfWorkFactory unitOfWorkFactory;

    public ModuleDescriptor module()
    {
        return module;
    }

    public UnitOfWorkInstance instance()
    {
        return uow;
    }

    @Override
    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return unitOfWorkFactory;
    }

    @Override
    public Instant currentTime()
    {
        return uow.currentTime();
    }

    @Override
    public Usecase usecase()
    {
        return uow.usecase();
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return uow.metaInfo().get( infoType );
    }

    @Override
    public void setMetaInfo( Object metaInfo )
    {
        uow.metaInfo().set( metaInfo );
    }

    @Override
    @SuppressWarnings( { "raw", "unchecked" } )
    public <T> Query<T> newQuery( QueryBuilder<T> queryBuilder )
    {
        QueryBuilderSPI queryBuilderSPI = (QueryBuilderSPI) queryBuilder;

        return queryBuilderSPI.newQuery( new UoWQuerySource( this ) );
    }

    @Override
    public <T> T newEntity( Class<T> type )
        throws NoSuchEntityTypeException, LifecycleException
    {
        return newEntity( type, null );
    }

    @Override
    public <T> T newEntity( Class<T> type, Identity identity )
        throws NoSuchEntityTypeException, LifecycleException
    {
        return newEntityBuilder( type, identity ).newInstance();
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws NoSuchEntityTypeException
    {
        return newEntityBuilder( type, null );
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type, Identity identity )
        throws NoSuchEntityTypeException
    {
        EntityDescriptor model = module.typeLookup().lookupEntityModel( type );

        if( model == null )
        {
            throw new NoSuchEntityTypeException( type.getName(), module );
        }

        ModuleDescriptor modelModule = model.module();
        EntityStore entityStore = ( (ModuleSpi) modelModule.instance() ).entityStore();

        // Generate id if necessary
        if( identity == null )
        {
            IdentityGenerator idGen = ( (ModuleSpi) modelModule.instance() ).identityGenerator();
            identity = idGen.generate( model.types().findFirst().orElse( null ) );
        }
        EntityBuilder<T> builder;

        builder = new EntityBuilderInstance<>( model,
                                               this,
                                               uow.getEntityStoreUnitOfWork( entityStore ),
                                               identity );
        return builder;
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilderWithState(
        Class<T> type,
        Function<PropertyDescriptor, Object> propertyFunction,
        Function<AssociationDescriptor, EntityReference> associationFunction,
        Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
        Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
                                                         )
        throws NoSuchEntityTypeException
    {
        return newEntityBuilderWithState( type, null,
                                          propertyFunction,
                                          associationFunction,
                                          manyAssociationFunction,
                                          namedAssociationFunction );
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilderWithState(
        Class<T> type, Identity identity,
        Function<PropertyDescriptor, Object> propertyFunction,
        Function<AssociationDescriptor, EntityReference> associationFunction,
        Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
        Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
                                                         )
        throws NoSuchEntityTypeException
    {
        Objects.requireNonNull( propertyFunction, "propertyFunction" );
        Objects.requireNonNull( associationFunction, "associationFunction" );
        Objects.requireNonNull( manyAssociationFunction, "manyAssociationFunction" );
        Objects.requireNonNull( namedAssociationFunction, "namedAssociationFunction" );

        EntityDescriptor model = module.typeLookup().lookupEntityModel( type );

        if( model == null )
        {
            throw new NoSuchEntityTypeException( type.getName(), module );
        }

        ModuleDescriptor modelModule = model.module();
        ModuleSpi moduleSpi = (ModuleSpi) modelModule.instance();
        EntityStore entityStore = moduleSpi.entityStore();

        FunctionStateResolver stateResolver = new FunctionStateResolver(
            propertyFunction, associationFunction, manyAssociationFunction, namedAssociationFunction
        );

        if( identity == null )
        {
            // Use reference from StateResolver if available
            PropertyModel identityModel = (PropertyModel) model
                .state()
                .findPropertyModelByQualifiedName( IDENTITY_STATE_NAME );
            String propertyState = (String) stateResolver.getPropertyState( identityModel );
            if( propertyState == null )
            {
                // Generate reference
                IdentityGenerator idGen = moduleSpi.identityGenerator();
                identity = idGen.generate( model.types().findFirst().orElse( null ) );
            }
            else
            {
                identity = StringIdentity.identityOf( propertyState );
            }
        }

        return new EntityBuilderInstance<>( model,
                                            this,
                                            uow.getEntityStoreUnitOfWork( entityStore ),
                                            identity,
                                            stateResolver );
    }

    @Override
    public <T> T get( Class<T> type, Identity identity )
        throws NoSuchEntityTypeException, NoSuchEntityException
    {
        Iterable<EntityDescriptor> models = module.typeLookup().lookupEntityModels( type );

        if( !models.iterator().hasNext() )
        {
            throw new NoSuchEntityTypeException( type.getName(), module );
        }

        return uow.get( EntityReference.create( identity ), this, models, type );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T get( T entity )
        throws NoSuchEntityTypeException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = (EntityInstance) compositeInstanceOf( entityComposite );
        EntityDescriptor model = compositeInstance.entityModel();
        Class<T> type = (Class<T>) compositeInstance.types().findFirst().orElse( null );
        return uow.get( compositeInstance.reference(), this, Collections.singletonList( model ), type );
    }

    @Override
    public void remove( Object entity )
        throws LifecycleException
    {
        uow.checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;

        EntityInstance compositeInstance = (EntityInstance) compositeInstanceOf( entityComposite );

        if( compositeInstance.status() == EntityStatus.NEW )
        {
            compositeInstance.remove( this );
            uow.remove( compositeInstance.reference() );
        }
        else if( compositeInstance.status() == EntityStatus.LOADED || compositeInstance.status() == EntityStatus.UPDATED )
        {
            compositeInstance.remove( this );
        }
        else
        {
            throw new NoSuchEntityException( compositeInstance.reference(), compositeInstance.types(), usecase() );
        }
    }

    @SuppressWarnings( "DuplicateThrows" )
    @Override
    public void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        uow.complete();
    }

    @Override
    public void discard()
    {
        uow.discard();
    }

    @Override
    public void close()
    {
        discard();
    }

    @Override
    public boolean isOpen()
    {
        return uow.isOpen();
    }

    @Override
    public boolean isPaused()
    {
        return uow.isPaused();
    }

    @Override
    public void pause()
    {
        uow.pause();
    }

    @Override
    public void resume()
    {
        uow.resume();
    }

    @Override
    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        uow.addUnitOfWorkCallback( callback );
    }

    @Override
    public void removeUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        uow.removeUnitOfWorkCallback( callback );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ModuleUnitOfWork that = (ModuleUnitOfWork) o;

        return uow.equals( that.uow );
    }

    @Override
    public int hashCode()
    {
        return uow.hashCode();
    }

    @Override
    public String toString()
    {
        return uow.toString();
    }

    void addEntity( EntityInstance instance )
    {
        uow.addEntity( instance );
    }

    @Override
    public <T extends HasIdentity> T toValue( Class<T> primaryType, T entityComposite )
    {
        Objects.requireNonNull( primaryType );
        if( entityComposite == null )
        {
            return null;
        }
        Function<PropertyDescriptor, Object> propertyFunction = new ToValuePropertyMappingFunction( entityComposite );
        Function<AssociationDescriptor, EntityReference> assocationFunction = new ToValueAssociationMappingFunction<>( entityComposite );
        Function<AssociationDescriptor, Stream<EntityReference>> manyAssocFunction = new ToValueManyAssociationMappingFunction<>( entityComposite );
        Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssocFunction = new ToValueNameAssociationMappingFunction<>( entityComposite );
        ToValueConverter converter = getConverter( ToValueConverter.class );
        if( converter != null )
        {
            propertyFunction = converter.properties( entityComposite, propertyFunction );
            assocationFunction = converter.associations( entityComposite, assocationFunction );
            manyAssocFunction = converter.manyAssociations( entityComposite, manyAssocFunction );
            namedAssocFunction = converter.namedAssociations( entityComposite, namedAssocFunction );
        }
        @SuppressWarnings( "unchecked" )
        ValueBuilder<T> builder = module().instance().newValueBuilderWithState(
            primaryType, propertyFunction, assocationFunction, manyAssocFunction, namedAssocFunction );
        return builder.newInstance();
    }

    @Override
    public <T extends HasIdentity> Map<String, T> toValueMap( NamedAssociation<T> association )
    {
        @SuppressWarnings( "unchecked" )
        Class<T> primaryType = (Class<T>) api.associationDescriptorFor( association ).type();

        return association
            .toMap()
            .entrySet()
            .stream()
            .collect( Collectors.toMap( Map.Entry::getKey, entry -> toValue( primaryType, entry.getValue() ) ) );
    }

    @Override
    public <T extends HasIdentity> List<T> toValueList( ManyAssociation<T> association )
    {
        @SuppressWarnings( "unchecked" )
        Class<T> primaryType = (Class<T>) api.associationDescriptorFor( association ).type();

        return association
            .toList()
            .stream()
            .map( entity -> toValue( primaryType, entity ) )
            .collect( Collectors.toList() );
    }

    @Override
    public <T extends HasIdentity> Set<T> toValueSet( ManyAssociation<T> association )
    {
        @SuppressWarnings( "unchecked" )
        Class<T> primaryType = (Class<T>) api.associationDescriptorFor( association ).type();

        return association
            .toSet()
            .stream()
            .map( entity -> toValue( primaryType, entity ) )
            .collect( Collectors.toSet() );
    }

    @Override
    public <T extends HasIdentity> T toEntity( Class<T> primaryType, T valueComposite )
    {
        Objects.requireNonNull( primaryType );
        if( valueComposite == null )
        {
            return null;
        }
        Function<PropertyDescriptor, Object> propertyFunction = new ToEntityPropertyMappingFunction<>( valueComposite );
        Function<AssociationDescriptor, EntityReference> assocationFunction = new ToEntityAssociationMappingFunction<>( valueComposite );
        Function<AssociationDescriptor, Stream<EntityReference>> manyAssocFunction = new ToEntityManyAssociationMappingFunction<>( valueComposite );
        Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssocFunction = new ToEntityNameAssociationMappingFunction<>( valueComposite );
        ToEntityConverter converter = getConverter( ToEntityConverter.class );
        if( converter != null )
        {
            propertyFunction = converter.properties( valueComposite, propertyFunction );
            assocationFunction = converter.associations( valueComposite, assocationFunction );
            manyAssocFunction = converter.manyAssociations( valueComposite, manyAssocFunction );
            namedAssocFunction = converter.namedAssociations( valueComposite, namedAssocFunction );
        }
        try
        {
            T entity = get( primaryType, valueComposite.identity().get() );
            // If successful, then this entity is to by modified.
            EntityInstance instance = (EntityInstance) compositeInstanceOf( (EntityComposite) entity );
            EntityState state = instance.entityState();
            FunctionStateResolver stateResolver = new FunctionStateResolver( propertyFunction,
                                                                             assocationFunction,
                                                                             manyAssocFunction,
                                                                             namedAssocFunction );
            EntityModel model = (EntityModel) compositeInstanceOf( (Composite) entity ).descriptor();
            stateResolver.populateState( model, state );
            return entity;
        }
        catch( NoSuchEntityException e )
        {
            EntityBuilder<T> entityBuilder = newEntityBuilderWithState( primaryType,
                                                                        valueComposite.identity().get(),
                                                                        propertyFunction,
                                                                        assocationFunction,
                                                                        manyAssocFunction,
                                                                        namedAssocFunction );
            return entityBuilder.newInstance();
        }
    }

    private <T> T getConverter( Class<T> converterType )
    {
        T converter = null;
        Usecase usecase = usecase();
        if( usecase != null )
        {
            converter = usecase.metaInfo( converterType );
        }
        if( converter == null )
        {
            converter = metaInfo( converterType );
        }
        return converter;
    }

    private static EntityState getEntityState( Object entity )
    {
        return ( (EntityInstance) compositeInstanceOf( (Composite) entity ) ).entityState();
    }

    private static class UoWQuerySource
        implements QuerySource
    {
        private final ModuleUnitOfWork moduleUnitOfWork;

        private UoWQuerySource( ModuleUnitOfWork moduleUnitOfWork )
        {
            this.moduleUnitOfWork = moduleUnitOfWork;
        }

        @Override
        public <T> T find( Class<T> resultType,
                           Predicate<Composite> whereClause,
                           List<OrderBy> orderBySegments,
                           Integer firstResult,
                           Integer maxResults,
                           Map<String, Object> variables
                         )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module()
                                                              .instance()
                                                              .findService( EntityFinder.class )
                                                              .get();

            try
            {
                EntityReference foundEntity = entityFinder.findEntity( resultType, whereClause, variables == null ? Collections.emptyMap() : variables );
                if( foundEntity != null )
                {
                    try
                    {
                        return moduleUnitOfWork.get( resultType, foundEntity.identity() );
                    }
                    catch( NoSuchEntityException e )
                    {
                        return null; // Index is out of sync - entity has been removed
                    }
                }
                // No entity was found
                return null;
            }
            catch( EntityFinderException e )
            {
                throw new QueryExecutionException( "Finder caused exception", e );
            }
        }

        @Override
        public <T> long count( Class<T> resultType,
                               Predicate<Composite> whereClause,
                               List<OrderBy> orderBySegments,
                               Integer firstResult,
                               Integer maxResults,
                               Map<String, Object> variables
                             )
        {
            EntityFinder entityFinder = moduleUnitOfWork.module().instance().findService( EntityFinder.class ).get();

            try
            {
                return entityFinder.countEntities( resultType, whereClause, variables == null ? Collections.emptyMap() : variables );
            }
            catch( EntityFinderException e )
            {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public <T> Stream<T> stream( Class<T> resultType,
                                     Predicate<Composite> whereClause,
                                     List<OrderBy> orderBySegments,
                                     Integer firstResult,
                                     Integer maxResults,
                                     Map<String, Object> variables )
        {
            EntityFinder entityFinder = moduleUnitOfWork.module().instance().findService( EntityFinder.class ).get();

            try
            {
                return entityFinder.findEntities(
                    resultType,
                    whereClause,
                    orderBySegments,
                    firstResult,
                    maxResults,
                    variables == null ? Collections.emptyMap() : variables
                                                ).map( ref ->
                                                       {
                                                           try
                                                           {
                                                               return moduleUnitOfWork.get( resultType, ref.identity() );
                                                           }
                                                           catch( NoSuchEntityException e )
                                                           {
                                                               // Index is out of sync - entity has been removed
                                                               return null;
                                                           }
                                                       } );
            }
            catch( EntityFinderException e )
            {
                throw new QueryExecutionException( "Query '" + toString() + "' could not be executed", e );
            }
        }
    }

    private class ToValuePropertyMappingFunction
        implements Function<PropertyDescriptor, Object>
    {
        private Object entity;

        ToValuePropertyMappingFunction( Object entity )
        {
            this.entity = entity;
        }

        @Override
        public Object apply( PropertyDescriptor propertyDescriptor )
        {
            return getEntityState( entity ).propertyValueOf( propertyDescriptor.qualifiedName() );
        }
    }

    private class ToValueAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, EntityReference>
    {
        private final T entity;

        ToValueAssociationMappingFunction( T entity )
        {
            this.entity = entity;
        }

        @Override
        public EntityReference apply( AssociationDescriptor associationDescriptor )
        {
            return getEntityState( entity ).associationValueOf( associationDescriptor.qualifiedName() );
        }
    }

    private class ToValueManyAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, Stream<EntityReference>>
    {
        private final T entity;

        ToValueManyAssociationMappingFunction( T entity )
        {
            this.entity = entity;
        }

        @Override
        public Stream<EntityReference> apply( AssociationDescriptor associationDescriptor )
        {
            return getEntityState( entity ).manyAssociationValueOf( associationDescriptor.qualifiedName() ).stream();
        }
    }

    private class ToValueNameAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>>
    {
        private final T entity;

        ToValueNameAssociationMappingFunction( T entity )
        {
            this.entity = entity;
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>> apply( AssociationDescriptor associationDescriptor )
        {
            return getEntityState( entity ).namedAssociationValueOf( associationDescriptor.qualifiedName() ).stream();
        }

    }

    private class ToEntityPropertyMappingFunction<T>
        implements Function<PropertyDescriptor, Object>
    {
        private final T value;

        private ToEntityPropertyMappingFunction( T value )
        {
            this.value = value;
        }

        @Override
        public Object apply( PropertyDescriptor propertyDescriptor )
        {
            StateHolder state = getValueStateInstance( value );
            Property<Object> property = state.propertyFor( propertyDescriptor.accessor() );
            return property.get();
        }
    }

    private class ToEntityAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, EntityReference>
    {

        private final T value;

        private ToEntityAssociationMappingFunction( T value )
        {
            this.value = value;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public EntityReference apply( AssociationDescriptor associationDescriptor )
        {
            AssociationStateHolder state = getValueStateInstance( value );
            AssociationInstance<T> association = (AssociationInstance<T>) state.associationFor( associationDescriptor.accessor() );
            return association.getAssociationState().get();
        }
    }

    private class ToEntityManyAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, Stream<EntityReference>>
    {

        private final T value;

        private ToEntityManyAssociationMappingFunction( T valueComposite )
        {
            this.value = valueComposite;
        }

        @Override
        public Stream<EntityReference> apply( AssociationDescriptor associationDescriptor )
        {
            return getValueStateInstance( value ).manyAssociationFor( associationDescriptor.accessor() ).references();
        }
    }

    private class ToEntityNameAssociationMappingFunction<T>
        implements Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>>
    {
        private final T value;

        private ToEntityNameAssociationMappingFunction( T valueComposite )
        {
            this.value = valueComposite;
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>> apply( AssociationDescriptor associationDescriptor )
        {
            return getValueStateInstance( value ).namedAssociationFor( associationDescriptor.accessor() ).references();
        }
    }

    private static ValueStateInstance getValueStateInstance( Object value )
    {
        return (ValueStateInstance) compositeInstanceOf( (Composite) value ).state();
    }
}
