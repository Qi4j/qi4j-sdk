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
package org.qi4j.index.sql.assembly;

import java.io.IOException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.index.sql.SQLIndexingConfiguration;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.vendor.SQLVendorProvider;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.vendor.SQLVendorProvider;

public abstract class AbstractSQLIndexQueryAssembler<AssemblerType> extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{
    public static final Identity DEFAULT_IDENTITY = StringIdentity.identityOf( "indexing-sql" );

    private Class<? extends ReindexingStrategy> reindexingStrategy = ReindexingStrategy.NeverNeed.class;

    public AbstractSQLIndexQueryAssembler()
    {
        identifiedBy( DEFAULT_IDENTITY.toString() );
    }

    @SuppressWarnings( "unchecked" )
    public AssemblerType withReindexingStrategy( Class<? extends ReindexingStrategy> reindexingStrategy )
    {
        this.reindexingStrategy = reindexingStrategy;
        return (AssemblerType) this;
    }

    protected SQLVendor getSQLVendor()
        throws IOException
    {
        return SQLVendorProvider.createVendor( SQLVendor.class );
    }

    protected abstract Class<?> getIndexQueryServiceType();

    @Override
    public final void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        try
        {
            SQLVendor sqlVendor = getSQLVendor();
            if( sqlVendor == null )
            {
                throw new AssemblyException( "SQL Vendor could not be determined." );
            }
            module.services( getIndexQueryServiceType() )
                  .taggedWith( "sql", "query", "indexing" )
                  .identifiedBy( identity() )
                  .setMetaInfo( sqlVendor )
                  .visibleIn( visibility() )
                  .instantiateOnStartup();
        }
        catch( IOException ex )
        {
            throw new AssemblyException( "SQL Vendor could not be created", ex );
        }

        module.services( ReindexerService.class ).
            visibleIn( Visibility.module );
        module.services( ReindexingStrategy.class ).
            withMixins( reindexingStrategy ).
                  visibleIn( Visibility.module );

        if( hasConfig() )
        {
            configModule().entities( SQLIndexingConfiguration.class,
                                     ReindexerConfiguration.class ).
                              visibleIn( configVisibility() );
        }
    }
}
