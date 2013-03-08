/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.entitystore.gae2;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.util.Classes;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;

import static com.google.appengine.api.datastore.DatastoreServiceConfig.Builder.withReadPolicy;
import static org.qi4j.functional.Iterables.first;

public class GaeEntityStoreMixin
    implements GaeEntityStoreActivation, MapEntityStore
{
    @This
    private ReadWriteLock lock;

    @This
    private Configuration<GaeEntityStoreConfiguration> config;

    private DatastoreService datastore;
    private String entityKind;

    @Override
    public void activateGaeEntityStore()
        throws Exception
    {
        GaeEntityStoreConfiguration conf = config.get();
        // eventually consistent reads with a 5 second deadline
        DatastoreServiceConfig configuration =
            withReadPolicy( new ReadPolicy( ReadPolicy.Consistency.valueOf( conf.readPolicy().get().toUpperCase() ) ) )
                .deadline( conf.deadline().get() );
        datastore = DatastoreServiceFactory.getDatastoreService( configuration );
        entityKind = conf.entityKind().get();
        System.out.println( "\nActivating Google App Engine Store" +
                            "\n----------------------------------" +
                            "\n      Read Policy: " + conf.readPolicy().get() +
                            "\n         Deadline: " + conf.deadline().get() +
                            "\n      Entity Kind: " + entityKind +
                            "\n        Datastore: " + datastore +
                            "\n    Configuration: " + configuration +
                            "\n"
        );
    }

    @Override
    public Reader get( EntityReference ref )
        throws EntityStoreException
    {

        try
        {
            Key key = KeyFactory.createKey( entityKind, ref.toURI() );
            Entity entity = datastore.get( key );
            Text serializedState = (Text) entity.getProperty( "value" );
            if( serializedState == null )
            {
                throw new EntityNotFoundException( ref );
            }
            return new StringReader( serializedState.getValue() );
        }
        catch( com.google.appengine.api.datastore.EntityNotFoundException e )
        {
            e.printStackTrace();
            throw new EntityNotFoundException( ref );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException

    {
        final Transaction transaction = datastore.beginTransaction();
        try
        {
            changes.visitMap( new GaeMapChanger( transaction ) );
            transaction.commit();
        }
        catch( RuntimeException e )
        {
            if( transaction.isActive() )
            {
                transaction.rollback();
            }
            if( e instanceof EntityStoreException )
            {
                throw (EntityStoreException) e;
            }
            else
            {
                throw new IOException( e );
            }
        }
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                Query query = new Query();
                PreparedQuery preparedQuery = datastore.prepare( query );
                final QueryResultIterable<Entity> iterable = preparedQuery.asQueryResultIterable();

                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        for( Entity entity : iterable )
                        {
                            Text serializedState = (Text) entity.getProperty( "value" );
                            receiver.receive( new StringReader( serializedState.getValue() ) );
                        }
                    }
                } );
            }
        };
    }

    private class GaeMapChanger
        implements MapChanger
    {
        private final Transaction transaction;

        public GaeMapChanger( Transaction transaction )
        {
            this.transaction = transaction;
        }

        @Override
        public Writer newEntity( final EntityReference ref, final EntityDescriptor descriptor )
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    Key key = KeyFactory.createKey( entityKind, ref.toURI() );
                    Entity entity = new Entity( key );
                    Text value = new Text( toString() );
                    entity.setUnindexedProperty( "value", value );
                    entity.setProperty( "ref", ref.identity() );
                    entity.setProperty( "type", Classes.toURI( first( descriptor.types() ) ) );
                    datastore.put( transaction, entity );
                }
            };
        }

        @Override
        public Writer updateEntity( final EntityReference ref, final EntityDescriptor descriptor )
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    Key key = KeyFactory.createKey( entityKind, ref.toURI() );
                    Entity entity = new Entity( key );
                    Text value = new Text( toString() );
                    entity.setUnindexedProperty( "value", value );
                    entity.setProperty( "ref", ref.identity() );
                    entity.setProperty( "type", Classes.toURI( first( descriptor.types() ) ) );
                    datastore.put( transaction, entity );
                }
            };
        }

        @Override
        public void removeEntity( EntityReference ref, EntityDescriptor descriptor )
            throws EntityNotFoundException
        {
            Key key = KeyFactory.createKey( entityKind, ref.toURI() );
            datastore.delete( transaction, key );
        }
    }
}