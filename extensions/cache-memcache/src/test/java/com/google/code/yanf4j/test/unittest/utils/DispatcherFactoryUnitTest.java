package com.google.code.yanf4j.test.unittest.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.code.yanf4j.core.Dispatcher;
import com.google.code.yanf4j.util.DispatcherFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DispatcherFactoryUnitTest {
  
  @Test
  public void testNewDispatcher() throws Exception {
    Dispatcher dispatcher =
        DispatcherFactory.newDispatcher(1, new ThreadPoolExecutor.AbortPolicy(), "test");
    Assertions.assertNotNull(dispatcher);
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicInteger count = new AtomicInteger();
    dispatcher.dispatch(new Runnable() {
      public void run() {
        count.incrementAndGet();
        latch.countDown();
      }
    });
    latch.await();
    Assertions.assertEquals(1, count.get());

    Assertions.assertNull(
        DispatcherFactory.newDispatcher(0, new ThreadPoolExecutor.AbortPolicy(), "test"));
    Assertions.assertNull(
        DispatcherFactory.newDispatcher(-1, new ThreadPoolExecutor.AbortPolicy(), "test"));
    dispatcher.stop();
    try {
      dispatcher.dispatch(new Runnable() {
        public void run() {
          Assertions.fail();
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
