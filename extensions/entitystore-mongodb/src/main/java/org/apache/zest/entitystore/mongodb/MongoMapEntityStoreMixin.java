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
package org.apache.zest.entitystore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB implementation of MapEntityStore.
 */
public class MongoMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore, MongoAccessors
{
    private static final String DEFAULT_DATABASE_NAME = "zest:entitystore";
    private static final String DEFAULT_COLLECTION_NAME = "zest:entitystore:entities";
    public static final String IDENTITY_COLUMN = "_id";
    public static final String STATE_COLUMN = "state";
    @This
    private Configuration<MongoEntityStoreConfiguration> configuration;
    private List<ServerAddress> serverAddresses;
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
        MongoClientOptions options = MongoClientOptions.builder().writeConcern( writeConcern ).build();
        if( username.isEmpty() )
        {
            mongo = new MongoClient( serverAddresses, options );
        }
        else
        {
            MongoCredential credential = MongoCredential.createMongoCRCredential( username, databaseName, password );
            mongo = new MongoClient( serverAddresses, Collections.singletonList( credential ), options );
        }
        db = mongo.getDatabase( databaseName );

        // Create index if needed
        MongoCollection<Document> entities = db.getCollection( collectionName );
        if( !entities.listIndexes().iterator().hasNext() )
        {
            entities.createIndex( new BasicDBObject( IDENTITY_COLUMN, 1 ) );
        }
    }

    private void loadConfiguration()
        throws UnknownHostException
    {
        configuration.refresh();
        MongoEntityStoreConfiguration config = configuration.get();

        // Combine hostname, port and nodes configuration properties
        // If no configuration, use 127.0.0.1:27017
        serverAddresses = new ArrayList<>();
        int port = config.port().get() == null ? 27017 : config.port().get();
        if( config.nodes().get().isEmpty() )
        {
            String hostname = config.hostname().get() == null ? "127.0.0.1" : config.hostname().get();
            serverAddresses.add( new ServerAddress( hostname, port ) );
        }
        else
        {
            if( config.hostname().get() != null && !config.hostname().get().isEmpty() )
            {
                serverAddresses.add( new ServerAddress( config.hostname().get(), port ) );
            }
            serverAddresses.addAll( config.nodes().get() );
        }

        // If database name not configured, set it to zest:entitystore
        databaseName = config.database().get();
        if( databaseName == null )
        {
            databaseName = DEFAULT_DATABASE_NAME;
        }

        // If collection name not configured, set it to zest:entitystore:entities
        collectionName = config.collection().get();
        if( collectionName == null )
        {
            collectionName = DEFAULT_COLLECTION_NAME;
        }

        // If write concern not configured, set it to normal
        switch( config.writeConcern().get() )
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
        Arrays.fill( password, ' ' );
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
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        MongoCursor<Document> cursor = db.getCollection( collectionName )
                                         .find( byIdentity( entityReference ) )
                                         .limit( 1 ).iterator();
        if( !cursor.hasNext() )
        {
            throw new EntityNotFoundException( entityReference );
        }
        Document bsonState = (Document) cursor.next().get( STATE_COLUMN );
        String jsonState = JSON.serialize( bsonState );
        return new StringReader( jsonState );
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        final MongoCollection<Document> entities = db.getCollection( collectionName );

        changes.visitMap( new MapChanger()
        {
            @Override
            public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse( toString() );
                        Document entity = new Document();
                        entity.put( IDENTITY_COLUMN, ref.identity().toString() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.insertOne( entity );
                    }
                };
            }

            @Override
            public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse( toString() );
                        Document entity = new Document();
                        entity.put( IDENTITY_COLUMN, ref.identity().toString() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.replaceOne( byIdentity( ref ), entity );
                    }
                };
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws EntityNotFoundException
            {
                Bson byIdFilter = byIdentity( ref );
                MongoCursor<Document> cursor = db.getCollection( collectionName )
                                                 .find( byIdFilter )
                                                 .limit( 1 ).iterator();
                if( !cursor.hasNext() )
                {
                    throw new EntityNotFoundException( ref );
                }
                entities.deleteOne( byIdFilter );
            }
        } );
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo(
                Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo(
                        Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        FindIterable<Document> cursor = db.getCollection( collectionName ).find();
                        for( Document eachEntity : cursor )
                        {
                            Document bsonState = (Document) eachEntity.get( STATE_COLUMN );
                            String jsonState = JSON.serialize( bsonState );
                            receiver.receive( new StringReader( jsonState ) );
                        }
                    }
                } );
            }
        };
    }

    private Bson byIdentity( EntityReference entityReference )
    {
        return eq( IDENTITY_COLUMN, entityReference.identity().toString() );
    }
}
