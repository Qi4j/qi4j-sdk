package org.qi4j.messaging.pulsar;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

public class SimpleSender
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    void sendByteArray( String topicAlias, SimpleType data)
    {
        uowf.currentUnitOfWork().send( "test-alias", data );
    }
}
