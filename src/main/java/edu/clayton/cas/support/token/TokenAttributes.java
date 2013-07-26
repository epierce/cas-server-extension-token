package edu.clayton.cas.support.token;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines the object encoded in a {@link Token}'s "credentials" property.
 */
public class TokenAttributes extends ConcurrentHashMap<String, Object> {
  private final static Logger log = LoggerFactory.getLogger(TokenAttributes.class);

  private List<String> requiredTokenAttributes;
  private Map<String, String> tokenAttributesMap;

  /**
   * Initialize a {@linkplain TokenAttributes} object from a JSON
   * encoded string.
   *
   * @param data The JSON encoded string of data.
   */
  public TokenAttributes(String data) {
    this(data, null, null);
  }

  /**
   * Initialize a {@linkplain TokenAttributes} object from a JSON
   * encoded string. Also, set the required attributes list and the
   * attributes mapping.
   *
   * @param data The JSON encoded string of data.
   * @param requiredTokenAttributes A list of required attribute names.
   * @param tokenAttributesMap A map that maps incoming attribute names to properties of this object.
   */
  public TokenAttributes(String data, List requiredTokenAttributes, Map tokenAttributesMap)
  {
    Assert.notNull(data);
    this.requiredTokenAttributes = requiredTokenAttributes;
    this.tokenAttributesMap = tokenAttributesMap;

    JSONObject jsonObject;
    try {
      jsonObject= new JSONObject(data);

      String[] keys = JSONObject.getNames(jsonObject);

      for (String k : keys) {
        this.put(k, jsonObject.get(k));
      }

    } catch (JSONException e) {
      log.error("Could not parse TokenAttributes data!");
      log.debug(e.toString());
    }
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
   * <p>Defines a mapping of attribute names in the incoming "credentials" object
   * to properties of a {@link TokenAttributes} instance. The map structure
   * should be such that the key name is the {@linkplain TokenAttributes}
   * property name, and the value the key name of the credentials object.
   * For example:</p>
   *
   * <pre>
   *   {
   *     "firstName" : "fname",
   *     "lastName" : "lname",
   *     "email" : "email",
   *     "username" : "uname"
   *   }
   * </pre>
   *
   * <p>Note that all properties of a {@linkplain TokenAttributes} instance
   * are present in the mapping. You should do this whenever you define
   * an attribute mapping.</p>
   *
   * @param attributesMap A {@link Map} of attribute names.
   */
  public void setTokenAttributesMap(Map<String, String> attributesMap) {
    this.tokenAttributesMap = attributesMap;
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

    if (result == true && this.requiredTokenAttributes != null) {
      for (String attr : this.requiredTokenAttributes) {
        if (this.get(attr) == null) {
          result = false;
          break;
        }
      }
    }

    return result;
  }

  public String getEmail() {
    return (String) this.get(this.lookupAttributeName("email"));
  }

  public void setEmail(String email) {
    this.put(this.lookupAttributeName("email"), email);
  }

  public String getFirstName() {
    return (String) this.get(this.lookupAttributeName("firstName"));
  }

  public void setFirstName(String firstName) {
    this.put(this.lookupAttributeName("firstName"), firstName);
  }

  public String getLastName() {
    return (String) this.get(this.lookupAttributeName("lastName"));
  }

  public void setLastName(String lastName) {
    this.put(this.lookupAttributeName("lastName"), lastName);
  }

  public String getUsername() {
    return (String) this.get(this.lookupAttributeName("username"));
  }

  public void setUsername(String username) {
    this.put(this.lookupAttributeName("username"), username);
  }

  /**
   * Used to lookup the name of an attribute based on whether or not
   * there is an alternate mapping supplied by
   * {@link TokenAttributes#tokenAttributesMap}.
   *
   * @param attribute The name of the {@linkplain TokenAttributes} property to lookup.
   * @return The mapped name, or the passed in name.
   */
  private String lookupAttributeName(String attribute) {
    // .toLowerCase() because the original methods were not camel cased.
    String result = attribute.toLowerCase();
    String tmp;

    if (this.tokenAttributesMap != null) {
      tmp = this.tokenAttributesMap.get(attribute);
      result = (tmp == null) ? result : tmp;
    }

    return result;
  }
}