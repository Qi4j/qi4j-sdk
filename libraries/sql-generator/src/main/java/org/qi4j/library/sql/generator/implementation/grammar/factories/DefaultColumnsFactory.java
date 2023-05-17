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
package org.qi4j.library.sql.generator.implementation.grammar.factories;

import java.util.Collection;
import org.qi4j.library.sql.generator.grammar.builders.query.ColumnsBuilder;
import org.qi4j.library.sql.generator.grammar.common.ColumnNameList;
import org.qi4j.library.sql.generator.grammar.common.SetQuantifier;
import org.qi4j.library.sql.generator.grammar.common.ValueExpression;
import org.qi4j.library.sql.generator.grammar.query.ColumnReferenceByExpression;
import org.qi4j.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.qi4j.library.sql.generator.implementation.grammar.builders.query.ColumnsBuilderImpl;
import org.qi4j.library.sql.generator.implementation.grammar.common.ColumnNameListImpl;
import org.qi4j.library.sql.generator.implementation.grammar.query.ColumnReferenceByExpressionImpl;
import org.qi4j.library.sql.generator.implementation.grammar.query.ColumnReferenceByNameImpl;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultColumnsFactory extends AbstractColumnsFactory
{

    public DefaultColumnsFactory(SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public ColumnsBuilder columnsBuilder( SetQuantifier setQuantifier )
    {
        return new ColumnsBuilderImpl( this.getProcessor(), setQuantifier );
    }

    public ColumnReferenceByName colName( String tableName, String colName )
    {
        return new ColumnReferenceByNameImpl( this.getProcessor(), tableName, colName );
    }

    public ColumnReferenceByExpression colExp( ValueExpression expression )
    {
        return new ColumnReferenceByExpressionImpl( this.getProcessor(), expression );
    }

    public ColumnNameList colNames( String... names )
    {
        return new ColumnNameListImpl( this.getProcessor(), names );
    }

    public ColumnNameList colNames( Collection<String> names )
    {
        return new ColumnNameListImpl( this.getProcessor(), names );
    }
}
