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

import org.qi4j.library.sql.generator.grammar.common.TableName;
import org.qi4j.library.sql.generator.implementation.TypeableImpl;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 */
public class TableNameImpl<TableNameType extends TableName> extends SQLSyntaxElementBase<TableName, TableNameType>
    implements TableName
{
    private final String _schemaName;

    protected TableNameImpl(SQLProcessorAggregator processor, Class<? extends TableNameType> implClass,
                            String schemaName )
    {
        super( processor, implClass );

        this._schemaName = schemaName;
    }

    public String getSchemaName()
    {
        return this._schemaName;
    }

    @Override
    protected boolean doesEqual( TableNameType another )
    {
        return TypeableImpl.bothNullOrEquals( this._schemaName, another.getSchemaName() );
    }
}
