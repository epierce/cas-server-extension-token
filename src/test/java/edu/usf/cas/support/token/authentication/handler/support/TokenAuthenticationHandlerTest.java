package edu.usf.cas.support.token.authentication.handler.support;

import edu.clayton.cas.support.token.keystore.JSONKeystore;
import edu.usf.cas.support.token.authentication.principal.TokenCredentials;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenAuthenticationHandlerTest {
  private String b64Token =
      "xo69dSlsDQ0hGhwIrfYzP4j1CqgsvjXHbEJb9vc2Tp8DZHBFgZFw8x6wYzyLDbTxyJk4CG5zsyt4\n" +
      "Mb6BbYtfNA/w5LQKiAz1uOpzB88NwuaUVtldIDyZnJ1rU5vqtmADo2ustZo17JTCKgJ81oaUttoU\n" +
      "Q4dMhGTv9+bUExU6BYY4thlr036qvtjAPnG5dgcfFO1NXRVj3TlqZv1ZOHLeMmROh1u8a75zhznH\n" +
      "MH6wuvG2kqgL2paCXOEqroQCmkXc5mwug6iag6dBIYCWSLATZwt73z+ANIKvAwn4UCCHqEvaYSaN\n" +
      "UxpMQciGUvtOlpXV";

  private TokenAuthenticationHandler handler;
  private TokenCredentials tokenCredentials;

  @Before
  public void setup() {
    this.handler = new TokenAuthenticationHandler();
    this.tokenCredentials = new TokenCredentials("auser", this.b64Token);
  }

  @Test
  public void testSupports() {
    assertTrue(this.handler.supports(tokenCredentials));
  }

  @Test
  public void testAuthSuccess() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithGoodKey.json");
    File keystoreFile = new File(url.toURI());
    JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

    this.handler.setEncryptionKey("abcdefghijklmnop");
    this.handler.setMaxDrift(Integer.MAX_VALUE);
    this.handler.setKeystore(jsonKeystore);

    assertTrue(this.handler.doAuthentication(this.tokenCredentials));
  }

  @Test(expected = BadCredentialsAuthenticationException.class)
  public void testAuthFailure() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithBadKey.json");
    File keystoreFile = new File(url.toURI());
    JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

    this.handler.setEncryptionKey("abcdefghijklmnop");
    this.handler.setMaxDrift(Integer.MAX_VALUE);
    this.handler.setKeystore(jsonKeystore);

    assertFalse(this.handler.doAuthentication(this.tokenCredentials));
  }
}