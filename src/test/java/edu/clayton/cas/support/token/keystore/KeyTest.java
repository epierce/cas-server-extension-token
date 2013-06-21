package edu.clayton.cas.support.token.keystore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KeyTest {
  @Test
  public void testKey() {
    Key key = new Key("test", "123456789012345");

    assertEquals("test", key.name());
    assertTrue(new String(key.data()).equals("123456789012345"));

    key = new Key("test", "123456789012345".getBytes());
    assertEquals("test", key.name());
    assertTrue(new String(key.data()).equals("123456789012345"));
  }
}