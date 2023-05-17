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
package org.qi4j.library.shiro;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.library.shiro.assembly.PasswordDomainAssembler;
import org.qi4j.library.shiro.assembly.PermissionsDomainAssembler;
import org.qi4j.library.shiro.assembly.StandaloneShiroAssembler;
import org.qi4j.library.shiro.domain.passwords.PasswordSecurable;
import org.qi4j.library.shiro.domain.permissions.Role;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.qi4j.library.shiro.domain.permissions.RoleFactory;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class PermissionsDomainTest
    extends AbstractQi4jTest
{

    // START SNIPPET: domain
    public interface User
            extends PasswordSecurable, RoleAssignee
    {
    }

    // END SNIPPET: domain
    @Mixins( UserFactoryMixin.class )
    public interface UserFactory
    {

        User createNewUser( String username, String password );

    }

    public static class UserFactoryMixin
            implements UserFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private PasswordService passwordService;

        @Override
        public User createNewUser( String username, String password )
        {
            EntityBuilder<User> userBuilder = uowf.currentUnitOfWork().newEntityBuilder( User.class );
            User user = userBuilder.instance();
            user.subjectIdentifier().set( username );
            user.password().set( passwordService.encryptPassword( password ) );
            return userBuilder.newInstance();
        }

    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {

        new EntityTestAssembler().assemble( module );
        new RdfMemoryStoreAssembler().assemble( module );
        ModuleAssembly configModule = module;
        // START SNIPPET: assembly
        new StandaloneShiroAssembler().
            withConfig( configModule, Visibility.layer ).
            assemble( module );
        new PasswordDomainAssembler().
            withConfig( configModule, Visibility.layer ).
            assemble( module );
        new PermissionsDomainAssembler().
            assemble( module );

        module.entities( User.class );
        module.services( UserFactory.class );

        // END SNIPPET: assembly
        configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:standalone-shiro.ini" );
    }

    private UserFactory userFactory;

    private RoleFactory roleFactory;

    @BeforeEach
    public void before_PermissionsDomainTest()
    {
        userFactory = serviceFinder.findService( UserFactory.class ).get();
        roleFactory = serviceFinder.findService( RoleFactory.class ).get();
    }

    @Test
    public void test()
            throws UnitOfWorkCompletionException
    {
        // START SNIPPET: usage
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        User user = userFactory.createNewUser( "foo", "bar" );
        Role role = roleFactory.create( "role-one", "permission-one", "permission-two" );
        role.assignTo( user );

        uow.complete();

        // END SNIPPET: usage
        // START SNIPPET: usage
        uow = unitOfWorkFactory.newUnitOfWork();

        Subject currentUser = SecurityUtils.getSubject();
        currentUser.login( new UsernamePasswordToken( "foo", "bar" ) );

        if ( !currentUser.hasRole( "role-one" ) ) {
            fail( "User 'foo' must have 'role-one' role." );
        }

        if ( !currentUser.isPermitted( "permission-one" ) ) {
            fail( "User 'foo' must have 'permission-one' permission." );
        }

        // END SNIPPET: usage
        assertThat( currentUser.hasRole( "role-one" ), is( true ) );
        assertThat( currentUser.hasRole( "role-two" ), is( false ) );

        assertThat( currentUser.isPermitted( "permission-one" ), is( true ) );
        assertThat( currentUser.isPermitted( "permission-two" ), is( true ) );
        assertThat( currentUser.isPermitted( "permission-three" ), is( false ) );

        // START SNIPPET: usage
        uow.discard();
        // END SNIPPET: usage
    }

}
