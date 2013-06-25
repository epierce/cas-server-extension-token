package edu.clayton.cas.support.token;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Defines the object encoded in a {@link Token}'s "credentials" property.
 */
public class TokenAttributes {
  private final static Logger log = LoggerFactory.getLogger(TokenAttributes.class);

  private String username;
  private String firstName;
  private String lastName;
  private String email;

  /**
   * Initialize a {@linkplain TokenAttributes} object from a JSON
   * encoded string.
   *
   * @param data The JSON encoded string of data.
   */
  public TokenAttributes(String data) {
    Assert.notNull(data);
    JSONObject jsonObject;

    try {
      jsonObject= new JSONObject(data);
      this.username = jsonObject.getString("username");
      this.firstName = jsonObject.getString("firstname");
      this.lastName = jsonObject.getString("lastname");
      this.email = jsonObject.getString("email");
    } catch (JSONException e) {
      log.error("Could not parse TokenAttributes data!");
      log.debug(e.toString());
    }
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}