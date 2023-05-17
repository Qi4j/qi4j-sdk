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

package org.qi4j.migration.assembly;

/**
 * Builder for set of migration rules. This needs to be passed in as metadata to the MigrationService.
 */
public class MigrationBuilder
{
    private MigrationRules<EntityMigrationRule> entityRules;
    private MigrationRules<MigrationRule> rules;

    private String fromVersion;

    public MigrationBuilder( String fromVersion )
    {
        this.entityRules = new MigrationRules<>();
        this.rules = new MigrationRules<>();
        this.fromVersion = fromVersion;
    }

    public VersionMigrationBuilder toVersion( String toVersion )
    {
        return new VersionMigrationBuilder( this, fromVersion, toVersion );
    }

    public MigrationRules<EntityMigrationRule> entityMigrationRules()
    {
        return entityRules;
    }

    public MigrationRules<MigrationRule> migrationRules()
    {
        return rules;
    }
}
