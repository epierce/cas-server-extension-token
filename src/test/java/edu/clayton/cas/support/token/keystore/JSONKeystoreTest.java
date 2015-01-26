package edu.clayton.cas.support.token.keystore;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

public class JSONKeystoreTest {
  private File keystoreFile;
  private JSONKeystore keystore;

  @Before
  public void buildStore() throws URISyntaxException {
    URL url = this.getClass().getClassLoader().getResource("testStore.json");
    this.keystoreFile = new File(url.toURI());
    this.keystore = new JSONKeystore(this.keystoreFile);
  }

  @Test
  public void testKeys() {
    Key fooKey = this.keystore.getKeyNamed("foo");
    Key barKey = this.keystore.getKeyNamed("bar");
    Key foobarKey = this.keystore.getKeyNamed("foobar");

    assertEquals("foo", fooKey.name());
    assertTrue(new String(fooKey.data()).equals("pqrQtXkVPrKyVEWQwqMmsNCVmqWzhvaK"));

    assertEquals("bar", barKey.name());
    assertTrue(new String(barKey.data()).equals("xagkrotMHogRhPpqLtCYsPtbYqdihfqw"));

    assertEquals("foobar", foobarKey.name());
    assertTrue(new String(foobarKey.data()).equals("UyfGUrqZeFkDdJDzaCGkCApAAqwJnHLm"));
  }

  @Test
  public void testAddRemoveKey() {
    this.keystore.addKey(new Key("newKey", "xTuWpxJkLZpqRtDqXRJHePkeAQLniAXN"));
    Key newKey = this.keystore.getKeyNamed("newKey");

    assertNotNull(newKey);
    assertEquals("newKey", newKey.name());
    assertTrue(new String(newKey.data()).equals("xTuWpxJkLZpqRtDqXRJHePkeAQLniAXN"));

    this.keystore.removeKeyNamed("newKey");
    assertNull(this.keystore.getKeyNamed("newKey"));
  }

  /**
   * This test simulates Spring adding the keystore file after it has
   * already created an instance of {@link JSONKeystore}.
   */
  @Test
  public void nullConstructor() {
    JSONKeystore jsonKeystore = new JSONKeystore(null);
    jsonKeystore.setStoreFile(this.keystoreFile);

    Key fooKey = jsonKeystore.getKeyNamed("foo");

    assertNotNull(fooKey);
    assertEquals("foo", fooKey.name());
    assertTrue(new String(fooKey.data()).equals("pqrQtXkVPrKyVEWQwqMmsNCVmqWzhvaK"));
  }
}