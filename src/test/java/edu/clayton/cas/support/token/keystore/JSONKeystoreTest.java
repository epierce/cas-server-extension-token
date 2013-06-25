package edu.clayton.cas.support.token.keystore;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

public class JSONKeystoreTest {
  private JSONKeystore keystore;

  @Before
  public void buildStore() throws URISyntaxException {
    URL url = this.getClass().getClassLoader().getResource("testStore.json");
    File file = new File(url.toURI());
    this.keystore = new JSONKeystore(file);
  }

  @Test
  public void testKeys() {
    Key fooKey = this.keystore.getKeyNamed("foo");
    Key barKey = this.keystore.getKeyNamed("bar");
    Key foobarKey = this.keystore.getKeyNamed("foobar");

    assertEquals("foo", fooKey.name());
    assertTrue(new String(fooKey.data()).equals("123456789012345"));

    assertEquals("bar", barKey.name());
    assertTrue(new String(barKey.data()).equals("098765432109876"));

    assertEquals("foobar", foobarKey.name());
    assertTrue(new String(foobarKey.data()).equals("abcdefghijklmno"));
  }

  @Test
  public void testAddRemoveKey() {
    this.keystore.addKey(new Key("newKey", "123456789012345"));
    Key newKey = this.keystore.getKeyNamed("newKey");

    assertNotNull(newKey);
    assertEquals("newKey", newKey.name());
    assertTrue(new String(newKey.data()).equals("123456789012345"));

    this.keystore.removeKeyNamed("newKey");
    assertNull(this.keystore.getKeyNamed("newKey"));
  }
}