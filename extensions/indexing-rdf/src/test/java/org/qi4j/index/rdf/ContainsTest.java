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
package org.qi4j.index.rdf;

import java.io.File;
import java.util.Set;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.TemporaryFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith( TemporaryFolder.class )
public class ContainsTest extends AbstractQi4jTest
{
    private TemporaryFolder tmpDir;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
        ModuleAssembly prefModule = module.layer().module( "PrefModule" );
        prefModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        prefModule.forMixin( NativeConfiguration.class ).declareDefaults()
            .dataDirectory().set( new File( tmpDir.getRoot(), "rdf-data" ).getAbsolutePath() );
        new EntityTestAssembler().assemble( prefModule );

        module.entities( ContainsAllTest.ExampleEntity.class );
        module.values( ContainsAllTest.ExampleValue.class, ContainsAllTest.ExampleValue2.class );

        EntityTestAssembler testAss = new EntityTestAssembler();
        testAss.assemble( module );

        RdfNativeSesameStoreAssembler rdfAssembler = new RdfNativeSesameStoreAssembler();
        rdfAssembler.assemble( module );
    }

    @Test
    public void simpleContainsSuccessTest()
        throws Exception
    {
        ContainsAllTest.ExampleEntity result = this.performContainsStringTest(
            ContainsAllTest.setOf( ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3 ),
            ContainsAllTest.TEST_STRING_3
        );

        assertThat( "The entity must have been found", result != null, is( true ) );
    }

    @Test
    public void simpleContainsSuccessFailTest()
        throws Exception
    {
        ContainsAllTest.ExampleEntity result = this.performContainsStringTest(
            ContainsAllTest.setOf( ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3 ),
            ContainsAllTest.TEST_STRING_4
        );

        assertThat( "The entity must not have been found", result == null, is( true ) );
    }

    @Test
    public void simpleContainsNullTest()
        throws Exception
    {
        assertThrows( NullPointerException.class, () ->
            this.performContainsStringTest( ContainsAllTest.setOf( ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3 ), null )
        );
    }

    @Test
    public void simpleContainsStringValueSuccessTest()
        throws Exception
    {
        ContainsAllTest.ExampleEntity result = this.performContainsStringValueTest(
            ContainsAllTest.setOf( ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3 ),
            ContainsAllTest.TEST_STRING_3
        );

        assertThat( "The entity must have been found", result != null, is( true ) );
    }

    @Test
    public void simpleContainsStringValueFailTest()
        throws Exception
    {
        ContainsAllTest.ExampleEntity result = this.performContainsStringTest(
            ContainsAllTest.setOf( ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3 ),
            ContainsAllTest.TEST_STRING_4
        );

        assertThat( "The entity must not have been found", result == null, is( true ) );
    }

    private ContainsAllTest.ExampleEntity findEntity(String string )
    {
        QueryBuilder<ContainsAllTest.ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ContainsAllTest.ExampleEntity.class );

        builder = builder.where(
            QueryExpressions.contains( QueryExpressions.templateFor( ContainsAllTest.ExampleEntity.class ).strings(), string ) );
        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder ).find();
    }

    private ContainsAllTest.ExampleEntity findEntityBasedOnValueString(String valueString )
    {
        ValueBuilder<ContainsAllTest.ExampleValue2> vBuilder = this.valueBuilderFactory.newValueBuilder( ContainsAllTest.ExampleValue2.class );
        vBuilder.prototype().stringProperty().set( valueString );

        ValueBuilder<ContainsAllTest.ExampleValue> vBuilder2 = this.valueBuilderFactory.newValueBuilder( ContainsAllTest.ExampleValue.class );
        vBuilder2.prototype().valueProperty().set( vBuilder.newInstance() );

        return this.createComplexQuery( vBuilder2.newInstance() ).find();
    }

    private Query<ContainsAllTest.ExampleEntity> createComplexQuery(ContainsAllTest.ExampleValue value )
    {
        QueryBuilder<ContainsAllTest.ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ContainsAllTest.ExampleEntity.class );
        builder = builder.where(
            QueryExpressions.contains( QueryExpressions.templateFor( ContainsAllTest.ExampleEntity.class ).complexValue(), value ) );

        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder );
    }

    private ContainsAllTest.ExampleEntity performContainsStringTest(Set<String> entityStrings, String queryableString )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        ContainsAllTest.createEntityWithStrings( creatingUOW, this.valueBuilderFactory,
                                                 entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            return this.findEntity( queryableString );
        }
        finally
        {
            queryingUOW.discard();
        }
    }

    private ContainsAllTest.ExampleEntity performContainsStringValueTest(Set<String> entityStrings, String queryableString )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        ContainsAllTest.createEntityWithComplexValues( creatingUOW, this.valueBuilderFactory,
                                                       entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            return this.findEntityBasedOnValueString( queryableString );
        }
        finally
        {
            queryingUOW.discard();
        }
    }
}
