/* Copyright 2015 University of South Florida.
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
package edu.usf.cims.cas.support.token.authentication;

import edu.clayton.cas.support.token.Token;
import edu.clayton.cas.support.token.TokenAttributes;
import org.jasig.cas.authentication.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * This class creates a CAS-compatible credential using data from an AES128-encrypted token
 * 
 * @author Eric Pierce
 * @since 0.1
 */
public final class TokenCredentials implements Credential {
    
  private static final long serialVersionUID = 2749515041385101770L;

  private static final Logger logger = LoggerFactory.getLogger(TokenCredentials.class);

  private Token token;

  private String username;

  private String tokenService;

  private Map<String, Object> userAttributes;

  public TokenCredentials(final String username, final String token, final String tokenService) {
    Assert.notNull(token, "token cannot be null");
    Assert.notNull(username, "username cannot be null");
    Assert.notNull(tokenService, "tokenService cannot be null");
    this.token = new Token(token);
    this.tokenService = tokenService;
    this.username = username;
  }

  public final void setToken(final Token token) {
    this.token = token;
  }

  public final Token getToken() {
    return this.token;
  }

  public final String getTokenService() {
    return this.tokenService;
  }

  public final void setUsername(final String username) {
    this.username = username;
  }

  public final String getUsername() {
    return this.username;
  }

  public final Map<String, Object> getUserAttributes() {
    return this.userAttributes;
  }

  public final String getId() {
    return this.username;
  }

  /**
   * Create a map of the user's attributes for use by the CAS server classes.
   *
   * @param userProfile The TokenAttributes
   */
  public void setUserAttributes(TokenAttributes userProfile) {
    Assert.notNull(userProfile);
    this.userAttributes = userProfile;
  }

  public String toString() {
    if (this.userAttributes != null && this.userAttributes.containsKey("PreferredUsername")){
      return (String) userAttributes.get("PreferredUsername");
    } else {
      return "[authentication token: " + this.username + ":" + this.token + "]";
    }
  }
}