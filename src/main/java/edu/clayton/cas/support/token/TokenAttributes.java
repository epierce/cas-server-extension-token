package edu.clayton.cas.support.token;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines the object encoded in a {@link Token}'s "credentials" property.
 */
public class TokenAttributes extends ConcurrentHashMap<String, Object> {
  private final static Logger log = LoggerFactory.getLogger(TokenAttributes.class);

  private static final String DEFAULT_USERNAME_ATTR = "username";

  private List<String> requiredTokenAttributes;
  private String usernameAttribute;

  /**
   * Initialize a {@linkplain TokenAttributes} object from a JSON
   * encoded string.
   *
   * @param data The JSON encoded string of data.
   */
  public TokenAttributes(String data) {
    this(data, DEFAULT_USERNAME_ATTR, Arrays.asList(DEFAULT_USERNAME_ATTR));
  }

  /**
   * Initialize a {@linkplain TokenAttributes} object from a JSON
   * encoded string. Also, set the required attributes list and the
   * attributes mapping.
   *
   * @param data The JSON encoded string of data.
   * @param requiredTokenAttributes A list of required attribute names.
   * @param usernameAttribute The token attribute that contains the username for this principal.
   */
  public TokenAttributes(String data, String usernameAttribute, List<String> requiredTokenAttributes)
  {
    Assert.notNull(data);
    this.requiredTokenAttributes = requiredTokenAttributes;
    this.usernameAttribute = usernameAttribute;

    JSONObject jsonObject;
    try {
      jsonObject= new JSONObject(data);

      String[] keys = JSONObject.getNames(jsonObject);

      for (String k : keys) {
        Object entry = jsonObject.get(k);
        if (entry instanceof JSONArray) {
          this.put(k, toList( (JSONArray) entry));
        } else {
          this.put(k, entry.toString());
        } 
      }

    } catch (JSONException e) {
      log.error("Could not parse TokenAttributes data!");
      log.error(e.toString());
    }
  }

  private List<String> toList(JSONArray array) throws JSONException {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length(); i++) {
      list.add(array.get(i).toString());
    }
    return list;
  }

  /**
   * Defines a list of attributes that are required to be present in the
   * "credentials" object of the decrypted {@link Token}. A call to
   * {@link edu.clayton.cas.support.token.TokenAttributes#isValid()} must
   * be made to determine if the attributes defined by this list are present
   * on the {@linkplain TokenAttributes} instance.
   *
   * @param attributes A {@link List} of required attributes.
   */
  public void setRequiredTokenAttributes(List<String> attributes) {
    this.requiredTokenAttributes = attributes;
  }

  /**
   * Used to determine if the instance contains all of the
   * {@link TokenAttributes#requiredTokenAttributes}. If any of the
   * required attributes are missing, this will return {@code false}.
   *
   * @return {@code true} if all required attributes are present
   */
  public boolean isValid() {
    boolean result = true;

    // The username attribute will always be required.
    result = (this.getUsername() != null);

    if (result && this.requiredTokenAttributes != null) {
      for (String attr : this.requiredTokenAttributes) {
        if (this.get(attr) == null) {
          result = false;
          break;
        }
      }
    }

    return result;
  }

  public String getUsername() {
    return (String) this.get(this.usernameAttribute);
  }

}