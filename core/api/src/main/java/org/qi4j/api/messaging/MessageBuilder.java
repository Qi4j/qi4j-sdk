package org.qi4j.api.messaging;

import java.util.function.Consumer;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

public class MessageBuilder
{
    @Structure
    private ValueBuilderFactory vbf;

    <T> T build(Class<T> type, Consumer<ValueBuilder<T>> builder)
    {
        ValueBuilder<T> vb = vbf.newValueBuilder(type);
        builder.accept(vb);
        return vb.newInstance();
    }
}
