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

package org.qi4j.index.solr.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.index.solr.EmbeddedSolrService;
import org.qi4j.index.solr.SolrQueryService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.serialization.javaxjson.JavaxJsonFactories;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.SchemaField;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
public abstract class SolrEntityIndexerMixin
    implements SolrQueryService
{
    @Service
    private EmbeddedSolrService solr;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Uses
    private EntityStateSerializer stateSerializer;

//    private ValueFactory valueFactory = new ValueFactoryImpl();

    private SolrServer server;
    private Map<String, SchemaField> indexedFields;

    Logger logger = LoggerFactory.getLogger( getClass() );

    @Override
    public void inflateSolrSchema()
    {
        server = solr.solrServer();
        SolrCore solrCore = solr.solrCore();
        try
        {
            indexedFields = solrCore.getSchema().getFields();
        }
        finally
        {
            solrCore.close();
        }
    }

    @Override
    public void releaseSolrSchema()
    {
        server = null;
        indexedFields = null;
    }

    @Override
    public void notifyChanges( Iterable<EntityState> entityStates )
    {
        try
        {
            try
            {
                // Figure out what to update
                List<String> deleted = null;
                List<SolrInputDocument> added = new ArrayList<>();
                for( EntityState entityState : entityStates )
                {
                    if( entityState.entityDescriptor().queryable() )
                    {
                        if( entityState.status().equals( EntityStatus.REMOVED ) )
                        {
                            if( deleted == null )
                            {
                                deleted = new ArrayList<>();
                            }
                            deleted.add( entityState.entityReference().identity().toString() );
                        }
                        else if( entityState.status().equals( EntityStatus.UPDATED ) )
                        {
                            added.add( indexEntityState( entityState ) );
                        }
                        else if( entityState.status().equals( EntityStatus.NEW ) )
                        {
                            added.add( indexEntityState( entityState ) );
                        }
                    }
                }

                // Send changes to Solr
                if( deleted != null )
                {
                    server.deleteById( deleted );
                }
                if( !added.isEmpty() )
                {
                    server.add( added );
                }
            }
            finally
            {
                if( server != null )
                {
                    server.commit( false, false );
                }
            }
        }
        catch( Throwable e )
        {
            logger.error( "Could not update Solr", e );
            //TODO What shall we do with the exception?
        }
    }

    private SolrInputDocument indexEntityState( final EntityState entityState )
        throws IOException, SolrServerException
    {
        Graph graph = new GraphImpl();
        stateSerializer.serialize( entityState, false, graph );

        SolrInputDocument input = new SolrInputDocument();
        input.addField( "id", entityState.entityReference().identity() );
        input.addField( "type", entityState.entityDescriptor().types().findFirst().get().getName() );
        input.addField( "lastModified", java.util.Date.from( entityState.lastModified() ) );

        for( Statement statement : graph )
        {
            SchemaField field = indexedFields.get( statement.getPredicate().getLocalName() );
            if( field != null )
            {
                if( statement.getObject() instanceof Literal )
                {
                    String value = statement.getObject().stringValue();
                    if( field.getType().getTypeName().equals( "json" ) )
                    {
                        try( JsonParser parser = jsonFactories.parserFactory()
                                                              .createParser( new StringReader( value ) ) )
                        {
                            JsonParser.Event event = parser.next();
                            switch( event )
                            {
                                case START_ARRAY:
                                    try( JsonReader reader = jsonFactories.readerFactory()
                                                                          .createReader( new StringReader( value ) ) )
                                    {
                                        indexJson( input, reader.readArray() );
                                    }
                                    break;
                                case START_OBJECT:
                                    try( JsonReader reader = jsonFactories.readerFactory()
                                                                          .createReader( new StringReader( value ) ) )
                                    {
                                        indexJson( input, reader.readObject() );
                                    }
                                    break;
                            }
                        }
                    }
                    else
                    {
                        input.addField( field.getName(), value );
                    }
                }
                else if( statement.getObject() instanceof URI && !"type".equals( field.getName() ) )
                {
                    String value = statement.getObject().stringValue();
                    value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );
                    String name = field.getName();
                    input.addField( name, value );
                }
                else if( statement.getObject() instanceof BNode )
                {
                    Resource resource = (Resource) statement.getObject();
                    URIImpl uri = new URIImpl( "http://www.w3.org/1999/02/22-rdf-syntax-ns#li" );
                    Iterator<Statement> seq = graph.match( resource, uri, null, (Resource) null );
                    while( seq.hasNext() )
                    {
                        Statement seqStatement = seq.next();
                        String value = seqStatement.getObject().stringValue();
                        value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );

                        input.addField( field.getName(), value );
                    }
                }
            }
        }

        return input;
    }

    private void indexJson( SolrInputDocument input, Object object )
    {
        if( object instanceof JsonArray )
        {
            JsonArray array = (JsonArray) object;
            for( int i = 0; i < array.size(); i++ )
            {
                indexJson( input, array.get( i ) );
            }
        }
        else
        {
            JsonObject jsonObject = (JsonObject) object;
            for( String name : jsonObject.keySet() )
            {
                JsonValue jsonValue = jsonObject.get( name );
                if( jsonValue.getValueType() == JsonValue.ValueType.OBJECT
                    || jsonValue.getValueType() == JsonValue.ValueType.ARRAY )
                {
                    indexJson( input, jsonValue );
                }
                else
                {
                    SchemaField field = indexedFields.get( name );
                    if( field != null )
                    {
                        Object value;
                        switch( jsonValue.getValueType() )
                        {
                            case NULL:
                                value = null;
                                break;
                            case STRING:
                                value = ( (JsonString) jsonValue ).getString();
                                break;
                            case NUMBER:
                                JsonNumber jsonNumber = (JsonNumber) jsonValue;
                                value = jsonNumber.isIntegral() ? jsonNumber.longValue() : jsonNumber.doubleValue();
                                break;
                            case TRUE:
                                value = Boolean.TRUE;
                                break;
                            case FALSE:
                                value = Boolean.FALSE;
                                break;
                            default:
                                value = jsonValue.toString();
                        }
                        input.addField( name, value );
                    }
                }
            }
        }
    }
}
