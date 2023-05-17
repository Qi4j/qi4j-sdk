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
package org.qi4j.library.sql.generator.implementation.grammar.common;

import java.util.Objects;
import org.qi4j.library.sql.generator.grammar.common.TableNameDirect;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableNameDirectImpl extends TableNameImpl<TableNameDirect>
    implements TableNameDirect
{
    private final String _tableName;

    public TableNameDirectImpl(SQLProcessorAggregator processor, String schemaName, String tableName )
    {
        this( processor, TableNameDirect.class, schemaName, tableName );
    }

    protected TableNameDirectImpl( SQLProcessorAggregator processor, Class<? extends TableNameDirect> implClass,
                                   String schemaName, String tableName )
    {
        super( processor, implClass, schemaName );
        Objects.requireNonNull( tableName, "table name" );

        this._tableName = tableName;
    }

    public String getTableName()
    {
        return this._tableName;
    }

    @Override
    protected boolean doesEqual( TableNameDirect another )
    {
        return super.doesEqual( another ) && this._tableName.equals( another.getTableName() );
    }
}
