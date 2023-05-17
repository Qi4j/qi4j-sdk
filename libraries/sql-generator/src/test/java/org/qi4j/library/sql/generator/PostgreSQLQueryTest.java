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

import org.qi4j.library.sql.generator.vendor.PostgreSQLVendor;
import org.qi4j.library.sql.generator.vendor.SQLVendorProvider;
import org.junit.jupiter.api.Test;
import org.qi4j.library.sql.generator.vendor.PostgreSQLVendor;
import org.qi4j.library.sql.generator.vendor.SQLVendorProvider;

public class PostgreSQLQueryTest extends AbstractQueryTest
{

    @Override
    protected PostgreSQLVendor loadVendor()
        throws Exception
    {
        return SQLVendorProvider.createVendor( PostgreSQLVendor.class );
    }

    @Test
    public void pgQuery5()
        throws Exception
    {
        ( (PostgreSQLVendor) this.getVendor() ).setLegacyOffsetAndLimit( true );
        super.query5();
    }

    @Test
    public void pgQuery6()
        throws Exception
    {
        ( (PostgreSQLVendor) this.getVendor() ).setLegacyOffsetAndLimit( true );
        super.query6();
    }

    @Test
    public void pgQuery7()
        throws Exception
    {
        ( (PostgreSQLVendor) this.getVendor() ).setLegacyOffsetAndLimit( true );
        super.query7();
    }
}
