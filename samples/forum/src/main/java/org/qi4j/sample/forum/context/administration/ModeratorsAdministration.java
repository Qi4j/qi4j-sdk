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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.sample.forum.data.Moderators;
import org.qi4j.sample.forum.data.entity.User;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;

/**
 * TODO
 */
public class ModeratorsAdministration
    implements ResourceIndex<Query<User>>
{
    @Structure
    Module module;

    ModeratorsAdmin moderatorsAdmin = new ModeratorsAdmin();

    public ModeratorsAdministration bind( @Uses Moderators moderators )
    {
        moderatorsAdmin.bind( moderators );
        return this;
    }

    public Query<User> index()
    {
        return moderatorsAdmin.moderators();
    }

    public void addModerator( User user )
    {
        moderatorsAdmin.addModerator( user );
    }

    public void removeModerator( User user )
    {
        moderatorsAdmin.removeModerator( user );
    }

    protected class ModeratorsAdmin
        extends Role<Moderators>
    {
        @Structure
        Module module;

        public Query<User> moderators()
        {
            return module.newQueryBuilder( User.class ).newQuery( self.moderators() );
        }

        public void addModerator( User user )
        {
            self().moderators().add( user );
        }

        public void removeModerator( User user )
        {
            self().moderators().remove( user );
        }
    }
}
