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

import java.util.Map;
import javax.json.JsonObject;
import org.qi4j.migration.assembly.MigrationContext;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.spi.entitystore.helpers.StateStore;
import org.qi4j.migration.assembly.MigrationContext;

/**
 * Add a named-association
 */
public class AddNamedAssociation
    implements EntityMigrationOperation
{
    private final String association;
    private final Map<String, String> defaultReferences;

    public AddNamedAssociation( String association, Map<String, String> defaultReferences )
    {
        this.association = association;
        this.defaultReferences = defaultReferences;
    }

    @Override
    public JsonObject upgrade(MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.addNamedAssociation( context, state, association, defaultReferences );
    }

    @Override
    public JsonObject downgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.removeNamedAssociation( context, state, association );
    }

    @Override
    public String toString()
    {
        return "Add named-association " + association + ", default:" + defaultReferences;
    }
}
