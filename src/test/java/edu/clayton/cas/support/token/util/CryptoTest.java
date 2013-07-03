package edu.clayton.cas.support.token.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CryptoTest {
  private final static Logger log = LoggerFactory.getLogger(CryptoTest.class);

  @Test
  public void testGenerateAes128KeyWithSeed() {
    log.info("testGenerateAes128KeyWithSeed()");

    String seed = "asldjfaphqjfnpq";
    String key = Crypto.generateAes128KeyWithSeed(seed);

    assertNotNull(key);
    assertFalse(key.contains("/\\="));

    log.info("Generated key is `{}`", key);
  }
}