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
 */
package org.qi4j.entitystore.sqlkv.assembly;

import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sqlkv.SQLEntityStoreConfiguration;
import org.qi4j.entitystore.sqlkv.SQLEntityStoreService;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

/**
 * Base SQL EntityStore assembly.
 */
public abstract class AbstractSQLEntityStoreAssembler<AssemblerType> extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{
    public static final Identity DEFAULT_ENTITYSTORE_IDENTITY = StringIdentity.identityOf( "entitystore-sqlkv" );
    private static final String DEFAULT_CHANGELOG_PATH = "org/qi4j/entitystore/sql/changelog.xml";

    private String changelogPath = DEFAULT_CHANGELOG_PATH;

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        SQLDialect dialect = getSQLDialect();
        if( dialect == null )
        {
            throw new AssemblyException( "SQLDialect must not be null" );
        }
        Settings settings = getSettings();
        if( settings == null )
        {
            throw new AssemblyException( "Settings must not be null" );
        }

        String identity = ( hasIdentity() ? identity() : DEFAULT_ENTITYSTORE_IDENTITY ).toString();

        module.services( SQLEntityStoreService.class )
              .identifiedBy( identity )
              .visibleIn( visibility() )
              .setMetaInfo( dialect )
              .setMetaInfo( settings );

        if( hasConfig() )
        {
            configModule().entities( SQLEntityStoreConfiguration.class ).visibleIn( configVisibility() );
        }
    }

    protected Settings getSettings()
    {
        return new Settings().withRenderNameStyle( RenderNameStyle.QUOTED );
    }

    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.DEFAULT;
    }
}
