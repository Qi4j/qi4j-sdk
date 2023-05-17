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
package org.qi4j.sample.forum.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.sample.forum.data.entity.Board;
import org.qi4j.sample.forum.data.entity.Forum;
import org.qi4j.sample.forum.data.entity.User;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;

/**
 * TODO
 */
public class ForumAdministration
    implements ResourceIndex<Query<Board>>
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    QueryBuilderFactory qbf;

    ForumAdmin forumAdmin = new ForumAdmin();
    Administrator administrator = new Administrator();

    public ForumAdministration bind( @Uses Forum forum, @Uses User user )
    {
        forumAdmin.bind( forum );
        administrator.bind( user );
        return this;
    }

    public Query<Board> index()
    {
        return forumAdmin.boards();
    }

    public Board createBoard( @Name( "name" ) String name )
    {
        return forumAdmin.createBoard( name );
    }

    protected class ForumAdmin
        extends Role<Forum>
    {

        public Query<Board> boards()
        {
            return qbf.newQueryBuilder( Board.class ).newQuery( self.boards() );
        }

        public Board createBoard( String name )
        {
            Board board = uowf.currentUnitOfWork().newEntity( Board.class );
            board.name().set( name );
            administrator.makeModerator( board );
            return board;
        }
    }

    protected class Administrator
        extends Role<User>
    {
        public void makeModerator( Board board )
        {
            board.moderators().add( self );
        }
    }
}
