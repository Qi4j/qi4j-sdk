package org.qi4j.messaging.pulsar;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleByteArrayReceiver
{
    private final CopyOnWriteArrayList<byte[]> received = new CopyOnWriteArrayList<>();

    void untypedMessage( byte[] data ) throws Exception
    {
        received.add( data );
    }

    public CopyOnWriteArrayList<byte[]> received()
    {
        return received;
    }
}
