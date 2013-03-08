/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.uowfile.plural;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.library.uowfile.internal.UoWFileFactory;

@Mixins( HasUoWFiles.Mixin.class )
// START SNIPPET: contract
public interface HasUoWFiles<T extends Enum<T>>
{

    /**
     * IMPORTANT Use this {@link File} only inside read-only {@link UnitOfWork}s
     */
    File attachedFile( T key );

    /**
     * IMPORTANT Use these {@link File}s only inside read-only {@link UnitOfWork}s
     */
    Iterable<File> attachedFiles();

    File managedFile( T key );

    Iterable<File> managedFiles();
    // END SNIPPET: contract

    abstract class Mixin<R extends Enum<R>>
            implements HasUoWFiles<R>
    {

        @Service
        private UoWFileFactory uowFileFactory;
        @This
        private UoWFilesLocator<R> locator;

        @Override
        public File attachedFile( R key )
        {
            return locator.locateAttachedFile( key );
        }

        @Override
        public Iterable<File> attachedFiles()
        {
            return locator.locateAttachedFiles();
        }

        @Override
        public File managedFile( R key )
        {
            return uowFileFactory.createCurrentUoWFile( locator.locateAttachedFile( key ) ).asFile();
        }

        @Override
        public Iterable<File> managedFiles()
        {
            List<File> managedFiles = new ArrayList<File>();
            for ( File eachAttachedFile : locator.locateAttachedFiles() ) {
                managedFiles.add( uowFileFactory.createCurrentUoWFile( eachAttachedFile ).asFile() );
            }
            return managedFiles;
        }

    }

    // START SNIPPET: contract
}
// END SNIPPET: contract
