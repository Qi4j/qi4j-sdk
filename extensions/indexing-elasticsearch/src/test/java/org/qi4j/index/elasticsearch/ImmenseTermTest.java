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

package org.qi4j.index.elasticsearch;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESClientIndexQueryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.qi4j.test.TestName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * ImmenseTermTest.
 * <p>
 * See <a href="https://ops4j1.jira.com/browse/QI-412">QI-412</a>.
 */
@SuppressWarnings( "unused" )
@ExtendWith( { TemporaryFolder.class, EmbeddedElasticSearchExtension.class, TestName.class } )
public class ImmenseTermTest
    extends AbstractQi4jTest
{
    private static EmbeddedElasticSearchExtension ELASTIC_SEARCH;

    private TestName testName;

    private TemporaryFolder tmpDir;

    @BeforeAll
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Queryable( false )
        ManyAssociation<TestEntity2> manyAssociation();
    }

    public interface TestEntity2
        extends EntityComposite
    {
        @Optional
        Property<String> property();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // EntityStore
        new EntityTestAssembler().assemble( module );

        // Index/Query
        new ESClientIndexQueryAssembler( ELASTIC_SEARCH.client() )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        ElasticSearchIndexingConfiguration esConfig = config.forMixin( ElasticSearchIndexingConfiguration.class ).declareDefaults();
        esConfig.index().set( ELASTIC_SEARCH.indexName( ElasticSearchQueryTest.class.getName(),
                                                        testName.getMethodName() ) );
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );

        // Entities & Values
        module.entities( TestEntity.class, TestEntity2.class );
    }

    @Test
    public void testManyAssociation()
        throws Exception
    {
        int count = 10_000;
        TestEntity testEntity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = uow.newEntity( TestEntity.class );
            for( int i = 0; i < count; i++ )
            {
                TestEntity2 testEntity2 = unitOfWorkFactory.currentUnitOfWork().newEntity( TestEntity2.class );
                testEntity2.property().set( "test" );
                testEntity.manyAssociation().add( testEntity2 );
            }
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = uow.get( testEntity );
            Query<TestEntity2> query = uow.newQuery(
                queryBuilderFactory.newQueryBuilder( TestEntity2.class ).where(
                    eq( templateFor( TestEntity2.class ).property(), "test" )
                )
            );
            assertThat( (int) query.count(), is( count ) );
            assertThat( testEntity.manyAssociation().count(), is( count ) );
        }
    }
}
