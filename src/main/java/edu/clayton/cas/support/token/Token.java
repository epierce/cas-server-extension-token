package edu.clayton.cas.support.token;

import edu.clayton.cas.support.token.keystore.Key;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

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
 *     {@code api_key}: a string that defines a key unique to the client service
 *   </li>
 *   <li>
 *     {@code credentials}: another {@linkplain Base64} encoded data string
 *   </li>
 * </ul>
 *
 * <p>The {@code credentials} property is a {@linkplain Base64} encoded, AES
 * encrypted, string. The key used to encrypt the string is defined in the
 * {@code api_key} property. When decrypted, this string represents a
 * {@linkplain JSONObject} that is a representation of a
 * {@link TokenAttributes} object.</p>
 *
 * <p>Initially, the {@linkplain Token} is not decrypted. Decryption will be
 * attempted when either the
 * {@link edu.clayton.cas.support.token.Token#getGenerated()} or
 * {@link edu.clayton.cas.support.token.Token#getAttributes()} methods are
 * invoked. Thus, it is imperative that
 * {@link Token#setServerKey(edu.clayton.cas.support.token.keystore.Key)}
 * method be invoked prior to accessing either of these properties.</p>
 */
public class Token {
  private final static Logger log = LoggerFactory.getLogger(Token.class);

  private Key serverKey;
  private Key clientKey;
  private String tokenData;
  private boolean isDecoded = false;

  private long generated;
  private TokenAttributes attributes;

  /**
   * Initializes a {@linkplain Token} object from a
   * {@link org.apache.commons.codec.binary.Base64} encoded data string.
   *
   * @param data The data to decode into a {@linkplain Token}.
   */
  public Token(String data) {
    this.tokenData = data;
  }

  /**
   * Retrieve the encryption key that is used to decode the embedded attributes.
   *
   * @return The {@link Token#clientKey}.
   */
  public Key getClientKey() {
    return this.clientKey;
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
   * Define the crypto key that will be used to decode the {@linkplain Token}
   * data.
   *
   * @param key A valid {@link Key} object.
   */
  public void setServerKey(Key key) {
    this.serverKey = key;
  }

  private void decryptData() throws Exception {
    byte[] output = null;

    try {
      // Decode the first chunk.
      log.debug(
          "Decrypting token with key = `{}`",
          new String(this.serverKey.data())
      );
      SecretKeySpec skey1 = new SecretKeySpec(this.serverKey.data(), "AES");
      Cipher cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher1.init(Cipher.DECRYPT_MODE, skey1);

      output = cipher1.doFinal(Base64.decodeBase64(this.tokenData));
      JSONObject jsonObject = new JSONObject(new String(output));
      log.debug("Decrypted token:");
      log.debug(jsonObject.toString());

      this.generated = jsonObject.getLong("generated");
      this.clientKey = new Key(jsonObject.getString("api_key"));

      log.debug(
          "Decrypting credentials with api_key = `{}`",
          new String(this.clientKey.data())
      );
      SecretKeySpec skey2 = new SecretKeySpec(this.clientKey.data(), "AES");
      Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher2.init(Cipher.DECRYPT_MODE, skey2);

      byte[] creds = Base64.decodeBase64(jsonObject.getString("credentials"));
      output = cipher2.doFinal(creds);
      log.debug("Decrypted credentials:");
      log.debug(new String(output));

      this.attributes = new TokenAttributes(new String(output));
      this.isDecoded = true;

      log.debug("Token successfully decrypted.");
    } catch (Exception e) {
      log.error("There was a problem decrypting the token data!");
      log.debug(e.toString());
      throw e;
    }
  }
}