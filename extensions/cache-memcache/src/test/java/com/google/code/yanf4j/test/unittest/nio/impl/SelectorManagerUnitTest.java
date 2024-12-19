package com.google.code.yanf4j.test.unittest.nio.impl;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import com.google.code.yanf4j.config.Configuration;
import com.google.code.yanf4j.core.EventType;
import com.google.code.yanf4j.nio.NioSession;
import com.google.code.yanf4j.nio.TCPController;
import com.google.code.yanf4j.nio.impl.Reactor;
import com.google.code.yanf4j.nio.impl.SelectorManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-24 03:09:42
 */

public class SelectorManagerUnitTest {
  private SelectorManager selectorManager;
  int selectorPoolSize = 3;

  @BeforeEach
  public void setUp() throws Exception {
    Configuration configuration = new Configuration();
    TCPController controller = new TCPController(configuration);
    this.selectorManager = new SelectorManager(this.selectorPoolSize, controller, configuration);
    this.selectorManager.start();
    controller.setSessionTimeout(1000);
    controller.getConfiguration().setSessionIdleTimeout(1000);
  }

  @Test
  public void testNextReactor() {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      Reactor reactor = this.selectorManager.nextReactor();
      Assertions.assertNotNull(reactor);
      Assertions.assertTrue(reactor.getReactorIndex() > 0);
    }
    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  public void testRegisterOpenChannel() throws Exception {
    MockSelectableChannel channel = new MockSelectableChannel();
    channel.selectionKey = new MockSelectionKey();
    Reactor reactor = this.selectorManager.registerChannel(channel, 1, "hello");
    Thread.sleep(Reactor.DEFAULT_WAIT * 3);
    Assertions.assertSame(reactor.getSelector(), channel.selector);
    Assertions.assertSame(reactor.getSelector(), channel.selectionKey.selector);
    Assertions.assertEquals(1, channel.ops);
    Assertions.assertEquals("hello", channel.attch);
  }

  @Test
  public void testRegisterCloseChannel() throws Exception {
    MockSelectableChannel channel = new MockSelectableChannel();
    channel.close();
    this.selectorManager.registerChannel(channel, 1, "hello");
    Thread.sleep(Reactor.DEFAULT_WAIT * 3);
    Assertions.assertNull(channel.selector);
    Assertions.assertEquals(0, channel.ops);
    Assertions.assertNull(channel.attch);
  }

  @Test
  public void testRegisterOpenSession() throws Exception {
    IMocksControl control = EasyMock.createControl();
    NioSession session = control.createMock(NioSession.class);
    EasyMock.makeThreadSafe(session, true);
    // next reactor index=2
    Reactor nextReactor = this.selectorManager.getReactorByIndex(2);
    session.onEvent(EventType.ENABLE_READ, nextReactor.getSelector());
    EasyMock.expectLastCall();
    EasyMock.expect(session.isClosed()).andReturn(false).times(2);
    EasyMock.expect(session.getAttribute(SelectorManager.REACTOR_ATTRIBUTE)).andReturn(null);
    EasyMock.expect(session.setAttributeIfAbsent(SelectorManager.REACTOR_ATTRIBUTE, nextReactor))
        .andReturn(null);

    control.replay();
    this.selectorManager.registerSession(session, EventType.ENABLE_READ);
    Thread.sleep(Reactor.DEFAULT_WAIT * 3);
    control.verify();
  }

  @Test
  public void testRegisterCloseSession() throws Exception {
    IMocksControl control = EasyMock.createControl();
    NioSession session = control.createMock(NioSession.class);
    EasyMock.expect(session.isClosed()).andReturn(true);
    control.replay();
    this.selectorManager.registerSession(session, EventType.ENABLE_READ);
    Thread.sleep(Reactor.DEFAULT_WAIT * 3);
    control.verify();
  }

  @AfterEach
  public void tearDown() throws Exception {
    if (this.selectorManager != null) {
      this.selectorManager.getController().stop();
      this.selectorManager.stop();
    }
  }

}
