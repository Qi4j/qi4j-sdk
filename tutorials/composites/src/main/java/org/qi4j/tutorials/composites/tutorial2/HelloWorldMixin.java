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
package org.qi4j.tutorials.composites.tutorial2;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * interface. The behaviour and state is mixed.
 */
public class HelloWorldMixin
    implements HelloWorld
{
    String phrase;
    String name;

    @Override
    public String say()
    {
        return getPhrase() + " " + getName();
    }

    @Override
    public void setPhrase( String phrase )
        throws IllegalArgumentException
    {
        if( phrase == null )
        {
            throw new IllegalArgumentException( "Phrase may not be null" );
        }

        this.phrase = phrase;
    }

    @Override
    public String getPhrase()
    {
        return phrase;
    }

    @Override
    public void setName( String name )
        throws IllegalArgumentException
    {
        if( name == null )
        {
            throw new IllegalArgumentException( "Name may not be null" );
        }

        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
// END SNIPPET: solution
