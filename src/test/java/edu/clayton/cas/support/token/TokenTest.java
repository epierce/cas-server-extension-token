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
  private Key serverKey = new Key("alphabet_key", "EKyrqGJnFQrUzohURXsJprFgBAKAPtrv");
  private Key clientKey = new Key("alphabet_key", "EKyrqGJnFQrUzohURXsJprFgBAKAPtrv");
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

    try {
      this.b64tokenData = Crypto.encryptWithKey(
              tokenData.toString(),
              new String(this.clientKey.data())
      );
    // If we got an Invalid Key length exception, they need to download the JCE policy files
    } catch (java.security.InvalidKeyException e) {
      System.out.println("******************************************************************************************");
      System.out.println("AES-256 is not supported on this system!");
      System.out.println("You must install the JCE Unlimited Strength Jurisdiction Policy Files");
      System.out.println("(http://www.oracle.com/technetwork/java/javase/downloads/index.html)");
      System.out.println("It is the user's responsibility to verify that this action is permissible under local regulations.");
      System.out.println("******************************************************************************************");
      System.exit(1);
    }
  }

  @Test
  public void wholeShebang() {
    Token token = new Token(this.b64tokenData);
    token.setUsernameAttribute("username");

    assertNull(token.getAttributes());
    assertEquals((new Date(0L)).getTime(), token.getGenerated());

    token.setKey(this.serverKey);
    TokenAttributes tokenAttributes = token.getAttributes();

    assertEquals(this.generatedTime, token.getGenerated());
    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.get("firstname")));
    assertTrue("Bar".equals(tokenAttributes.get("lastname")));
    assertTrue("foobar@example.com".equals(tokenAttributes.get("email")));

    tokenAttributes.put("answer", 42);
    assertEquals(42, tokenAttributes.get("answer"));
  }
}