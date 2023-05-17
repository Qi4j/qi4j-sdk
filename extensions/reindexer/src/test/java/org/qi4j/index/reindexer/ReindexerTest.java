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
package org.qi4j.index.reindexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith( TemporaryFolder.class )
public class ReindexerTest
    extends AbstractQi4jTest
{
    private static final String ENTITIES_DIR = "qi4j-entities";
    private static final String INDEX_DIR = "qi4j-index";

    private TemporaryFolder tmpDir;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // JDBM EntityStore
        new JdbmEntityStoreAssembler().assemble( module );

        // Native Sesame EntityFinder
        new RdfNativeSesameStoreAssembler().assemble( module );

        // Reindexer
        // START SNIPPET: assembly
        module.services( ReindexerService.class );
        // END SNIPPET: assembly

        // Configuration
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        config.entities( JdbmEntityStoreConfiguration.class, NativeConfiguration.class, ReindexerConfiguration.class )
              .visibleIn( Visibility.layer );
        config.forMixin( JdbmEntityStoreConfiguration.class ).declareDefaults()
              .file().set( new File( tmpDir.getRoot(), ENTITIES_DIR ).getAbsolutePath() );
        config.forMixin( NativeConfiguration.class ).declareDefaults()
              .dataDirectory().set( new File( tmpDir.getRoot(), INDEX_DIR ).getAbsolutePath() );

        // Test entity
        module.entities( MyEntity.class );
    }

    private static final String TEST_NAME = "foo";

    public interface MyEntity extends EntityComposite
    {

        Property<String> name();
    }

    @Test
    public void createDataWipeIndexReindexAndAssertData()
        throws UnitOfWorkCompletionException, IOException
    {

        // ----> Create data and wipe index

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        EntityBuilder<MyEntity> eBuilder = uow.newEntityBuilder( MyEntity.class );
        MyEntity e = eBuilder.instance();
        e.name().set( TEST_NAME );
        e = eBuilder.newInstance();

        uow.complete();

        // Wipe the index data on disk
        try( Stream<Path> files = Files.walk( new File( tmpDir.getRoot(), INDEX_DIR ).getAbsoluteFile().toPath() ) )
        {
            files.map( Path::toFile ).forEach( File::delete );
        }


        // ----> Reindex and assert data

        // START SNIPPET: usage
        Reindexer reindexer = serviceFinder.findService( Reindexer.class ).get();
        reindexer.reindex();
        // END SNIPPET: usage

        uow = unitOfWorkFactory.newUnitOfWork();

        QueryBuilder<MyEntity> qBuilder = queryBuilderFactory.newQueryBuilder( MyEntity.class );
        qBuilder = qBuilder.where( eq( templateFor( MyEntity.class ).name(), TEST_NAME ) );
        Query<MyEntity> q = uow.newQuery( qBuilder );

        assertThat( q.count(), equalTo( 1L ) );
        assertThat( q.iterator().next().name().get(), equalTo( TEST_NAME ) );

        uow.complete();
    }
}
