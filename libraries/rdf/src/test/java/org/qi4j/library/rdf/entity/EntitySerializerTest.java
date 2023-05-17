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

package org.qi4j.library.rdf.entity;

import java.io.PrintWriter;
import java.time.Instant;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.time.SystemTime;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.DcRdf;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * JAVADOC
 */
public class EntitySerializerTest
    extends AbstractQi4jTest
{
    @Service
    EntityStore entityStore;

    @Uses
    EntityStateSerializer serializer;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.entities( TestEntity.class );
        module.values( TestValue.class, Test2Value.class );
        module.objects( EntityStateSerializer.class, EntitySerializerTest.class );
    }

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        createDummyData();
    }

    @Test
    public void testEntitySerializer()
        throws RDFHandlerException
    {
        EntityReference entityReference = EntityReference.parseEntityReference( "test2" );
        Usecase usecase = UsecaseBuilder.newUsecase( "Test" );
        Instant currentTime = SystemTime.now();
        EntityStoreUnitOfWork unitOfWork = entityStore.newUnitOfWork( module, usecase, currentTime );
        EntityState entityState = unitOfWork.entityStateOf( module, entityReference );

        Iterable<Statement> graph = serializer.serialize( entityState );

        String[] prefixes = new String[]{ "rdf", "dc", " vc" };
        String[] namespaces = new String[]{ Rdfs.RDF, DcRdf.NAMESPACE, "http://www.w3.org/2001/vcard-rdf/3.0#" };

        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ), prefixes, namespaces );
    }

    void createDummyData()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
            valueBuilder.prototype().test1().set( 4L );
            ValueBuilder<Test2Value> valueBuilder2 = valueBuilderFactory.newValueBuilder( Test2Value.class );
            valueBuilder2.prototype().data().set( "Habba" );
            valueBuilder.prototype().test3().set( valueBuilder2.newInstance() );
            TestValue testValue = valueBuilder.newInstance();

            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class, StringIdentity.identityOf( "test1" ) );
            TestEntity rickardTemplate = builder.instance();
            rickardTemplate.name().set( "Rickard" );
            rickardTemplate.title().set( "Mr" );
            rickardTemplate.value().set( testValue );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( TestEntity.class, StringIdentity.identityOf( "test2" ) );
            TestEntity niclasTemplate = builder2.instance();
            niclasTemplate.name().set( "Niclas" );
            niclasTemplate.title().set( "Mr" );
            niclasTemplate.association().set( testEntity );
            niclasTemplate.manyAssoc().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            valueBuilder = valueBuilderFactory.newValueBuilderWithPrototype( testValue );
            valueBuilder.prototype().test1().set( 5L );
            testValue = valueBuilder.newInstance();
            niclasTemplate.value().set( testValue );
            builder2.newInstance();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }
}


