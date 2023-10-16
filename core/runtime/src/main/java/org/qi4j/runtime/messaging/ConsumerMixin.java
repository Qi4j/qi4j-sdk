package org.qi4j.runtime.messaging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.spi.messaging.ConsumerFactory;

public class ConsumerMixin
    implements InvocationHandler, ServiceActivation
{
    @Service
    ConsumerFactory spi;

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable
    {
        return null;
    }

    @Override
    public void activateService() throws Exception
    {

    }

    @Override
    public void passivateService() throws Exception
    {

    }
}
