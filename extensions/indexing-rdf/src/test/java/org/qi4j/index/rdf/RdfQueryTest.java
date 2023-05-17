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
package org.qi4j.index.rdf;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.qi4j.test.indexing.AbstractQueryTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith( TemporaryFolder.class )
public class RdfQueryTest
    extends AbstractQueryTest
{
    private TemporaryFolder tmpDir;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new RdfNativeSesameStoreAssembler( Visibility.module, Visibility.module ).assemble( module );

        ModuleAssembly config = module.layer().module( "Config" );
        config.entities( NativeConfiguration.class ).visibleIn( Visibility.layer );
        config.forMixin( NativeConfiguration.class ).declareDefaults()
              .dataDirectory().set( tmpDir.getRoot().getAbsolutePath() );
        new EntityTestAssembler().assemble( config );
    }

    @Test
    @Disabled( "oneOf() Query Expression not supported by RDF Indexing" )
    @Override
    public void script23()
    {
        super.script23();
    }

    @Test
    @Disabled( "Deep queries in complex values are not supported by RDF Indexing" )
    @Override
    public void script29()
    {
        super.script29();
    }

    @Test
    @Disabled( "NamedAssociation are not supported by RDF Indexing" )
    @Override
    public void script35()
    {
        super.script35();
    }

    @Test
    @Disabled( "NamedAssociation are not supported by RDF Indexing" )
    @Override
    public void script36()
    {
        super.script36();
    }

}
