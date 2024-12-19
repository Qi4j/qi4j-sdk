package com.google.code.yanf4j.test.unittest.core.impl;

import com.google.code.yanf4j.buffer.IoBuffer;
import com.google.code.yanf4j.core.CodecFactory.Encoder;
import com.google.code.yanf4j.core.impl.TextLineCodecFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-24 10:33:59
 */

public class TextLineCodecFactoryUnitTest {
  TextLineCodecFactory textLineCodecFactory;

  @BeforeEach
  public void setUp() {
    this.textLineCodecFactory = new TextLineCodecFactory();
    TextLineCodecFactory.SPLIT.clear();
  }

  @Test
  public void testEncodeNormal() throws Exception {
    Encoder encoder = this.textLineCodecFactory.getEncoder();
    Assertions.assertNotNull(encoder);
    IoBuffer buffer = encoder.encode("hello", null);
    Assertions.assertNotNull(buffer);
    Assertions.assertTrue(buffer.hasRemaining());
    Assertions.assertArrayEquals("hello\r\n".getBytes("utf-8"), buffer.array());

  }

  @Test
  public void testEncodeEmpty() throws Exception {
    Encoder encoder = this.textLineCodecFactory.getEncoder();
    Assertions.assertNull(encoder.encode(null, null));
    Assertions.assertEquals(TextLineCodecFactory.SPLIT, encoder.encode("", null));
  }

  @Test
  public void decodeNormal() throws Exception {
    Encoder encoder = this.textLineCodecFactory.getEncoder();
    Assertions.assertNotNull(encoder);
    IoBuffer buffer = encoder.encode("hello", null);

    String str = (String) this.textLineCodecFactory.getDecoder().decode(buffer, null);
    Assertions.assertEquals("hello", str);
  }

  @Test
  public void decodeEmpty() throws Exception {
    Assertions.assertNull(this.textLineCodecFactory.getDecoder().decode(null, null));
    Assertions.assertEquals("",
        this.textLineCodecFactory.getDecoder().decode(TextLineCodecFactory.SPLIT, null));
  }

}
