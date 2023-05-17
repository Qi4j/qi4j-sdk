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

package org.qi4j.library.rest.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.FreePortFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class RestTest extends AbstractQi4jTest
{
    private static final int ADMIN_PORT = FreePortFinder.findFreePortOnLoopback();

    @Override
    protected ApplicationDescriptor newApplicationModel()
        throws AssemblyException
    {
        return qi4j.newApplicationModel( new ApplicationAssemblerAdapter(
                                                 new Assembler[][][]
                                                     {
                                                         {
                                                             {
                                                                 RestTest.this,
                                                                 new RestAssembler(),
                                                                 new RdfMemoryStoreAssembler()
                                                             }
                                                         }
                                                     } )
                                             {
                                             }  // subclassing ApplicationAssemblerAdapter
        );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        config.configurations( RestServerConfiguration.class ).visibleIn( Visibility.layer );
        config.forMixin( RestServerConfiguration.class ).declareDefaults().port().set( ADMIN_PORT );

        module.objects( RestTest.class, RestTester.class );
        module.entities( PersonEntity.class );
        module.services( RestServerComposite.class ).instantiateOnStartup();
        new EntityTestAssembler().assemble( module );
    }

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder1 = uow.newEntityBuilder( PersonEntity.class, StringIdentity.identityOf( "P2" ) );
            PersonEntity maryDoe = builder1.instance();
            maryDoe.firstname().set( "Mary" );
            maryDoe.lastname().set( "Doe" );
            maryDoe = builder1.newInstance();

            EntityBuilder<PersonEntity> builder2 = uow.newEntityBuilder( PersonEntity.class, StringIdentity.identityOf( "P1" ) );
            PersonEntity joeDoe = builder2.instance();
            joeDoe.firstname().set( "Joe" );
            joeDoe.lastname().set( "Doe" );
            joeDoe.mother().set( maryDoe );
            builder2.newInstance();

            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenAnIdentityWhenExecutingGetCommandThenExpectTheCorrectRdf()
        throws Exception
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        String rdf = restTester.getEntity( "P1" );
        // System.out.println( rdf.replaceAll( "\n", "\\\\n" ).replaceAll( "\"", "\\\\\"" ) );
        assertThat( "Incorrect RDF produced", rdf, anyOf(
            // Open JDK 8 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.qi4j.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:qi4j:type:\" rdf:about=\"urn:qi4j:entity:P1\">\n\t<lastname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Doe</lastname>\n\t<firstname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Joe</firstname>\n\t<identity xmlns=\"urn:qi4j:type:org.qi4j.api.identity.HasIdentity#\">P1</identity>\n\t<mother xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:qi4j:entity:P2\"/>\n</org.qi4j.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.qi4j.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:qi4j:type:\" rdf:about=\"urn:qi4j:entity:P1\">\n\t<identity xmlns=\"urn:qi4j:type:org.qi4j.api.identity.HasIdentity#\">P1</identity>\n\t<firstname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Joe</firstname>\n\t<lastname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Doe</lastname>\n\t<mother xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:qi4j:entity:P2\"/>\n</org.qi4j.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            // Sun JDK 6 / Oracle JDK 7 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.qi4j.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:qi4j:type:\" rdf:about=\"urn:qi4j:entity:P1\">\n\t<firstname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Joe</firstname>\n\t<lastname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Doe</lastname>\n\t<identity xmlns=\"urn:qi4j:type:org.qi4j.api.identity.HasIdentity#\">P1</identity>\n\t<mother xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:qi4j:entity:P2\"/>\n</org.qi4j.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            // IBM JDK 6 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.qi4j.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:qi4j:type:\" rdf:about=\"urn:qi4j:entity:P1\">\n\t<identity xmlns=\"urn:qi4j:type:org.qi4j.api.identity.HasIdentity#\">P1</identity>\n\t<lastname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Doe</lastname>\n\t<firstname xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\">Joe</firstname>\n\t<mother xmlns=\"urn:qi4j:type:org.qi4j.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:qi4j:entity:P2\"/>\n</org.qi4j.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ) ) );
    }

    @Test
    public void givenExistingIdentityWhenExecutingPutCommandThenNewValuesInEntity()
        throws Throwable
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "reference", "P1" );
        properties.put( "firstname", "Jack" );
        properties.put( "lastname", "Doe" );
        restTester.putEntity( "P1", properties );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = work.get( PersonEntity.class, StringIdentity.identityOf( "P1" ) );
            assertThat( "FirstName not changed.", entity.firstname().get(), equalTo( "Jack" ) );
            assertThat( "LastName not changed.", entity.lastname().get(), equalTo( "Doe" ) );
            work.complete();
        }
        finally
        {
            work.discard();
        }
    }

    @Test
    public void givenExistingIdentityWhenExecutingDeleteCommandThenEntityIsRemoved()
        throws Throwable
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        restTester.deleteEntity( "P1" );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = null;
            try
            {
                entity = work.get( PersonEntity.class, StringIdentity.identityOf( "P1" ) );
            }
            catch( NoSuchEntityException expected )
            {
                // expected
            }
            assertThat( "Entity not removed.", entity, nullValue() );
            work.complete();
        }
        finally
        {
            work.discard();
        }
    }

    @Test
    public void givenExistingEntitiesWhenExecutingGetCommandThenExpectTheCorrectRdf()
        throws Exception
    {
        final RestTester restTester = objectFactory.newObject( RestTester.class );
        final String result = restTester.getEntities().replace( "\r", "" );
        assertThat(
            "Returned RDF", result,
            anyOf(
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns=\"urn:qi4j:\"\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<qi4j:entity rdf:about=\"/entity/P2.rdf\"/>\n<qi4j:entity rdf:about=\"/entity/P1.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\\\"no\\\"?>\n<rdf:RDF\n\txmlns=\"urn:qi4j:\"\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<qi4j:entity rdf:about=\"/entity/P2.rdf\"/>\n<qi4j:entity rdf:about=\"/entity/P1.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns=\"urn:qi4j:\"\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<qi4j:entity rdf:about=\"/entity/P1.rdf\"/>\n<qi4j:entity rdf:about=\"/entity/P2.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\\\"no\\\"?>\n<rdf:RDF\n\txmlns=\"urn:qi4j:\"\n\txmlns:qi4j=\"http://qi4j.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<qi4j:entity rdf:about=\"/entity/P1.rdf\"/>\n<qi4j:entity rdf:about=\"/entity/P2.rdf\"/>\n</rdf:RDF>\n" ) ) );
    }

    public static class RestTester
    {

        @Service
        private RestServer server;

        public String getEntity( String identity )
            throws IOException
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet method = new HttpGet( "http://localhost:" + ADMIN_PORT + "/entity/" + identity + ".rdf" );
            method.addHeader( "Accept", "application/rdf+xml" );
            try (CloseableHttpResponse response = client.execute( method ))
            {
                if( response.getStatusLine().getStatusCode() != 200 )
                {
                    throw new RuntimeException( "EntityResource returned status: " + response.getStatusLine() );
                }
                return EntityUtils.toString( response.getEntity(), StandardCharsets.UTF_8 );
            }
        }

        public void putEntity( String identity, Map<String, String> params )
            throws IOException
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost method = new HttpPost( "http://localhost:" + ADMIN_PORT + "/entity/" + identity );
            List<NameValuePair> parameters = new ArrayList<>();
            for( Map.Entry<String, String> entry : params.entrySet() )
            {
                parameters.add( new BasicNameValuePair( entry.getKey(), entry.getValue() ) );
            }
            method.setEntity( new UrlEncodedFormEntity( parameters ) );
            try (CloseableHttpResponse response = client.execute( method ))
            {
                if( response.getStatusLine().getStatusCode() != 205 )
                {
                    throw new RuntimeException( "EntityResource returned status: " + response.getStatusLine() );
                }
            }
        }

        public void deleteEntity( String identity )
            throws IOException
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpDelete method = new HttpDelete( "http://localhost:" + ADMIN_PORT + "/entity/" + identity );
            try (CloseableHttpResponse response = client.execute( method ))
            {
                if( response.getStatusLine().getStatusCode() != 204 )
                {
                    throw new RuntimeException( "EntityResource returned status: " + response.getStatusLine() );
                }
            }
        }

        public String getEntities()
            throws IOException
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet method = new HttpGet( "http://localhost:" + ADMIN_PORT + "/entity.rdf" );
            method.addHeader( "Accept", "application/rdf+xml" );
            try (CloseableHttpResponse response = client.execute( method ))
            {
                if( response.getStatusLine().getStatusCode() != 200 )
                {
                    throw new RuntimeException( "EntityResource returned status: " + response.getStatusLine() );
                }
                return EntityUtils.toString( response.getEntity(), StandardCharsets.UTF_8 );
            }
        }
    }

    public interface PersonEntity
        extends EntityComposite, Person
    {
    }

    public interface Person
    {

        Property<String> firstname();

        Property<String> lastname();

        @Optional
        Association<Person> mother();
    }
}
