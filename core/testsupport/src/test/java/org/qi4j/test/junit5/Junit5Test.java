package org.qi4j.test.junit5;

import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.test.Qi4jUnitExtension;
import org.qi4j.test.model.Cat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * This test is to help develop the JUnit 5 Extension.
 */
public class Junit5Test
{

    @RegisterExtension
    public Qi4jUnitExtension qi4j = Qi4jUnitExtension.forModule( module -> {
        module.values(Cat.class );
    } ).build();

    @Structure
    private ValueBuilderFactory vbf;

    @Test
    public void givenQi4jWhenInstantiatingCatExpectCatInstantiation()
    {
        ValueBuilder<Cat> builder = vbf.newValueBuilder( Cat.class );
        builder.prototype().identity().set( StringIdentity.identityOf( "123" ) );
        builder.prototype().name().set( "Kim" );
        Cat cat = builder.newInstance();

        assertThat( cat, notNullValue() );
        assertThat( cat.name().get(), equalTo("Kim") );
    }
}
