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
package org.qi4j.index.opensearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.time.SystemTime;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Classes;
import org.qi4j.serialization.jakartajson.JakartaJsonFactories;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.serialization.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen to Entity state changes and index them in ElasticSearch.
 *
 * QUID Use two indices, one for strict queries, one for full text and fuzzy search?
 */
@Mixins( OpenSearchIndexer.Mixin.class )
public interface OpenSearchIndexer extends StateChangeListener
{
    class Mixin
        implements StateChangeListener
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( OpenSearchIndexer.class );

        @Structure
        private ModuleDescriptor module;

        @Service
        private EntityStore entityStore;

        @Service
        private JsonSerializer jsonSerializer;

        @Service
        private JakartaJsonFactories jsonFactories;

        @This
        private OpenSearchSupport support;

        public void emptyIndex() throws IOException
        {
            DeleteIndexRequest request = new  DeleteIndexRequest.Builder().index(support.index()).build();
            DeleteIndexResponse response = support.client().indices().delete(request);
        }

        @Override
        public void notifyChanges( Iterable<EntityState> changedStates )
        {
            // All updated or new states
            Map<String, EntityState> newStates = new HashMap<>();
            for( EntityState eState : changedStates )
            {
                if( eState.status() == EntityStatus.UPDATED || eState.status() == EntityStatus.NEW )
                {
                    newStates.put( eState.entityReference().identity().toString(), eState );
                }
            }

            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork(
                module,
                UsecaseBuilder.newUsecase( "Load associations for indexing" ),
                SystemTime.now()
            );

            // Bulk index request builder
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

            // Handle changed entity states
            for( EntityState changedState : changedStates )
            {
                if( changedState.entityDescriptor().queryable() )
                {
                    switch( changedState.status() )
                    {
                        case REMOVED:
                            LOGGER.trace( "Removing Entity State from Index: {}", changedState );
                            remove( bulkBuilder, changedState.entityReference().identity().toString() );
                            break;
                        case UPDATED:
                            LOGGER.trace( "Updating Entity State in Index: {}", changedState );
                            remove( bulkBuilder, changedState.entityReference().identity().toString() );
                            String updatedJson = toJSON( changedState, newStates, uow ).toString();
                            LOGGER.trace( "Will index: {}", updatedJson );
                            index( bulkBuilder, changedState.entityReference().identity().toString(), updatedJson );
                            break;
                        case NEW:
                            LOGGER.trace( "Creating Entity State in Index: {}", changedState );
                            String newJson = toJSON( changedState, newStates, uow ).toString();
                            LOGGER.trace( "Will index: {}", newJson );
                            index( bulkBuilder, changedState.entityReference().identity().toString(), newJson );
                            break;
                        case LOADED:
                        default:
                            // Ignored
                            break;
                    }
                }
            }

            uow.discard();
//            if( bulkBuilder.numberOfActions() > 0 )  // can't figure out if this is possible
            {

                // Execute bulk actions
                BulkRequest bulkRequest = bulkBuilder.build();
                BulkResponse bulkResponse = null;
                try {
                    bulkResponse = support.client().bulk(bulkRequest);
                } catch (IOException e) {
                    throw new OpenSearchIndexingException( "Error executing: " + bulkRequest.toString(), e );
                }

                // Handle errors
                if( bulkResponse.errors() )
                {
                    throw new OpenSearchIndexingException( bulkResponse.items().toString() );
                }

                LOGGER.debug( "Indexing changed Entity states took: {}", bulkResponse.took() );

                // Refresh index
                try {
                    support.client().indices().refresh();
                } catch (IOException e) {
                    throw new OpenSearchIndexingException( "Unable to refresh indices.", e );
                }
            }
        }

        private void remove( BulkRequest.Builder bulkBuilder, String identity )
        {
            DeleteRequest.Builder builder = new DeleteRequest.Builder();
            builder.index(support.index()).id().;
            bulkBuilder.add( support.client().
                prepareDelete( support.index(), support.entitiesType(), identity ) );
        }

        private void index( BulkRequest.Builder bulkBuilder, String identity, String json )
        {
            bulkBuilder.add( support.client().
                prepareIndex( support.index(), support.entitiesType(), identity ).
                    setSource( json, XContentType.JSON ) );
        }

