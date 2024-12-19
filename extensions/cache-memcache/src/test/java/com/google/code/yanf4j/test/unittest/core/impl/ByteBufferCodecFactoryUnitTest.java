package com.google.code.yanf4j.test.unittest.core.impl;

import com.google.code.yanf4j.buffer.IoBuffer;
import com.google.code.yanf4j.core.CodecFactory.Encoder;
import com.google.code.yanf4j.core.impl.ByteBufferCodecFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-24 10:49:54
 */

public class ByteBufferCodecFactoryUnitTest {
  ByteBufferCodecFactory codecFactory;

  @BeforeEach
  public void setUp() {
    this.codecFactory = new ByteBufferCodecFactory();
  }

  @Test
  public void testEncodeNormal() throws Exception {
    Encoder encoder = this.codecFactory.getEncoder();
    Assertions.assertNotNull(encoder);
    IoBuffer buffer = encoder.encode(IoBuffer.wrap("hello".getBytes("utf-8")), null);
    Assertions.assertNotNull(buffer);
    Assertions.assertTrue(buffer.hasRemaining());
    Assertions.assertArrayEquals("hello".getBytes("utf-8"), buffer.array());

  }

  @Test
  public void testEncodeEmpty() throws Exception {
    Encoder encoder = this.codecFactory.getEncoder();
    Assertions.assertNull(encoder.encode(null, null));
    Assertions.assertEquals(IoBuffer.allocate(0), encoder.encode(IoBuffer.allocate(0), null));
  }

  @Test
  public void decodeNormal() throws Exception {
    Encoder encoder = this.codecFactory.getEncoder();
    Assertions.assertNotNull(encoder);
    IoBuffer buffer = encoder.encode(IoBuffer.wrap("hello".getBytes("utf-8")), null);

    IoBuffer decodeBuffer = (IoBuffer) this.codecFactory.getDecoder().decode(buffer, null);
    Assertions.assertEquals(IoBuffer.wrap("hello".getBytes("utf-8")), decodeBuffer);
  }

  @Test
  public void decodeEmpty() throws Exception {
    Assertions.assertNull(this.codecFactory.getDecoder().decode(null, null));
    Assertions.assertEquals(IoBuffer.allocate(0),
        this.codecFactory.getDecoder().decode(IoBuffer.allocate(0), null));
  }

  @Test
  public void testDirectEncoder() throws Exception {
    this.codecFactory = new ByteBufferCodecFactory(true);
    IoBuffer msg = IoBuffer.allocate(100);
    IoBuffer buffer = this.codecFactory.getEncoder().encode(msg, null);
    Assertions.assertTrue(buffer.isDirect());
  }

}
