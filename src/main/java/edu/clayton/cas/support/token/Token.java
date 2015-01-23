package edu.clayton.cas.support.token;

import edu.clayton.cas.support.token.keystore.Key;
import edu.clayton.cas.support.token.util.Crypto;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>A {@linkplain Token} is derived from data received from a client. This
 * data is a {@link Base64} encoded string. When decoded, this string is an
 * AES encrypted string that decrypts to another string that represents a
 * {@link JSONObject} with the following properties:</p>
 *
 * <ul>
 *   <li>
 *     {@code generated}: a timestamp, in milliseconds, at which the token
 *     was created
 *   </li>
 *   <li>
 *     {@code credentials}: a serialized {@link TokenAttributes} object
 *   </li>
 * </ul>
 *
 * <p>Initially, the {@linkplain Token} is not decrypted. Decryption will be
 * attempted when either the
 * {@link edu.clayton.cas.support.token.Token#getGenerated()} or
 * {@link edu.clayton.cas.support.token.Token#getAttributes()} methods are
 * invoked. Thus, it is imperative that
 * {@link Token#setKey(edu.clayton.cas.support.token.keystore.Key)}
 * method be invoked prior to accessing either of these properties.</p>
 */
public class Token {
  private final static Logger log = LoggerFactory.getLogger(Token.class);

  private Key key;
  private String tokenData;
  private boolean isDecoded = false;

  private long generated;
  private TokenAttributes attributes;
  private List<String> requiredTokenAttributes;
  private Map<String,String> tokenAttributesMap;

  /**
   * Initializes a {@linkplain Token} object from a
   * {@link org.apache.commons.codec.binary.Base64} encoded data string.
   *
   * @param data The data to decode into a {@linkplain Token}.
   */
  public Token(String data)
  {
    this.tokenData = data;
  }

  /**
   * Retrieve the {@link TokenAttributes} object associated with this
   * {@linkplain Token}. The attributes include the user's username, first name,
   * last name, and email address.
   *
   * @return A valid {@linkplain TokenAttributes} object or null if not decrypted.
   */
  public TokenAttributes getAttributes() {
    TokenAttributes attrs = null;

    if (this.isDecoded) {
      attrs = this.attributes;
    } else {
      try {
        this.decryptData();
      } catch (Exception e) {
        log.error("No TokenAttributes available!");
      }
      attrs = this.attributes;
    }

    return attrs;
  }

  /**
   * Return the timestamp, in milliseconds, when the {@linkplain Token} was
   * generated.
   *
   * @return The timestamp from the client or the epoch if not decrypted.
   */
  public long getGenerated() {
    long returnDate = (new Date(0L)).getTime();

    if (this.isDecoded) {
      returnDate = this.generated;
    } else {
      try {
        this.decryptData();
      } catch (Exception e) {
        log.error("No generated timestamp available!");
      }
      returnDate = this.generated;
    }

    return returnDate;
  }

  /**
   * Return the name of the key used to generate this token.  By convention, this is also an identifier for the
   * application that generated the token.
   *
   * @return The key name
   */
  public String getKeyGenerator() {
    return this.key.name();
  }

  /**
   * Define the crypto key that will be used to decode the {@linkplain Token}
   * data.
   *
   * @param key A valid {@link Key} object.
   */
  public void setKey(Key key) {
    this.key = key;
  }

  public void setRequiredTokenAttributes(List<String> requiredTokenAttributes) {
    this.requiredTokenAttributes = requiredTokenAttributes;
  }

  public void setTokenAttributesMap(Map<String,String> tokenAttributesMap) {
    this.tokenAttributesMap = tokenAttributesMap;
  }

  private void decryptData() throws Exception {
    try {
      log.debug(
          "Decrypting token with key = `{}`",
          new String(this.key.data())
      );
      String decryptedString = Crypto.decryptEncodedStringWithKey(
          this.tokenData,
          this.key
      );
      JSONObject jsonObject = new JSONObject(decryptedString);
      log.debug("Decrypted token:");
      log.debug(jsonObject.toString());

      this.generated = jsonObject.getLong("generated");
      this.attributes = new TokenAttributes(
          jsonObject.getJSONObject("credentials").toString(),
          this.requiredTokenAttributes,
          this.tokenAttributesMap
      );
      this.isDecoded = true;

      log.debug("Token successfully decrypted.");
    } catch (Exception e) {
      log.error("There was a problem decrypting the token data!");
      log.debug(e.toString());
      throw e;
    }
  }

  public String toString(){
    return this.getKeyGenerator()+"["+this.getGenerated()+"]";
  }
}