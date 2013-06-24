package edu.clayton.cas.support.token;

import edu.clayton.cas.support.token.keystore.Key;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenTest {
  private String b64tokenData;
  private Key serverKey = new Key("abcdefghijklmnop");
  private Key clientKey = new Key("1234567890123456");
  private long generatedTime;

  @Before
  public void buildTokenSourceData() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testTokenAttributes.json");
    File file = new File(url.toURI());
    FileInputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[(int)file.length()];

    fis.read(buffer);
    fis.close();

    String b64credentials;
    String attributes = new String(buffer);
    byte[] encryptedAttributes;
    SecretKeySpec skey1 = new SecretKeySpec(this.clientKey.data(), "AES");
    Cipher cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher1.init(Cipher.ENCRYPT_MODE, skey1);
    encryptedAttributes = cipher1.doFinal(attributes.getBytes());
    b64credentials = Base64.encodeBase64String(encryptedAttributes);

    JSONObject tokenData = new JSONObject();
    this.generatedTime = (new Date()).getTime();
    tokenData.put("generated", this.generatedTime);
    tokenData.put("api_key", this.clientKey.name());
    tokenData.put("credentials", b64credentials);

    byte[] encryptedTokenData;
    SecretKeySpec skey2 = new SecretKeySpec(this.serverKey.data(), "AES");
    Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher2.init(Cipher.ENCRYPT_MODE, skey2);
    encryptedTokenData = cipher2.doFinal(tokenData.toString().getBytes());
    this.b64tokenData = Base64.encodeBase64String(encryptedTokenData);
  }

  @Test
  public void wholeShebang() {
    Token token = new Token(this.b64tokenData);

    assertNull(token.getAttributes());
    assertEquals((new Date(0L)).getTime(), token.getGenerated());

    token.setServerKey(this.serverKey);
    TokenAttributes tokenAttributes = token.getAttributes();

    assertEquals(this.generatedTime, token.getGenerated());
    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
  }
}