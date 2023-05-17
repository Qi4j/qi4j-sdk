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

package org.qi4j.test.indexing.layered;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.test.model.Male;
import org.qi4j.test.model.Person;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class Suite1Case1
    implements TestCase
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private QueryBuilderFactory qbf;

    private QueryBuilder<Male> builder;

    private Query<Male> query;

    @Override
    public void given()
        throws Exception
    {
        QueryBuilder<Male> qb = qbf.newQueryBuilder( Male.class );
        Male prototype = QueryExpressions.templateFor( Male.class );
        builder = qb.where( QueryExpressions.eq(prototype.name(), "Joe Doe" ) );
    }

    @Override
    public void when()
        throws Exception
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        query = uow.newQuery( builder );
    }

    @Override
    public void expect()
        throws Exception
    {
        assertThat( query.count(), equalTo(1L) );
        Male male = query.find();
        assertThat( male.title().get(), equalTo( Person.Title.MR ));
        assertThat( male.name().get(), equalTo( "Joe Doe" ));
    }
}
