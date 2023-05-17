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
package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.Composite;

/**
 * This should be used when doing native queries, such as SQL, SPARQL or similar. EntityFinders can choose
 * what type of query languages they can understand by checking the language property of a QuerySpecification
 */
public class QuerySpecification
    implements Predicate<Composite>
{
    public static boolean isQueryLanguage( String language, Predicate<Composite> specification )
    {
        if( !( specification instanceof QuerySpecification ) )
        {
            return false;
        }

        return ( (QuerySpecification) specification ).language().equals( language );
    }

    private String language;
    private String query;

    public QuerySpecification( String language, String query )
    {
        this.language = language;
        this.query = query;
    }

    public String language()
    {
        return language;
    }

    public String query()
    {
        return query;
    }

    @Override
    public boolean test( Composite item )
    {
        return false;
    }

    @Override
    public String toString()
    {
        return language + ":" + query;
    }
}
