package com.google.code.yanf4j.test.unittest.core;

import java.util.HashMap;
import java.util.Map;
import com.google.code.yanf4j.core.SocketOption;
import com.google.code.yanf4j.core.impl.StandardSocketOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SocketOptionUnitTest {
  @Test
  public void testType() {
    Assertions.assertEquals(Integer.class, StandardSocketOption.SO_LINGER.type());
    Assertions.assertEquals(Boolean.class, StandardSocketOption.SO_KEEPALIVE.type());
    Assertions.assertEquals(Integer.class, StandardSocketOption.SO_RCVBUF.type());
    Assertions.assertEquals(Integer.class, StandardSocketOption.SO_SNDBUF.type());
    Assertions.assertEquals(Boolean.class, StandardSocketOption.SO_REUSEADDR.type());
    Assertions.assertEquals(Boolean.class, StandardSocketOption.TCP_NODELAY.type());
  }

  @Test
  public void testPutInMap() {
    Map<SocketOption, Object> map = new HashMap<SocketOption, Object>();
    map.put(StandardSocketOption.SO_KEEPALIVE, true);
    map.put(StandardSocketOption.SO_RCVBUF, 4096);
    map.put(StandardSocketOption.SO_SNDBUF, 4096);
    map.put(StandardSocketOption.TCP_NODELAY, false);

    Assertions.assertEquals(4096, map.get(StandardSocketOption.SO_RCVBUF));
    Assertions.assertEquals(4096, map.get(StandardSocketOption.SO_SNDBUF));
    Assertions.assertEquals(false, map.get(StandardSocketOption.TCP_NODELAY));
    Assertions.assertEquals(true, map.get(StandardSocketOption.SO_KEEPALIVE));
  }
}
