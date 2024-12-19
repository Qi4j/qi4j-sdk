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
package org.qi4j.entitystore.mongodb;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB implementation of MapEntityStore.
 */
public class MongoDBEntityStoreMixin
    implements ServiceActivation, MapEntityStore, MongoDBAccessors
{
    private static final String DEFAULT_DATABASE_NAME = "qi4j:entitystore";
    private static final String DEFAULT_COLLECTION_NAME = "entities";
    public static final String IDENTITY_COLUMN = "_id";
    public static final String STATE_COLUMN = "state";
    @This
    private Configuration<MongoDBEntityStoreConfiguration> configuration;
    private String connectionString;
    private String databaseName;
    private String collectionName;
    private WriteConcern writeConcern;
    private String username;
    private char[] password;
    private MongoClient mongo;
    private MongoDatabase db;

    @Override
    public void activateService()
        throws Exception
    {
        loadConfiguration();

        // Create Mongo driver and open the database
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .writeConcern(writeConcern);
        if (!username.isEmpty())
        {
            MongoCredential credential = MongoCredential.createCredential(username, databaseName, password);
            builder.credential(credential);
        }
        mongo = MongoClients.create(builder.build());
        db = mongo.getDatabase(databaseName);

        // Create index if needed
        MongoCollection<Document> entities = db.getCollection(collectionName);
        try (MongoCursor<Document> iterator = entities.listIndexes().iterator())
        {
            if (!iterator.hasNext())
            {
                entities.createIndex(new BasicDBObject(IDENTITY_COLUMN, 1));
            }
        }
    }

    private void loadConfiguration()
        throws UnknownHostException
    {
        configuration.refresh();
        MongoDBEntityStoreConfiguration config = configuration.get();

        // Combine hostname, port and nodes configuration properties
        // If no configuration, use 127.0.0.1:27017
        connectionString = config.connectionString().get();
        if (connectionString == null)
        {
            connectionString = "mongodb://127.0.0.1:27017";
        }

        // If database name not configured, set it to qi4j:entitystore
        databaseName = config.database().get();
        if (databaseName == null)
        {
            databaseName = DEFAULT_DATABASE_NAME;
        }

        // If collection name not configured, set it to qi4j:entitystore:entities
        collectionName = config.collection().get();
        if (collectionName == null)
        {
            collectionName = DEFAULT_COLLECTION_NAME;
        }

        // If write concern not configured, set it to normal
        switch (config.writeConcern().get())
        {
            case W1:
                writeConcern = WriteConcern.W1;
                break;
            case W2:
                writeConcern = WriteConcern.W2;
                break;
            case W3:
                writeConcern = WriteConcern.W3;
                break;
            case UNACKNOWLEDGED:
                writeConcern = WriteConcern.UNACKNOWLEDGED;
                break;
            case JOURNALED:
                writeConcern = WriteConcern.JOURNALED;
                break;
            case MAJORITY:
                writeConcern = WriteConcern.MAJORITY;
                break;
            case ACKNOWLEDGED:
            default:
                writeConcern = WriteConcern.ACKNOWLEDGED;
        }

        // Username and password are defaulted to empty strings
        username = config.username().get();
        password = config.password().get().toCharArray();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        mongo.close();
        mongo = null;
        databaseName = null;
        collectionName = null;
        writeConcern = null;
        username = null;
        Arrays.fill(password, ' ');
        password = null;
        db = null;
    }

    @Override
    public MongoClient mongoInstanceUsed()
    {
        return mongo;
    }

    @Override
    public MongoDatabase dbInstanceUsed()
    {
        return db;
    }

    @Override
    public String collectionUsed()
    {
        return collectionName;
    }

    @Override
    public Reader get(EntityReference entityReference)
        throws EntityStoreException
    {
        try (MongoCursor<Document> cursor = db.getCollection(collectionName)
            .find(byIdentity(entityReference))
            .limit(1).iterator())
        {
            if (!cursor.hasNext())
            {
                throw new EntityNotFoundException(entityReference);
            }
            Document bsonState = (Document) cursor.next().get(STATE_COLUMN);
            String jsonState = bsonState.toJson();
            return new StringReader(jsonState);
        }
    }

    @Override
    public void applyChanges(MapChanges changes)
        throws Exception
    {
        final MongoCollection<Document> entities = db.getCollection(collectionName);

        changes.visitMap(new MapChanger()
        {
            @Override
            public Writer newEntity(EntityReference ref, EntityDescriptor entityDescriptor)
                throws IOException
            {
                return new StringWriter(1000)
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse(toString());
                        Document entity = new Document();
                        entity.put(IDENTITY_COLUMN, ref.identity().toString());
                        entity.put(STATE_COLUMN, bsonState);
                        entities.insertOne(entity);
                    }
                };
            }

            @Override
            public Writer updateEntity(MapChange mapChange)
                throws IOException
            {
                return new StringWriter(1000)
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse(toString());
                        Document entity = new Document();
                        entity.put(IDENTITY_COLUMN, mapChange.reference().identity().toString());
                        entity.put(STATE_COLUMN, bsonState);
                        entities.replaceOne(byIdentity(mapChange.reference()), entity);
                    }
                };
            }

            @Override
            public void removeEntity(EntityReference ref, EntityDescriptor entityDescriptor)
                throws EntityNotFoundException
            {
                Bson byIdFilter = byIdentity(ref);
                try (MongoCursor<Document> cursor = db.getCollection(collectionName)
                    .find(byIdFilter)
                    .limit(1).iterator())
                {
                    if (!cursor.hasNext())
                    {
                        throw new EntityNotFoundException(ref);
                    }
                    entities.deleteOne(byIdFilter);
                }
            }
        });
    }

    @Override
    public Stream<Reader> entityStates()
    {
        return StreamSupport
            .stream(db.getCollection(collectionName).find().spliterator(), false)
            .map(eachEntity ->
            {
                Document bsonState = (Document) eachEntity.get(STATE_COLUMN);
                String jsonState = bsonState.toJson();
                return new StringReader(jsonState);
            });
    }

    private Bson byIdentity(EntityReference entityReference)
    {
        return eq(IDENTITY_COLUMN, entityReference.identity().toString());
    }
}
