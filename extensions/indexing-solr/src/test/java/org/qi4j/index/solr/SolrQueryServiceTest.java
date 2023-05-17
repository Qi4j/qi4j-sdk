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
package org.qi4j.index.solr;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.solr.assembly.SolrIndexingAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith( TemporaryFolder.class )
public class SolrQueryServiceTest
    extends AbstractQi4jTest
{
    private TemporaryFolder tmpDir;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setMode( Application.Mode.test );

        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );

        new EntityTestAssembler().assemble( module );
        // START SNIPPET: assembly
        new SolrIndexingAssembler().assemble( module );
        // END SNIPPET: assembly

        module.entities( TestEntity.class );
    }

    @BeforeEach
    public void index()
        throws UnitOfWorkCompletionException, InterruptedException
    {
        // Create and index an entity
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        TestEntity test = uow.newEntity( TestEntity.class );
        test.name().set( "Hello World" );
        uow.complete();
        Thread.sleep( 140 );
    }

    @Test
    public void testQuery()
        throws UnitOfWorkCompletionException
    {
        // Search for it
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Query<TestEntity> query = uow.newQuery( queryBuilderFactory.newQueryBuilder( TestEntity.class )
                                                                       .where( SolrExpressions.search( "hello" ) ) );

            TestEntity test = query.find();
            assertThat( test.name().get(), equalTo( "Hello World" ) );
        }
    }

    @Test
    public void testSearch()
        throws UnitOfWorkCompletionException, SolrServerException
    {
        // Search for it using search interface
        SolrSearch search = serviceFinder.findService( SolrSearch.class ).get();

        SolrDocumentList results = search.search( "hello" );

        List<String> lookAhead = new ArrayList<String>();
        for( SolrDocument result : results )
        {
            lookAhead.add( result.getFieldValue( "name" ).toString() );
        }

        assertThat( lookAhead.toString(), equalTo( "[Hello World]" ) );
    }

    public interface TestEntity
        extends EntityComposite
    {

        @UseDefaults
        Property<String> name();
    }
}
