/* Copyright 2013 University of South Florida.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package edu.usf.cims.cas.support.token.authentication.handler.support;

import edu.clayton.cas.support.token.Token;
import edu.clayton.cas.support.token.keystore.Key;
import edu.clayton.cas.support.token.keystore.Keystore;
import edu.usf.cims.cas.support.token.authentication.principal.TokenCredentials;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

/**
 * This handler authenticates token credentials 
 * 
 * @author Eric Pierce
 * @since 0.1
 */
public final class TokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
  private final static Logger log = LoggerFactory.getLogger(TokenAuthenticationHandler.class);

  /** An instance of a {@link edu.clayton.cas.support.token.keystore.Keystore}. **/
  private Keystore keystore;

  /* Maximum amount of time (before or after current time) that the 'generated' parameter 
   * in the supplied token can differ from the server */
  private int maxDrift;

  public boolean supports(Credentials credentials) {
      return credentials != null && (TokenCredentials.class.isAssignableFrom(credentials.getClass()));
  }
    
  @Override
  protected boolean doAuthentication(Credentials credentials) throws AuthenticationException {
    boolean result = false;
    TokenCredentials credential = (TokenCredentials) credentials;

    // Check to see if the api_key is allowed.
    Key apiKey = this.keystore.getKeyNamed(credential.getTokenService());
    if (apiKey == null) {
      log.warn("API key not found in keystore!");
      throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.apikey");
    }

    // Configure the credential's token so that it can be decrypted.
    Token token = credential.getToken();
    token.setPrimaryKey(apiKey);
    credential.setToken(token);

    try {
      credential.setUserAttributes(token.getAttributes());
    } catch (Exception e) {
      log.warn("Could not decrypt token!");
      throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.key");
    }

    String credUsername = credential.getUsername();
    String attrUsername = (String) credential.getUserAttributes().get("PreferredUsername");

    log.debug("Got username from token : {}", credUsername);

    // Get the difference between the generated time and now.
    int genTimeDiff = (int) (new Date().getTime() - token.getGenerated()) / 1000;
    if (genTimeDiff < 0) genTimeDiff = genTimeDiff * -1;
    log.debug("Token generated {} seconds ago", genTimeDiff);

    if (genTimeDiff > this.maxDrift) {
      log.warn("Authentication Error: Token expired for {}", credUsername);
      throw new BadCredentialsAuthenticationException("error.authentication.credentials.bad.token.expired");
    }

    if (attrUsername.equals(credUsername) &&
        Arrays.equals(apiKey.data(), token.getEmbeddedKey().data()))
    {
      log.debug("Authentication Success");
      result = true;
    }

    return result;
  }

  public final void setKeystore(final Keystore keystore) {
    this.keystore = keystore;
  }

  public final void setMaxDrift(final int maxDrift){
    this.maxDrift = maxDrift;
  }
    
}