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
package org.qi4j.migration;

import java.util.Map;
import jakarta.json.JsonObject;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.migration.assembly.MigrationContext;

/**
 * The Migrator implements this interface, which is invoked by MigrationOperation implementations
 * to perform changes to EntityState during a version migration.
 */
public interface Migrator
{
    JsonObject addProperty(ModuleDescriptor module, MigrationContext content, JsonObject state,
                           String name, Object defaultValue);

    JsonObject removeProperty(MigrationContext content, JsonObject state,
                              String name, ModuleDescriptor module);

    JsonObject renameProperty(ModuleDescriptor module, MigrationContext content, JsonObject state,
                              String from, String to);

    JsonObject addAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                              String name, String defaultReference);

    JsonObject removeAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                 String name );

    JsonObject renameAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                 String from, String to );

    JsonObject addManyAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                  String name, String... defaultReferences );

    JsonObject removeManyAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                     String name );

    JsonObject renameManyAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                     String from, String to );

    JsonObject addNamedAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                   String name, Map<String, String> defaultReferences);

    JsonObject removeNamedAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                      String name );

    JsonObject renameNamedAssociation(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                      String from, String to);

    JsonObject changeEntityType(ModuleDescriptor module, MigrationContext content, JsonObject state,
                                String fromType, String toType );
}
