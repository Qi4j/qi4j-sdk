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
package org.qi4j.tutorials.composites.tutorial3;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * interface. The behaviour and state is mixed. Since parameters
 * are mandatory as default in Qi4j there's no need to do null checks.
 */
public class HelloWorldMixin
    implements HelloWorld
{
    String phrase;
    String name;

    @Override
    public String say()
    {
        return phrase + " " + name;
    }

    @Override
    public String getPhrase()
    {
        return phrase;
    }

    @Override
    public void setPhrase( String phrase )
    {
        this.phrase = phrase;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
    }
}
// END SNIPPET: solution
