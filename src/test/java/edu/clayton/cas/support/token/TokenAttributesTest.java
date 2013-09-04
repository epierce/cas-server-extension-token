package edu.clayton.cas.support.token;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenAttributesTest {
  private static final Logger log = LoggerFactory.getLogger(TokenAttributesTest.class);

  private String json;

  private void readJSON(String fileName) throws Exception {
    URL url = this.getClass().getClassLoader().getResource(fileName);
    File file = new File(url.toURI());
    FileInputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[(int)file.length()];

    fis.read(buffer);
    fis.close();

    this.json = new String(buffer);
  }

  @Test
  public void standardAttributes() throws Exception {
    log.info("Checking a standard attributes object");

    this.readJSON("testTokenAttributes.json");
    TokenAttributes tokenAttributes = new TokenAttributes(this.json);

    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
  }

  @Test
  public void multipleAttributes() throws Exception {
    log.info("Checking multiple values in an attribute");

    this.readJSON("testMultipleTokenAttributes.json");
    TokenAttributes tokenAttributes = new TokenAttributes(this.json);

    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
    assertTrue("[one, two, three]".equals(tokenAttributes.get("multiple").toString()));
  }

  @Test
  public void alternateAttributes() throws Exception {
    log.info("Checking an alternate attributes object");

    // An Active Directory style attributes map.
    this.readJSON("testAlternateTokenAttributes.json");
    TokenAttributes tokenAttributes = new TokenAttributes(this.json);

    ArrayList<String> requiredAttributes = new ArrayList<String>(2);
    requiredAttributes.add("sAMAccountName");
    requiredAttributes.add("mail");
    tokenAttributes.setRequiredTokenAttributes(requiredAttributes);

    log.info("Verifying required attributes are missing");
    assertFalse(tokenAttributes.isValid());

    log.info("Setting an attributes mapping");
    HashMap<String, String> attributesMap = new HashMap<String, String>(4);
    attributesMap.put("username", "sAMAccountName");
    attributesMap.put("firstName", "givenName");
    attributesMap.put("lastName", "sn");
    attributesMap.put("email", "mail");
    tokenAttributes.setTokenAttributesMap(attributesMap);

    log.info("Verifying required attributes are now present");
    assertTrue(tokenAttributes.isValid());

    log.info("Verifying attributes are set correctly");
    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertTrue("Foo".equals(tokenAttributes.getFirstName()));
    assertTrue("Bar".equals(tokenAttributes.getLastName()));
    assertTrue("foobar@example.com".equals(tokenAttributes.getEmail()));
  }

  @Test
  public void testUsernameOnlyAttributeList() throws Exception {
    log.info("Checking an username only attributes object");

    this.readJSON("testUsernameOnlyTokenAttributes.json");
    TokenAttributes tokenAttributes = new TokenAttributes(this.json);

    assertTrue("auser".equals(tokenAttributes.getUsername()));
    assertNull(tokenAttributes.getFirstName());
  }
}