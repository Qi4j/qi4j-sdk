package com.google.code.yanf4j.test.unittest.utils;

import java.io.IOException;
import java.nio.channels.Selector;
import com.google.code.yanf4j.util.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemUtilsUniTest {

  @Test
  public void testOpenSelector() throws IOException {
    Selector selector = SystemUtils.openSelector();
    assertNotNull(selector);
    assertTrue(selector.isOpen());
    if (SystemUtils.isLinuxPlatform()) {
      final String pollClassName = selector.provider().getClass().getCanonicalName();
      assertTrue(pollClassName.equals("sun.nio.ch.EPollSelectorProvider")
          || pollClassName.equals("sun.nio.ch.PollSelectorProvider"));
    }
    Selector selector2 = SystemUtils.openSelector();;
    assertNotSame(selector, selector2);
    selector.close();
    selector2.close();
  }


}
