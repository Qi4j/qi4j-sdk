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
package org.qi4j.library.uowfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.api.unitofwork.concern.UnitOfWorkRetry;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.uowfile.bootstrap.UoWFileAssembler;
import org.qi4j.library.uowfile.internal.ConcurrentUoWFileModificationException;
import org.qi4j.library.uowfile.plural.HasUoWFilesLifecycle;
import org.qi4j.library.uowfile.plural.UoWFilesLocator;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith( TemporaryFolder.class )
public class HasUoWFilesTest
    extends AbstractQi4jTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( HasUoWFilesTest.class );
    private static final URL CREATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "creation.txt" );
    private static final URL MODIFICATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "modification.txt" );

    private TemporaryFolder tmpDir;

    // START SNIPPET: uowfile
    public enum MyEnum
    {
        fileOne, fileTwo
    }

    // START SNIPPET: entity
    public interface TestedEntity
        extends HasUoWFilesLifecycle<MyEnum> // END SNIPPET: entity
        , HasIdentity
    // START SNIPPET: entity
    {
        Property<String> name();
    }
    // END SNIPPET: entity
    // END SNIPPET: uowfile

    // START SNIPPET: locator
    public static abstract class TestedFilesLocatorMixin
        implements UoWFilesLocator<MyEnum>
    {
        @This
        private HasIdentity meAsIdentity;

        @Structure
        private Qi4jSPI spi;

        @Override
        public Iterable<File> locateAttachedFiles()
        {
            File baseDir = spi.entityDescriptorFor( meAsIdentity ).metaInfo( File.class );
            List<File> list = new ArrayList<>();
            for( MyEnum eachValue : MyEnum.values() )
            {
                list.add( new File( baseDir, meAsIdentity.identity().get() + "." + eachValue.name() ) );
            }
            return list;
        }

        @Override
        public File locateAttachedFile( MyEnum key )
        {
            File baseDir = spi.entityDescriptorFor( meAsIdentity ).metaInfo( File.class );
            return new File( baseDir, meAsIdentity.identity().get() + "." + key.name() );
        }
    }
    // END SNIPPET: locator

    @Mixins( TestServiceMixin.class )
    @Concerns( UnitOfWorkConcern.class )
    public interface TestService
    {
        void modifyFile( Identity entityId )
            throws IOException;

        @UnitOfWorkPropagation
        @UnitOfWorkRetry
        void modifyFileWithRetry( Identity entityId, long sleepBefore, long sleepAfter )
            throws IOException;
    }

    public static class TestServiceMixin
        implements TestService
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void modifyFile( Identity entityId )
            throws IOException
        {
            modifyFileImmediatly( entityId );
        }

        @Override
        public void modifyFileWithRetry( Identity entityId, long sleepBefore, long sleepAfter )
            throws IOException
        {
            LOGGER.info( "Waiting " + sleepBefore + "ms before file modification" );
            if( sleepBefore > 0 )
            {
                try
                {
                    Thread.sleep( sleepBefore );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
            modifyFileImmediatly( entityId );
            LOGGER.info( "Waiting " + sleepAfter + "ms after file modification" );
            if( sleepAfter > 0 )
            {
                try
                {
                    Thread.sleep( sleepAfter );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }

        private void modifyFileImmediatly( Identity entityId )
            throws IOException
        {
            TestedEntity entity = uowf.currentUnitOfWork().get( TestedEntity.class, entityId );
            // START SNIPPET: api
            File attachedFileTwo = entity.attachedFile( MyEnum.fileTwo );
            File managedFileOne = entity.managedFile( MyEnum.fileOne );
            // END SNIPPET: api
            try( InputStream input = MODIFICATION_CONTENT_URL.openStream() )
            {
                Files.copy( input, managedFileOne.toPath(), REPLACE_EXISTING );
            }
        }
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new UoWFileAssembler().assemble( module );

        module.entities( TestedEntity.class ).withMixins( TestedFilesLocatorMixin.class );
        // END SNIPPET: assembly
        module.entities( TestedEntity.class ).setMetaInfo( tmpDir.getRoot() );
        module.services( TestService.class );
        new EntityTestAssembler().assemble( module );
        new FileConfigurationAssembler().assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    private TestService testService;

    @BeforeEach
    public void beforeTest()
    {
        testService = serviceFinder.findService( TestService.class ).get();
    }

    @Test
    public void testCreation()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Creation ##############################################################################" );
        File attachedFile;

        // Test discarded creation
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation Rollback" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
        }
        assertThat( "File still exists after discarded creation UoW", attachedFile.exists(), is( false ) );

        // Test completed creation
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }
        try( Stream<String> lines = Files.lines( attachedFile.toPath() ) )
        {
            assertThat("File content was not the good one",
                       lines.limit( 1 ).findFirst().get(),
                       equalTo( "Creation" ) );
        }
    }

    @Test
    public void testModification()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Modification ##########################################################################" );
        final Identity entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded modification
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
        }
        try( Stream<String> lines = Files.lines( attachedFile.toPath() ) )
        {
            assertThat("File content after discarded modification was not the good one",
                       lines.limit( 1 ).findFirst().get(),
                       equalTo( "Creation" ) );
        }

        // Testing completed modification
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
            uow.complete();
        }
        try( Stream<String> lines = Files.lines( attachedFile.toPath() ) )
        {
            assertThat("Modified file content was not the good one",
                       lines.limit( 1 ).findFirst().get(),
                       equalTo( "Modification" ) );
        }
    }

    @Test
    public void testDeletion()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Deletion ##############################################################################" );
        final Identity entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Deletion" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded deletion
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
        }
        assertThat( "File do not exists after discarded deletion", attachedFile.exists(), is( true ) );

        // Testing completed deletion
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
            uow.complete();
        }
        assertThat( "File still exists after deletion", attachedFile.exists(), is( false ) );
    }

    @Test
    public void testConcurrentModification()
        throws IOException, UnitOfWorkCompletionException
    {
        LOGGER.info( "# Test Concurrent Modification ###############################################################" );
        final Identity entityId;

        // Create new
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            uow.complete();
        }

        // Testing concurrent modification
        UnitOfWork uow, uow2;
        TestedEntity entity;

        uow = unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        try( InputStream input = MODIFICATION_CONTENT_URL.openStream() )
        {
            Files.copy( input, entity.managedFile( MyEnum.fileOne ).toPath(), REPLACE_EXISTING );
        }

        uow2 = unitOfWorkFactory.newUnitOfWork();
        entity = uow2.get( TestedEntity.class, entityId );
        try( InputStream input = MODIFICATION_CONTENT_URL.openStream() )
        {
            Files.copy( input, entity.managedFile( MyEnum.fileOne ).toPath(), REPLACE_EXISTING );
        }

        uow.complete();
        try
        {
            uow2.complete();
            fail( "A ConcurrentUoWFileModificationException should have been raised" );
        }
        catch( ConcurrentUoWFileModificationException expected )
        {
            uow2.discard();
        }
    }

    @Test
    public void testRetry()
        throws IOException, UnitOfWorkCompletionException, InterruptedException
    {
        LOGGER.info( "# Test Retry #################################################################################" );
        final Identity entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        final List<Exception> ex = new ArrayList<>();
        Thread t1 = new Thread(() ->
        {
            try
            {
                testService.modifyFileWithRetry( entityId, 0, 10000 );
            }
            catch( Exception ex1 )
            {
                ex.add( ex1 );
            }
        }, "job1" );
        Thread t2 = new Thread(() ->
        {
            try
            {
                testService.modifyFileWithRetry( entityId, 5000, 0 );
            }
            catch( Exception ex1 )
            {
                ex.add( ex1 );
            }
        }, "job2" );

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        for( Exception eachEx : ex )
        {
            eachEx.printStackTrace();
        }

        assertThat( "There were errors during TestRetry", ex.isEmpty(), is( true ) );
        try( Stream<String> lines = Files.lines( attachedFile.toPath() ) )
        {
            assertThat("Modified file content was not the good one",
                       lines.limit( 1 ).findFirst().get(),
                       equalTo( "Modification" ) );
        }
    }

    private TestedEntity createTestedOneEntityTwoFilesEntity( UnitOfWork uow, String name )
        throws IOException
    {
        EntityBuilder<TestedEntity> builder = uow.newEntityBuilder( TestedEntity.class );
        TestedEntity entity = builder.instance();
        entity.name().set( name );
        entity = builder.newInstance();
        try( InputStream input = CREATION_CONTENT_URL.openStream() )
        {
            Files.copy( input, entity.managedFile( MyEnum.fileOne ).toPath() );
        }
        return entity;
    }

}
