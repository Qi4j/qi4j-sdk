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

package org.qi4j.test;

import java.util.ArrayList;

/**
 * JAVADOC
 */
public abstract class SomeMixin
    implements Some, Other, World
{

    protected SomeMixin()
    {
    }

    protected SomeMixin( String foo )
    {

    }

    public String some()
        throws Exception2, Exception1
    {
        multiEx( "foo" );
        unwrapResult();
        bar( 1.0, true, 1.0F, 'x', 0, 0L, (short) 0, (byte) 3, 4.0, new Object[ 0 ], new int[ 0 ] );
        generic( new ArrayList<>() );

        return "Hello " + other() + foo( "Test", 0 );
    }

    public String testConcern()
    {
        return someMethod( "Hello", 0, 0 );
    }

    public String someMethod( String foo, double x, int y )
    {
        return foo;
    }
}
