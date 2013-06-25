package edu.clayton.cas.support.token;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

public class TokenAttributesTest {
  private String json;

  @Before
  public void readJSON() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testTokenAttributes.json");
    File file = new File(url.toURI());
    FileInputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[(int)file.length()];

    fis.read(buffer);
    fis.close();

    this.json = new String(buffer);
  }

  @Test
  public void wholeShebang() {
    TokenAttributes tokenAttributes = new TokenAttributes(this.json);

    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
  }
}