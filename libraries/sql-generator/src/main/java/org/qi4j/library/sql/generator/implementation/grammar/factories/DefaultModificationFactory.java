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

import org.qi4j.library.sql.generator.grammar.builders.modification.ColumnSourceByValuesBuilder;
import org.qi4j.library.sql.generator.grammar.builders.modification.DeleteBySearchBuilder;
import org.qi4j.library.sql.generator.grammar.builders.modification.InsertStatementBuilder;
import org.qi4j.library.sql.generator.grammar.builders.modification.UpdateBySearchBuilder;
import org.qi4j.library.sql.generator.grammar.common.ColumnNameList;
import org.qi4j.library.sql.generator.grammar.common.TableNameDirect;
import org.qi4j.library.sql.generator.grammar.common.ValueExpression;
import org.qi4j.library.sql.generator.grammar.modification.ColumnSourceByQuery;
import org.qi4j.library.sql.generator.grammar.modification.SetClause;
import org.qi4j.library.sql.generator.grammar.modification.TargetTable;
import org.qi4j.library.sql.generator.grammar.modification.UpdateSource;
import org.qi4j.library.sql.generator.grammar.modification.UpdateSourceByExpression;
import org.qi4j.library.sql.generator.grammar.query.QueryExpression;
import org.qi4j.library.sql.generator.implementation.grammar.builders.modification.ColumnSourceByValuesBuilderImpl;
import org.qi4j.library.sql.generator.implementation.grammar.builders.modification.DeleteBySearchBuilderImpl;
import org.qi4j.library.sql.generator.implementation.grammar.builders.modification.InsertStatementBuilderImpl;
import org.qi4j.library.sql.generator.implementation.grammar.builders.modification.UpdateBySearchBuilderImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.ColumnSourceByQueryImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.SetClauseImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.TargetTableImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.UpdateSourceByExpressionImpl;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.qi4j.library.sql.generator.implementation.grammar.modification.ColumnSourceByQueryImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.SetClauseImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.TargetTableImpl;
import org.qi4j.library.sql.generator.implementation.grammar.modification.UpdateSourceByExpressionImpl;
import org.qi4j.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.qi4j.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultModificationFactory extends AbstractModificationFactory
{

    public DefaultModificationFactory(SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public ColumnSourceByValuesBuilder columnSourceByValues()
    {
        return new ColumnSourceByValuesBuilderImpl( this.getProcessor() );
    }

    public ColumnSourceByQuery columnSourceByQuery( ColumnNameList columnNames, QueryExpression query )
    {
        return new ColumnSourceByQueryImpl( this.getProcessor(), columnNames, query );
    }

    public DeleteBySearchBuilder deleteBySearch()
    {
        return new DeleteBySearchBuilderImpl( this.getProcessor(), this.getVendor().getBooleanFactory()
                                                                       .booleanBuilder() );
    }

    public InsertStatementBuilder insert()
    {
        return new InsertStatementBuilderImpl( this.getProcessor() );
    }

    public UpdateBySearchBuilder updateBySearch()
    {
        return new UpdateBySearchBuilderImpl( this.getProcessor(), this.getVendor().getBooleanFactory()
                                                                       .booleanBuilder() );
    }

    public TargetTable createTargetTable( TableNameDirect tableName, Boolean isOnly )
    {
        return new TargetTableImpl( this.getProcessor(), isOnly, tableName );
    }

    public UpdateSourceByExpression updateSourceByExp( ValueExpression expression )
    {
        return new UpdateSourceByExpressionImpl( this.getProcessor(), expression );
    }

    public SetClause setClause( String updateTarget, UpdateSource updateSource )
    {
        return new SetClauseImpl( this.getProcessor(), updateTarget, updateSource );
    }
}
