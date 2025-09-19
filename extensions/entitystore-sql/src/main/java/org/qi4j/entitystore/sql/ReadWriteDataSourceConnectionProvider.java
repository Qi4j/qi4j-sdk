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

import org.jooq.exception.DataAccessException;
import org.jooq.impl.DataSourceConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ReadWriteDataSourceConnectionProvider extends DataSourceConnectionProvider
{
    private final DataSource dataSource;

    public ReadWriteDataSourceConnectionProvider(DataSource dataSource)
    {
        super(dataSource);
        this.dataSource = dataSource;
    }

    // We need to set the connection to read-only, otherwise we get a
    // "Cannot change transaction read-only flag in the middle of a transaction" exception.
    @Override
    public Connection acquire()
    {
        Connection acquire = super.acquire();
        try
        {
            acquire.setReadOnly(false);
        }
        catch(SQLException e)
        {
            throw new DataAccessException("Error getting connection from data source " + dataSource, e);
        }
        return acquire;
    }
}
