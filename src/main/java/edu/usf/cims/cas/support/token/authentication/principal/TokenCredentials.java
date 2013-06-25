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
package edu.usf.cims.cas.support.token.authentication.principal;

import edu.clayton.cas.support.token.Token;
import edu.clayton.cas.support.token.TokenAttributes;
import org.jasig.cas.authentication.principal.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * This class creates a CAS-compatible credential using data from an AES128-encrypted token
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class TokenCredentials implements Credentials {

  private static final long serialVersionUID = 2749515041385101769L;

  private static final Logger logger = LoggerFactory.getLogger(TokenCredentials.class);

  private Token token;

  private String username;

  private Map<String, Object> userAttributes;

  public TokenCredentials(final String username, final String token) {
    Assert.notNull(token, "token cannot be null");
    Assert.notNull(token, "username cannot be null");
    this.token = new Token(token);
    this.username = username;
  }

  public final void setToken(final Token token) {
    this.token = token;
  }

  public final Token getToken() {
    return this.token;
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

  public void setUserAttributes(TokenAttributes userProfile) {
    Assert.notNull(userProfile);
    this.userAttributes = new HashMap<String, Object>();
    this.userAttributes.put("ProviderName", "UsfNetId");
    this.userAttributes.put(
        "DisplayName",
        String.format("%s %s", userProfile.getFirstName(), userProfile.getLastName())
    );
    this.userAttributes.put("FamilyName", userProfile.getLastName());
    this.userAttributes.put("GivenName", userProfile.getFirstName());
    this.userAttributes.put("Email", userProfile.getEmail());
    this.userAttributes.put("PreferredUsername", userProfile.getUsername());
  }

  public String toString() {
    return "[authentication token: " + this.username + ":" + this.token + "]";
  }
}
