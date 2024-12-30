package org.qi4j.messaging.pulsar;

import org.junit.jupiter.api.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

public class PulsarMessagingTest extends AbstractQi4jTest
{

    @Test
    public void sendSimpleType()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            ValueBuilder<SimpleType> builder = valueBuilderFactory.newValueBuilder( SimpleType.class );
            SimpleType prototype = builder.prototype();
            prototype.topic().set(  );
            prototype.topic().set(  );
            SimpleType msg1 = builder.newInstance();
            uow.createMessage( SimpleType.class, builder -> {

                }
            );
            uow.send( "test-alias", msg1 );
        }
    }

    @Override
    public void assemble( ModuleAssembly module ) throws Exception
    {
        module.defaultServices();
        module.values( SimpleType.class );
    }
}
