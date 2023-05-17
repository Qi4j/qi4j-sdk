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
package org.qi4j.library.uowfile.plural;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins( HasUoWFilesLifecycle.Mixin.class )
public interface HasUoWFilesLifecycle<T extends Enum<T>>
    extends HasUoWFiles<T>, Lifecycle
{
    class Mixin
        implements Lifecycle
    {
        @This
        private HasUoWFiles<?> hasUoWFiles;

        @Override
        public void create()
        {
            // NOOP
        }

        @Override
        public void remove()
            throws IOException
        {
            // We use the managed files so that if the UoW gets discarded the files will be restored
            List<File> errors = new ArrayList<>();
            for( File eachFile : hasUoWFiles.managedFiles() )
            {
                if( eachFile.exists() )
                {
                    if( !eachFile.delete() )
                    {
                        errors.add( eachFile );
                    }
                }
            }
            if( !errors.isEmpty() )
            {
                throw new IOException( "Unable to delete existing files: " + errors );
            }
        }
    }

}
