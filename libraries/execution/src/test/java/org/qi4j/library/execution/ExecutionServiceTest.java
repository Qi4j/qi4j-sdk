package org.qi4j.library.execution;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.execution.assembly.ExecutionServiceAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExecutionServiceTest extends AbstractQi4jTest
{

    private CopyOnWriteArraySet<Thread> threads = new CopyOnWriteArraySet<>();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new ExecutionServiceAssembler()
            .withMaxThreads( 3 )
            .assemble( module );
    }

    @Test
    void givenMaxThreeThreadsWhenSubmittingManyTasksExpectToOnlySeeThreeThreads()
        throws InterruptedException
    {
        ExecutorService underTest = serviceFinder.findService( ExecutorService.class ).get();
        Runnable r = () -> {
            threads.add( Thread.currentThread() );
        };
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        Thread.sleep( 10 );
        assertThat( threads.size(), equalTo( 3 ) );
        underTest.shutdownNow();
    }
}
