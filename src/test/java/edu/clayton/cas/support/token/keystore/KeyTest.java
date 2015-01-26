package edu.clayton.cas.support.token.keystore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyTest {
  @Test
  public void testKey() {
    Key key = new Key("test", "UyfGUrqZeFkDdJDzaCGkCApAAqwJnHLm");

    assertEquals("test", key.name());
    assertTrue(new String(key.data()).equals("UyfGUrqZeFkDdJDzaCGkCApAAqwJnHLm"));

    key = new Key("test", "UyfGUrqZeFkDdJDzaCGkCApAAqwJnHLm".getBytes());
    assertEquals("test", key.name());
    assertTrue(new String(key.data()).equals("UyfGUrqZeFkDdJDzaCGkCApAAqwJnHLm"));
  }

  @Test
  public void testKeysEqual() {
    Key key1 = new Key("foobar");
    Key key2 = new Key("foobar");
    Key key3 = new Key("barfoo");

    assertTrue(key1.equals(key2));
    assertFalse(key1.equals(key3));
  }
}