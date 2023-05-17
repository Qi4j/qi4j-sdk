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

package org.qi4j.runtime.unitofwork;

import java.time.Instant;
import java.util.Stack;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.time.SystemTime;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.module.ModuleSpi;

public class UnitOfWorkFactoryMixin
    implements UnitOfWorkFactory
{
    @Structure
    private TransientBuilderFactory tbf;

    @Structure
    private ModuleSpi module;

    // Implementation of UnitOfWorkFactory
    @Override
    public UnitOfWork newUnitOfWork()
    {
        return newUnitOfWork( Usecase.DEFAULT );
    }

    @Override
    public UnitOfWork newUnitOfWork(Instant currentTime )
    {
        return newUnitOfWork( Usecase.DEFAULT, currentTime );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase )
    {
        return newUnitOfWork( usecase == null ? Usecase.DEFAULT : usecase, SystemTime.now() );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase, Instant currentTime )
    {
        UnitOfWorkInstance unitOfWorkInstance = new UnitOfWorkInstance( module, usecase, currentTime, metricsProvider() );
        return tbf.newTransient( UnitOfWork.class, unitOfWorkInstance );
    }

    private MetricsProvider metricsProvider()
    {
        return module.metricsProvider();
    }

    @Override
    public boolean isUnitOfWorkActive()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        return !stack.isEmpty();
    }

    @Override
    public UnitOfWork currentUnitOfWork()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        if( stack.size() == 0 )
        {
            throw new IllegalStateException( "No current UnitOfWork active" );
        }
        return tbf.newTransient( UnitOfWork.class, stack.peek() );
    }

    @Override
    public UnitOfWork getUnitOfWork( EntityComposite entity )
    {
        EntityInstance instance = (EntityInstance) CompositeInstance.compositeInstanceOf( entity );
        return instance.unitOfWork();
    }
}
