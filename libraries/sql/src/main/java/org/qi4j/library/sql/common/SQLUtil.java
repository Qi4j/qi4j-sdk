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
package org.qi4j.library.sql.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtil
{
    public static SQLException withAllSQLExceptions( SQLException sqlEx )
    {
        SQLException next = sqlEx.getNextException();
        while( next != null )
        {
            sqlEx.addSuppressed( next );
            next = next.getNextException();
        }
        return sqlEx;
    }

    public static void closeQuietly( ResultSet resultSet )
    {
        closeQuietly( resultSet, null );
    }

    public static void closeQuietly( ResultSet resultSet, Throwable originalException )
    {
        if( resultSet != null )
        {
            try
            {
                resultSet.close();
            }
            catch( SQLException ignored )
            {
                if( originalException != null )
                {
                    originalException.addSuppressed( ignored );
                }
            }
        }
    }

    public static void closeQuietly( Statement select )
    {
        closeQuietly( select, null );
    }

    public static void closeQuietly( Statement select, Throwable originalException )
    {
        if( select != null )
        {
            try
            {
                select.close();
            }
            catch( SQLException ignored )
            {
                if( originalException != null )
                {
                    originalException.addSuppressed( ignored );
                }
            }
        }
    }

    public static void closeQuietly( Connection connection )
    {
        closeQuietly( connection, null );
    }

    public static void closeQuietly( Connection connection, Throwable originalException )
    {
        if( connection != null )
        {
            try
            {
                connection.close();
            }
            catch( SQLException ignored )
            {
                if( originalException != null )
                {
                    originalException.addSuppressed( ignored );
                }
            }
        }
    }

    public static void rollbackQuietly( Connection connection )
    {
        rollbackQuietly( connection, null );
    }

    public static void rollbackQuietly( Connection connection, Throwable originalException )
    {
        if( connection != null )
        {
            try
            {
                connection.rollback();
            }
            catch( SQLException ignored )
            {
                if( originalException != null )
                {
                    originalException.addSuppressed( ignored );
                }
            }
        }
    }

    private SQLUtil()
    {
    }
}
