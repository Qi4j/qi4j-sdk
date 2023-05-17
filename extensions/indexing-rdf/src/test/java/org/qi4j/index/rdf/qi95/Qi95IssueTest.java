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
package org.qi4j.index.rdf.qi95;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith( TemporaryFolder.class )
public class Qi95IssueTest
{
    private TemporaryFolder tmpDir;

    @Test
    public void canCreateAndQueryWithNativeRdfAndJdbm()
        throws Exception
    {
        Application application = createApplication( nativeRdf, jdbmStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithAllInMemory()
        throws Exception
    {
        Application application = createApplication( inMemoryRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithNativeRdfWithInMemoryStore()
        throws Exception
    {
        Application application = createApplication( nativeRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithInMemoryRdfWithJdbm()
        throws Exception
    {
        Application application = createApplication( inMemoryRdf, jdbmStore, domain );
        try
        {
            application.activate();

            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain );
        }
        finally
        {
            application.passivate();
        }
    }

    public void createABunchOfStuffAndDoQueries( UnitOfWorkFactory unitOfWorkFactory,
                                                 QueryBuilderFactory queryBuilderFactory
                                               )
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        newItemType( uow, "Band" );
        newItemType( uow, "Bracelet" );
        newItemType( uow, "Necklace" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<ItemType> qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> initialList = copyOf( uow.newQuery( qb ) );

        assertThat( "Band is not in the initial list", hasItemTypeNamed( "Band", initialList ), is( true ) );
        assertThat( "Bracelet is not in the initial list", hasItemTypeNamed( "Bracelet", initialList ), is( true ) );
        assertThat( "Necklace is not in the initial list", hasItemTypeNamed( "Necklace", initialList ), is( true ) );

        newItemType( uow, "Watch" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> listAfterFirstQueryAndAdd = copyOf( uow.newQuery( qb ) );

        assertThat( "Band is not in the list after the first query and add",
                    hasItemTypeNamed( "Band", listAfterFirstQueryAndAdd ), is( true ) );
        assertThat( "Bracelet is not in the list after the first query and add",
                    hasItemTypeNamed( "Bracelet", listAfterFirstQueryAndAdd ), is( true ) );
        assertThat( "Necklace is not in the list after the first query and add",
                    hasItemTypeNamed( "Necklace", listAfterFirstQueryAndAdd ), is( true ) );
        assertThat( "Watch is not in the list after the first query and add",
                    hasItemTypeNamed( "Watch", listAfterFirstQueryAndAdd ), is( true ) );

        newItemType( uow, "Ear ring" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        Iterable<ItemType> finalList = copyOf( uow.newQuery( qb ) );
        assertThat( "Band is not in the final list", hasItemTypeNamed( "Band", finalList ), is( true ) );
        assertThat( "Bracelet is not in the final list", hasItemTypeNamed( "Bracelet", finalList ), is( true ) );
        assertThat( "Necklace is not in the final list", hasItemTypeNamed( "Necklace", finalList ), is( true ) );
        assertThat( "Watch is not in the final list", hasItemTypeNamed( "Watch", finalList ), is( true ) );
        assertThat( "Ear ring is not in the final list", hasItemTypeNamed( "Ear ring", finalList ), is( true ) );
        uow.complete();
    }

    private Application createApplication( final ModuleAssemblyBuilder queryServiceModuleBuilder,
                                           final ModuleAssemblyBuilder entityStoreModuleBuilder,
                                           final LayerAssemblyBuilder domainLayerBuilder
                                         )
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();
        Application application = qi4j.newApplication( factory -> {
            ApplicationAssembly applicationAssembly = factory.newApplicationAssembly();

            LayerAssembly configLayer = applicationAssembly.layer( "Config" );
            configModule.buildModuleAssembly( configLayer, "Configuration" );

            LayerAssembly infrastructureLayer = applicationAssembly.layer( "Infrastructure" );
            infrastructureLayer.uses( configLayer );

            queryServiceModuleBuilder.buildModuleAssembly( infrastructureLayer, "Query Service" );
            entityStoreModuleBuilder.buildModuleAssembly( infrastructureLayer, "Entity Store" );

            LayerAssembly domainLayer = domainLayerBuilder.buildLayerAssembly( applicationAssembly );
            domainLayer.uses( infrastructureLayer );
            return applicationAssembly;
        } );
        return application;
    }

    interface LayerAssemblyBuilder
    {
        LayerAssembly buildLayerAssembly( ApplicationAssembly appAssembly )
            throws AssemblyException;
    }

    interface ModuleAssemblyBuilder
    {
        ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException;
    }

    final ModuleAssemblyBuilder nativeRdf =
        ( layer, name ) -> addModule( layer, name, new RdfNativeSesameStoreAssembler() );

    final ModuleAssemblyBuilder inMemoryStore =
        ( layer, name ) -> addModule( layer, name, new EntityTestAssembler().visibleIn( Visibility.application ) );

    final ModuleAssemblyBuilder inMemoryRdf =
        ( layer, name ) -> addModule( layer, name, new RdfMemoryStoreAssembler() );

    final ModuleAssemblyBuilder jdbmStore =
        ( layer, name ) -> addModule( layer, name, module -> {
            new JdbmEntityStoreAssembler().visibleIn( Visibility.application ).assemble( module );
            module.defaultServices().visibleIn( Visibility.application );
        } );

    final ModuleAssemblyBuilder configModule =
        ( layer, name ) -> addModule( layer, name, entityStoreConfigAssembler() );

    final LayerAssemblyBuilder domain =
        application -> {
            LayerAssembly domainLayer = application.layer( "Domain" );
            addModule( domainLayer, "Domain", module -> module.entities( ItemTypeEntity.class ) );
            return domainLayer;
        };

    private Assembler entityStoreConfigAssembler()
    {
        return module -> {
            new EntityTestAssembler().assemble( module );

            module.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
            module.forMixin( NativeConfiguration.class )
                  .declareDefaults()
                  .dataDirectory()
                  .set( rdfDirectory().getAbsolutePath() );

            module.entities( JdbmEntityStoreConfiguration.class ).visibleIn( Visibility.application );
            module.forMixin( JdbmEntityStoreConfiguration.class )
                  .declareDefaults()
                  .file()
                  .set( jdbmDirectory().getAbsolutePath() );
        };
    }

    private ModuleAssembly addModule( LayerAssembly layerAssembly, String name, Assembler assembler )
    {
        ModuleAssembly moduleAssembly = layerAssembly.module( name );
        try
        {
            assembler.assemble( moduleAssembly );
        }
        catch( RuntimeException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Unable to assemble module " + name + " in layer " + layerAssembly.name() );
        }
        return moduleAssembly;
    }

    private File rdfDirectory()
    {
        return createTempDirectory( "rdf-index" );
    }

    private File jdbmDirectory()
    {
        return createTempDirectory( "jdbm-store" );
    }

    private File createTempDirectory( String name )
    {
        File t = new File( tmpDir.getRoot(), name );
        t.mkdirs();
        return t;
    }

    boolean hasItemTypeNamed( String name, Iterable<ItemType> list )
    {
        for( ItemType i : list )
        {
            Property<String> nameProperty = i.name();
            String entityName = nameProperty.get();
            if( entityName.equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    private Iterable<ItemType> copyOf( Iterable<ItemType> iterable )
    {
        Collection<ItemType> copy = new ArrayList<>();
        for( ItemType i : iterable )
        {
            copy.add( i );
        }
        return Collections.unmodifiableCollection( copy );
    }

    private ItemType newItemType( UnitOfWork uow, String name )
    {
        EntityBuilder<ItemType> builder = uow.newEntityBuilder( ItemType.class );
        builder.instance().name().set( name );
        return builder.newInstance();
    }

    interface ItemType
    {
        Property<String> name();
    }

    interface ItemTypeEntity extends ItemType
    {
    }
}
