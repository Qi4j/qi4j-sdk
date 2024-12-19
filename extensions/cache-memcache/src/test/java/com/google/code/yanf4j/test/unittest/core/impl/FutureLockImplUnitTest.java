package com.google.code.yanf4j.test.unittest.core.impl;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.code.yanf4j.core.impl.FutureLockImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-24 11:04:22
 */

public class FutureLockImplUnitTest {

  private static final class NotifyFutureRunner implements Runnable {
    FutureLockImpl<Boolean> future;
    long sleepTime;
    Throwable throwable;

    public NotifyFutureRunner(FutureLockImpl<Boolean> future, long sleepTime, Throwable throwable) {
      super();
      this.future = future;
      this.sleepTime = sleepTime;
      this.throwable = throwable;
    }

    public void run() {
      try {
        Thread.sleep(this.sleepTime);
        if (this.throwable != null) {
          this.future.failure(this.throwable);
        } else {
          this.future.setResult(true);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  public void testGet() throws Exception {
    FutureLockImpl<Boolean> future = new FutureLockImpl<Boolean>();
    new Thread(new NotifyFutureRunner(future, 2000, null)).start();
    boolean result = future.get();
    Assertions.assertTrue(result);
    Assertions.assertTrue(future.isDone());
    Assertions.assertFalse(future.isCancelled());
  }

  @Test
  public void testGetImmediately() throws Exception {
    FutureLockImpl<Boolean> future = new FutureLockImpl<Boolean>();
    future.setResult(true);
    boolean result = future.get();
    Assertions.assertTrue(result);
    Assertions.assertTrue(future.isDone());
    Assertions.assertFalse(future.isCancelled());
  }

  @Test
  public void testGetException() throws Exception {
    FutureLockImpl<Boolean> future = new FutureLockImpl<Boolean>();
    new Thread(new NotifyFutureRunner(future, 2000, new IOException("hello"))).start();
    try {
      future.get();
      Assertions.fail();
    } catch (ExecutionException e) {
      Assertions.assertEquals("hello", e.getCause().getMessage());

    }
    Assertions.assertTrue(future.isDone());
    Assertions.assertFalse(future.isCancelled());

  }

  @Test
  public void testCancel() throws Exception {
    final FutureLockImpl<Boolean> future = new FutureLockImpl<Boolean>();
    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(3000);
          future.cancel(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
    try {
      future.get();
      Assertions.fail();
    } catch (CancellationException e) {
      Assertions.assertTrue(true);

    }
    Assertions.assertTrue(future.isDone());
    Assertions.assertTrue(future.isCancelled());
  }

  @Test
  public void testGetTimeout() throws Exception {
    FutureLockImpl<Boolean> future = new FutureLockImpl<Boolean>();
    try {
      future.get(1000, TimeUnit.MILLISECONDS);
      Assertions.fail();
    } catch (TimeoutException e) {
      Assertions.assertTrue(true);
    }
  }
}
