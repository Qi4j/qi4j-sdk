package com.google.code.yanf4j.test.unittest.core.impl;

import com.google.code.yanf4j.core.impl.PoolDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author boyan
 * @since 1.0, 2009-12-24 ����11:34:43
 */

public class PoolDispatcherUnitTest
{
    PoolDispatcher dispatcher;

    @BeforeEach
    public void setUp()
    {
        this.dispatcher =
            new PoolDispatcher(10, 60, TimeUnit.SECONDS, new ThreadPoolExecutor.AbortPolicy(), "test");
    }

    @AfterEach
    public void tearDown()
    {
        this.dispatcher.stop();
    }

    private static final class TestRunner implements Runnable
    {
        boolean ran;

        public void run()
        {
            this.ran = true;

        }
    }

    @Test
    public void testDispatch()
        throws Exception
    {
        TestRunner runner = new TestRunner();
        this.dispatcher.dispatch(runner);
        Thread.sleep(1000);
        Assertions.assertTrue(runner.ran);

    }

    @Test
    public void testDispatchNull()
        throws Exception
    {
        Assertions.assertThrows(NullPointerException.class, () ->
            this.dispatcher.dispatch(null)
        );
    }

    @Test
    public void testDispatcherStop()
        throws Exception
    {
        this.dispatcher.stop();
        TestRunner runner = new TestRunner();
        this.dispatcher.dispatch(runner);
        Thread.sleep(1000);
        Assertions.assertFalse(runner.ran);
    }

    @Test
    public void testDispatchReject()
        throws Exception
    {
        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            this.dispatcher = new PoolDispatcher(1, 1, 1, 60, TimeUnit.SECONDS,
                new ThreadPoolExecutor.AbortPolicy(), "test");
            this.dispatcher.dispatch(new Runnable()
            {
                public void run()
                {
                    while (!Thread.currentThread().isInterrupted())
                    {

                    }
                }
            });
            this.dispatcher.dispatch(new Runnable()
            {
                public void run()
                {
                    while (!Thread.currentThread().isInterrupted())
                    {

                    }
                }
            });

            // Should throw a RejectedExecutionException
            this.dispatcher.dispatch(new TestRunner());
        });
    }
}
