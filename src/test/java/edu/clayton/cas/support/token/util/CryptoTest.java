package edu.clayton.cas.support.token.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.clayton.cas.support.token.keystore.Key;

import static org.junit.Assert.*;

public class CryptoTest {
  private final static Logger log = LoggerFactory.getLogger(CryptoTest.class);

  @Test
  public void testGenerateAes256KeyWithSeed() {
    log.info("testGenerateAes128KeyWithSeed()");

    String seed = "asldjfaphqjfnpq";
    String key = Crypto.generateAes256KeyWithSeed(seed);

    assertNotNull(key);
    assertFalse(key.contains("/\\="));

    log.info("Generated key is `{}`", key);
  }

  @Test
  public void testEncryptWithKey() {
    Key key = new Key("test", "12345678901234561234567890123456");
    String plaintext = "Mary had a little lamb.";

    try {
      String cipherText = Crypto.encryptWithKey(plaintext, new String(key.data()));
      String decrypted = Crypto.decryptEncodedStringWithKey(cipherText, key);

      assertNotEquals(plaintext, cipherText);
      assertEquals(plaintext, decrypted);

    } catch (Exception e){

    }
  }


}