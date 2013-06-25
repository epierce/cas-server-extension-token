package edu.usf.cims.cas.support.token.authentication.handler.support;

import edu.clayton.cas.support.token.keystore.JSONKeystore;
import edu.usf.cims.cas.support.token.authentication.principal.TokenCredentials;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenAuthenticationHandlerTest {
  private String b64Token =
      "iLqtjso73xuX6Z1rmkvJ2cgvgWgPqpEeRDg4tRsPachItOgH5ehHBbd93js1kgi5jJ29f57Wa7r8\n" +
      "Tq/jl0Mis+bAWdOJMXvmqPq68RlScuBbsAMs6488rntGILGRqsc+YK8Q+VKGvx9tBqZmRjUfcDBY\n" +
      "YieGnGqxysiX2vAhKztLTX+RwBMnwjCpSLXl5+Ja5mDdxfGaA8N+1ZaaNGVffw==";

  private TokenAuthenticationHandler handler;
  private TokenCredentials tokenCredentials;

  @Before
  public void setup() {
    this.handler = new TokenAuthenticationHandler();
    this.tokenCredentials = new TokenCredentials(
        "auser",
        this.b64Token,
        "alphabet_key"
    );
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

    this.handler.setKeystore(jsonKeystore);
    this.handler.setEncryptionKey("abcdefghijklmnop");
    this.handler.setMaxDrift(Integer.MAX_VALUE);

    assertTrue(this.handler.doAuthentication(this.tokenCredentials));
  }

  @Test(expected = BadCredentialsAuthenticationException.class)
  public void testAuthFailure() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithBadKey.json");
    File keystoreFile = new File(url.toURI());
    JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

    this.handler.setKeystore(jsonKeystore);
    this.handler.setEncryptionKey("abcdefghijklmnop");
    this.handler.setMaxDrift(Integer.MAX_VALUE);

    assertFalse(this.handler.doAuthentication(this.tokenCredentials));
  }
}