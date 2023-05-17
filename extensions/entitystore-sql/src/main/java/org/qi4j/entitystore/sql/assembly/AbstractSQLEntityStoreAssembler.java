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
package org.qi4j.entitystore.sql.assembly;

import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.JooqDslContext;
import org.qi4j.entitystore.sql.SqlEntityStoreConfiguration;
import org.qi4j.entitystore.sql.SqlEntityStoreService;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

/**
 * The common abstract part of the SQL EntityStore assembly.
 */
@SuppressWarnings( "WeakerAccess" )
public abstract class AbstractSQLEntityStoreAssembler<T extends AbstractSQLEntityStoreAssembler> extends Assemblers.VisibilityIdentityConfig<T>
    implements Assembler
{
    public static final Identity DEFAULT_ENTITYSTORE_IDENTITY = StringIdentity.identityOf( "entitystore-sql" );

    @Override
    public void assemble( ModuleAssembly module )
    {
        Settings settings = getSettings();
        if( settings == null )
        {
            throw new AssemblyException( "Settings must not be null" );
        }

        String identity = ( hasIdentity() ? identity() : DEFAULT_ENTITYSTORE_IDENTITY ).toString();
        module.transients( JooqDslContext.class );

        module.services( SqlEntityStoreService.class )
              .identifiedBy( identity )
              .visibleIn( visibility() )
              .instantiateOnStartup()
              .setMetaInfo( settings );

        if( hasConfig() )
        {
            configModule().configurations( SqlEntityStoreConfiguration.class ).visibleIn( configVisibility() );
            SqlEntityStoreConfiguration defaults = configModule().forMixin( SqlEntityStoreConfiguration.class )
                                                                 .declareDefaults();
            defaults.dialect().set( getSQLDialect().toString() );
        }
        super.assemble( module );
    }

    protected abstract SQLDialect getSQLDialect();

    protected Settings getSettings()
    {
        return new Settings().withRenderNameStyle( RenderNameStyle.QUOTED );
    }
}
