package org.qi4j.library.execution;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.execution.assembly.ScheduledExecutionServiceAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ScheduledExecutionServiceTest extends AbstractQi4jTest
{

    private volatile AtomicInteger executed = new AtomicInteger( 0 );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new ScheduledExecutionServiceAssembler()
            .assemble( module );
    }

    @Test
    void givenScheduleOfTenMillisWhenSubmittingTwoTasksFor105MillisExpect20Invocations()
        throws InterruptedException
    {
        ScheduledExecutorService underTest = serviceFinder.findService( ScheduledExecutorService.class ).get();
        Runnable r = () -> {
            executed.incrementAndGet();
        };
        underTest.scheduleAtFixedRate( r, 10, 10, TimeUnit.MILLISECONDS );
        underTest.scheduleAtFixedRate( r, 10, 10, TimeUnit.MILLISECONDS );
        Thread.sleep( 105 );
        assertThat( executed.intValue(), equalTo( 20 ) );
        underTest.shutdownNow();
    }

}