        /**
         * <pre>
         * {
         *  "_identity": "ENTITY-IDENTITY",
         *  "_types": [ "All", "Entity", "types" ],
         *  "property.name": property.value,
         *  "association.name": { "reference": "ASSOCIATED-IDENTITY" }
         *  "manyassociation.name": [ { "reference": "ASSOCIATED" }, { "reference": "IDENTITIES" } ]
         *  "namedassociation.name": [ { "_named": "NAMED", "reference": "IDENTITY" } }
         * }
         * </pre>
         */
        private JsonObject toJSON( EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {
            JsonObjectBuilder builder = jsonFactories.builderFactory().createObjectBuilder();

            builder.add( "_identity", state.entityReference().identity().toString() );

            JsonArrayBuilder typesBuilder = jsonFactories.builderFactory().createArrayBuilder();
            state.entityDescriptor().mixinTypes().map( Classes.toClassName() ).forEach( typesBuilder::add );
            builder.add( "_types", typesBuilder.build() );

            EntityDescriptor entityType = state.entityDescriptor();

            // Properties
            entityType.state().properties().forEach(
                propDesc ->
                {
                    if( propDesc.queryable() )
                    {
                        String key = propDesc.qualifiedName().name();
                        Object value = state.propertyValueOf( propDesc.qualifiedName() );
                        JsonValue jsonValue = jsonSerializer.toJson( Serializer.Options.NO_TYPE_INFO, value );
                        builder.add( key, jsonValue );
                    }
                } );

            // Associations
            entityType.state().associations().forEach(
                assocDesc ->
                {
                    if( assocDesc.queryable() )
                    {
                        String key = assocDesc.qualifiedName().name();
                        EntityReference associated = state.associationValueOf( assocDesc.qualifiedName() );
                        if( associated == null )
                        {
                            builder.add( key, JsonValue.NULL );
                        }
                        else
                        {
                            if( assocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity().toString() ) )
                                {
                                    builder.add( key, toJSON( newStates.get( associated.identity().toString() ),
                                                              newStates, uow ) );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( associated.identity() );
                                    EntityState assocState = uow.entityStateOf( entityType.module(), reference );
                                    builder.add( key, toJSON( assocState, newStates, uow ) );
                                }
                            }
                            else
                            {
                                builder.add( key, jsonFactories.builderFactory().createObjectBuilder()
                                                               .add( "reference", associated.identity().toString() ) );
                            }
                        }
                    }
                } );

            // ManyAssociations
            entityType.state().manyAssociations().forEach(
                manyAssocDesc ->
                {
                    if( manyAssocDesc.queryable() )
                    {
                        String key = manyAssocDesc.qualifiedName().name();
                        JsonArrayBuilder assBuilder = jsonFactories.builderFactory().createArrayBuilder();
                        ManyAssociationState assocs = state.manyAssociationValueOf( manyAssocDesc.qualifiedName() );
                        for( EntityReference associated : assocs )
                        {
                            if( manyAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity().toString() ) )
                                {
                                    assBuilder.add( toJSON( newStates.get( associated.identity().toString() ),
                                                            newStates, uow ) );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( associated.identity() );
                                    EntityState assocState = uow.entityStateOf( entityType.module(), reference );
                                    assBuilder.add( toJSON( assocState, newStates, uow ) );
                                }
                            }
                            else
                            {
                                assBuilder.add( jsonFactories.builderFactory().createObjectBuilder()
                                                             .add( "reference",
                                                                   associated.identity().toString() ) );
                            }
                        }
                        builder.add( key, assBuilder.build() );
                    }
                } );

            // NamedAssociations
            entityType.state().namedAssociations().forEach(
                namedAssocDesc ->
                {
                    if( namedAssocDesc.queryable() )
                    {
                        String key = namedAssocDesc.qualifiedName().name();
                        JsonArrayBuilder assBuilder = jsonFactories.builderFactory().createArrayBuilder();
                        NamedAssociationState assocs = state.namedAssociationValueOf(
                            namedAssocDesc.qualifiedName() );
                        for( String name : assocs )
                        {
                            Identity identity = assocs.get( name ).identity();
                            if( namedAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                String identityString = identity.toString();
                                if( newStates.containsKey( identityString ) )
                                {
                                    assBuilder.add(
                                        jsonFactories.cloneBuilder( toJSON( newStates.get( identityString ),
                                                                            newStates, uow ) )
                                                     .add( "_named", name )
                                                     .build() );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( identity );
                                    EntityState assocState = uow.entityStateOf( entityType.module(), reference );
                                    assBuilder.add(
                                        jsonFactories.cloneBuilder( toJSON( assocState, newStates, uow ) )
                                                     .add( "_named", name ).build() );
                                }
                            }
                            else
                            {
                                assBuilder.add( jsonFactories.builderFactory().createObjectBuilder()
                                                             .add( "_named", name )
                                                             .add( "reference", identity.toString() )
                                                             .build() );
                            }
                        }
                        builder.add( key, assBuilder.build() );
                    }
                } );
            return builder.build();
        }
    }
}
