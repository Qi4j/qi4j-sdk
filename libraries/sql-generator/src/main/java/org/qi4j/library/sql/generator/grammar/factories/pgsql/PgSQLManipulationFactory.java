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
package org.qi4j.library.sql.generator.grammar.factories.pgsql;

import org.qi4j.library.sql.generator.grammar.common.TableNameDirect;
import org.qi4j.library.sql.generator.grammar.factories.ManipulationFactory;
import org.qi4j.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.qi4j.library.sql.generator.grammar.manipulation.ObjectType;
import org.qi4j.library.sql.generator.grammar.manipulation.pgsql.PgSQLDropTableOrViewStatement;
import org.qi4j.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.qi4j.library.sql.generator.grammar.manipulation.ObjectType;
import org.qi4j.library.sql.generator.grammar.manipulation.pgsql.PgSQLDropTableOrViewStatement;

/**
 */
public interface PgSQLManipulationFactory
    extends ManipulationFactory
{

    PgSQLDropTableOrViewStatement createDropTableOrViewStatement(TableNameDirect tableName, ObjectType theType,
                                                                 DropBehaviour dropBehaviour );

    /**
     * Creates {@code DROP TABLE/VIEW} statement, which may use {@code IF EXISTS} clause before the table name.
     *
     * @param tableName     The name of the table/view to drop.
     * @param theType       What to drop - {@link ObjectType#TABLE} or {@link ObjectType#VIEW}.
     * @param dropBehaviour Drop behaviour - {@link DropBehaviour#CASCADE} or {@link DropBehaviour#RESTRICT}.
     * @param useIfExists   {@code true} to append {@code IF EXISTS} before table/view name, {@code false} otherwise.
     * @return New {@code DROP TABLE/VIEW} statement.
     */
    PgSQLDropTableOrViewStatement createDropTableOrViewStatement( TableNameDirect tableName, ObjectType theType,
                                                                  DropBehaviour dropBehaviour, Boolean useIfExists );
}
