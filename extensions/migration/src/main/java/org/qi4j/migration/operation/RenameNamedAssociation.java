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
package org.qi4j.migration.operation;

import javax.json.JsonObject;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationContext;
import org.qi4j.spi.entitystore.helpers.StateStore;
import org.qi4j.migration.assembly.MigrationContext;

/**
 * Rename a NamedAssociation
 */
public class RenameNamedAssociation
    implements EntityMigrationOperation
{
    private final String from;
    private final String to;

    public RenameNamedAssociation( String from, String to )
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public JsonObject upgrade(MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.renameNamedAssociation( context, state, from, to );
    }

    @Override
    public JsonObject downgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.renameNamedAssociation( context, state, to, from );
    }

    @Override
    public String toString()
    {
        return "Rename named-association " + from + " to " + to;
    }
}
