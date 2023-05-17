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
package org.qi4j.sample.forum.data.entity;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.sample.forum.data.Administrators;

import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
@Mixins( Forums.Mixin.class )
public interface Forums
    extends Administrators, EntityComposite
{
    Identity FORUMS_ID = StringIdentity.identityOf( "forums" );

    Query<Forum> forums();

    abstract class Mixin
        implements Forums
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        QueryBuilderFactory qbf;

        public Query<Forum> forums()
        {
            return uowf.currentUnitOfWork()
                .newQuery( qbf.newQueryBuilder( Forum.class ) )
                .orderBy( templateFor( Forum.class ).name() );
        }
    }
}
