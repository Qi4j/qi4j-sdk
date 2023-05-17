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
package org.qi4j.entitystore.sql;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.MaxLength;
import org.qi4j.library.sql.common.SQLConfiguration;

/**
 * Configuration for the SQL Entity Store.
 */
// START SNIPPET: config
public interface SqlEntityStoreConfiguration
{
    /**
     * Name of the entities table.
     * <p>
     * This table contains the Identity and other metadata about each entity instance
     * </p>
     */
    @UseDefaults( "ENTITIES" )
    Property<String> entitiesTableName();

    /**
     * Name of the entity types table.
     * <p>
     * This table contains the metainfo about each type. Types are versioned according to
     * application version, to support entity migration over time, and therefor there might
     * be (but not necessarily) multiple tables for entity types that has evolved beyond
     * what can be managed within a single table.
     * </p>
     */
    @UseDefaults( "TYPES" )
    Property<String> typesTableName();

    /**
     * Defines whether the database table should be created if not already present.
     */
    @UseDefaults( "true" )
    Property<Boolean> createIfMissing();

    /**
     * The SQL dialect that is being used.
     * <p>
     * Typically that is matching a supporting dialect in JOOQ.
     * See {@link org.jooq.SQLDialect} for supported values.
     * </p>
     * @return The property with the dialect value.
     */
    @UseDefaults( "" )
    Property<String> dialect();

    /** Length of Identity strings.
     *
     * MariaDb and MySQL dialects will not allow unspecified VARCHAR lengths for storing Strings.
     * <p/>
     * Default: 100
     */
    @Optional
    Property<Integer> identityLength();

    /** Length of Identity strings.
     *
     * MariaDb and MySQL dialects will not allow unspecified VARCHAR lengths for storing Strings. This configuration
     * value defines what value x in VARCHAR(x) should be used for each String property, unless otherwise specified
     * as a {@link MaxLength} annotation on the property.
     * <p/>
     * Default: 1000
     */
    @Optional
    Property<Integer> stringLength();
}
// END SNIPPET: config
