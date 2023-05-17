/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.sql.generator;

import org.qi4j.library.sql.generator.grammar.common.SQLStatement;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.qi4j.library.sql.generator.vendor.SQLVendor;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 */
public abstract class AbstractSQLSyntaxTest
{

    private SQLVendor _vendor;

    protected void logStatement( String statementType, SQLVendor vendor, SQLStatement statement )
    {
        String stringStmt = vendor.toString( statement );
        LoggerFactory.getLogger( this.getClass().getName() ).info( statementType + ":" + "\n" + stringStmt + "\n" );

        assertThat(
            "Strings must be same from both SQLVendor.toString(...) and statement.toString() methods.",
            statement.toString(),
            equalTo( stringStmt ) );
    }

    @BeforeEach
    public final void setUp()
        throws Exception
    {
        this._vendor = this.loadVendor();
    }

    @AfterEach
    public final void tearDown()
    {
        this._vendor = null;
    }

    protected final SQLVendor getVendor()
    {
        return this._vendor;
    }

    protected abstract SQLVendor loadVendor()
        throws Exception;
}
