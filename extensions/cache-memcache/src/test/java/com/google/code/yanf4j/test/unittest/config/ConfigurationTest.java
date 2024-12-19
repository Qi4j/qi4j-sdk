package com.google.code.yanf4j.test.unittest.config;

import com.google.code.yanf4j.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationTest {
  Configuration configuration;

  @BeforeEach
  protected void setUp() throws Exception {
    this.configuration = new Configuration();
  }

  @Test
  public void testDefaultConfig() {
    assertTrue(this.configuration.isHandleReadWriteConcurrently());

    assertEquals(32 * 1024, this.configuration.getSessionReadBufferSize());
    assertEquals(0, this.configuration.getSoTimeout());

    assertEquals(1, this.configuration.getReadThreadCount());
    assertEquals(1000, this.configuration.getCheckSessionTimeoutInterval());
    assertEquals(5 * 60 * 1000, this.configuration.getStatisticsInterval());
    assertFalse(this.configuration.isStatisticsServer());
    assertEquals(5000L, this.configuration.getSessionIdleTimeout());

    this.configuration.setSessionReadBufferSize(8 * 1024);
    assertEquals(8 * 1024, this.configuration.getSessionReadBufferSize());
    try {
      this.configuration.setSessionReadBufferSize(0);
      fail();
    } catch (IllegalArgumentException e) {

    }
    this.configuration.setReadThreadCount(11);
    assertEquals(11, this.configuration.getReadThreadCount());
    try {
      this.configuration.setReadThreadCount(-10);
      fail();
    } catch (IllegalArgumentException e) {

    }

    this.configuration.setSoTimeout(1000);
    assertEquals(1000, this.configuration.getSoTimeout());
    this.configuration.setSoTimeout(0);
    assertEquals(0, this.configuration.getSoTimeout());
    try {
      this.configuration.setSoTimeout(-1000);
      fail();
    } catch (IllegalArgumentException e) {

    }

  }

}
