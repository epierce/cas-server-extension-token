package edu.clayton.cas.support.token;

import edu.clayton.cas.support.token.keystore.Key;
import edu.clayton.cas.support.token.util.Crypto;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.*;

public class TokenTest {
  private String b64tokenData;
  private Key serverKey = new Key("alphabet_key", "abcdefghijklmnop");
  private Key clientKey = new Key("alphabet_key", "abcdefghijklmnop");
  private long generatedTime;

  @Before
  public void buildTokenSourceData() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testTokenAttributes.json");
    File file = new File(url.toURI());
    FileInputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[(int)file.length()];

    fis.read(buffer);
    fis.close();

    JSONObject tokenData = new JSONObject();
    this.generatedTime = (new Date()).getTime();
    tokenData.put("generated", this.generatedTime);
    tokenData.put("credentials", new JSONObject(new String(buffer)));

    this.b64tokenData = Crypto.encryptWithKey(
        tokenData.toString(),
        new String(this.serverKey.data())
    );
  }

  @Test
  public void wholeShebang() {
    Token token = new Token(this.b64tokenData);

    assertNull(token.getAttributes());
    assertEquals((new Date(0L)).getTime(), token.getGenerated());

    token.setKey(this.serverKey);
    TokenAttributes tokenAttributes = token.getAttributes();

    assertEquals(this.generatedTime, token.getGenerated());
    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
  }
}