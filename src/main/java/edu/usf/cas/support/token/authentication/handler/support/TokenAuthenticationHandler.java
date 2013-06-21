/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.usf.cas.support.token.authentication.handler.support;

import edu.usf.cas.support.token.authentication.principal.TokenCredentials;
import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.util.Date;

/**
 * This handler authenticates token credentials
 *
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class TokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

  /** Key used for AES128 encryption **/
  private String encryptionKey;

  /** Maximum amount of time (before or after current time) that the 'generated' parameter in the supplied token can differ from the server **/
  private int maxDrift;

  public boolean supports(Credentials credentials) {
      return credentials != null && (TokenCredentials.class.isAssignableFrom(credentials.getClass()));
  }

  @Override
  protected boolean doAuthentication(Credentials credentials) throws AuthenticationException {
    TokenCredentials credential = (TokenCredentials) credentials;

    try {
      String result = decrypt(this.encryptionKey, credential.getToken());
      log.debug("Got decryption result : {}", result);

      JSONObject json = new JSONObject(result);
      log.debug("Got username from token : {}", json.get("username"));
      String jsonUsername = json.getString("username");

      //Get the difference between the generated time and now
      int genTimeDiff = (int) (new Date().getTime() - json.getLong("generated")) / 1000;
      if (genTimeDiff < 0) genTimeDiff = genTimeDiff * -1;
      log.debug("Token generated {} seconds ago", genTimeDiff);


      if (genTimeDiff > this.maxDrift) {
        log.warn("Authentication Error: Token expired for {}", jsonUsername);
        throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.expired");
      }
      if (jsonUsername.equals(credential.getUsername())) {
        log.debug("Authentication Success");
        credential.setUserAttributes(json);
        return true;
      }
      return false;
    } catch (InvalidKeyException e) {
      log.warn(e.getMessage());
      throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.key");
    } catch (Exception e) {
      log.warn(e.getMessage());
      throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.json");
    }
  }

  private String decrypt(String key, String input) throws InvalidKeyException {
    byte[] output = null;
    try {
      SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, skey);
      output = cipher.doFinal(Base64.decodeBase64(input));
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new InvalidKeyException();

    }
    return new String(output);
  }

  public final void setEncryptionKey(final String encryptionKey) {
    this.encryptionKey = encryptionKey;
  }

  public final void setMaxDrift(final int maxDrift) {
    this.maxDrift = maxDrift;
  }


}
