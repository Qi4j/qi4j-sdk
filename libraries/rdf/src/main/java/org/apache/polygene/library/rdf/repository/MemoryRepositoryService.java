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

package org.apache.polygene.library.rdf.repository;

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;

@Mixins( MemoryRepositoryService.MemoryRepositoryMixin.class )
@Activators( MemoryRepositoryService.Activator.class )
public interface MemoryRepositoryService
    extends Repository
{

    @Override
    void initialize()
        throws RepositoryException;

    @Override
    void shutDown()
        throws RepositoryException;

    class Activator
        extends ActivatorAdapter<ServiceReference<MemoryRepositoryService>>
    {

        @Override
        public void afterActivation( ServiceReference<MemoryRepositoryService> activated )
            throws Exception
        {
            activated.get().initialize();
        }

        @Override
        public void beforePassivation( ServiceReference<MemoryRepositoryService> passivating )
            throws Exception
        {
            passivating.get().shutDown();
        }
    }

    abstract class MemoryRepositoryMixin
        implements MemoryRepositoryService, ResetableRepository
    {

        SailRepository repo;

        public MemoryRepositoryMixin()
        {
            repo = new SailRepository( new MemoryStore() );
        }

        @Override
        public void initialize()
            throws RepositoryException
        {
            repo.initialize();
        }

        @Override
        public boolean isInitialized()
        {
            return repo.isInitialized();
        }

        @Override
        public void shutDown()
            throws RepositoryException
        {
            repo.shutDown();
        }

        @Override
        public void setDataDir( File dataDir )
        {
            repo.setDataDir( dataDir );
        }

        @Override
        public File getDataDir()
        {
            return repo.getDataDir();
        }

        @Override
        public boolean isWritable()
            throws RepositoryException
        {
            return repo.isWritable();
        }

        @Override
        public RepositoryConnection getConnection()
            throws RepositoryException
        {
            return repo.getConnection();
        }

        @Override
        public ValueFactory getValueFactory()
        {
            return repo.getValueFactory();
        }

        @Override
        public void discardEntireRepository()
            throws RepositoryException
        {
            repo = new SailRepository( new MemoryStore() );
        }
    }
}
