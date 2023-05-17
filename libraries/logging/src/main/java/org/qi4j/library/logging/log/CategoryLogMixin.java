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

package org.qi4j.library.logging.log;

public class CategoryLogMixin
    implements CategoryLog
{
    @Override
    public void info( String category, String message )
    {
    }

    @Override
    public void info( String category, String message, Object param1 )
    {
    }

    @Override
    public void info( String category, String message, Object param1, Object param2 )
    {
    }

    @Override
    public void info( String category, String message, Object... params )
    {
    }

    @Override
    public void warning( String category, String message )
    {
    }

    @Override
    public void warning( String category, String message, Object param1 )
    {
    }

    @Override
    public void warning( String category, String message, Object param1, Object param2 )
    {
    }

    @Override
    public void warning( String category, String message, Object... params )
    {
    }

    @Override
    public void error( String category, String message )
    {
    }

    @Override
    public void error( String category, String message, Object param1 )
    {
    }

    @Override
    public void error( String category, String message, Object param1, Object param2 )
    {
    }

    @Override
    public void error( String category, String message, Object... params )
    {
    }
}
