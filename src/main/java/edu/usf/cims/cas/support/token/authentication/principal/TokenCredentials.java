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
package edu.usf.cims.cas.support.token.authentication.principal;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.springframework.util.Assert;

import org.apache.commons.lang.StringUtils;

import org.jasig.cas.authentication.principal.Credentials;

import org.json.JSONObject;
import org.json.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a CAS-compatible credential using data from an AES128-encrypted token
 * 
 * @author Eric Pierce
 * @since 0.1
 */
public final class TokenCredentials implements Credentials {
    
  private static final long serialVersionUID = 2749515041385101768L;

  private static final Logger logger = LoggerFactory.getLogger(TokenCredentials.class);

  private String token;

  private String username;

  private Map<String, Object> userAttributes;

  public TokenCredentials(final String username, final String token) {
    Assert.notNull(token, "token cannot be null");
    Assert.notNull(token, "username cannot be null");
    this.token = token;
    this.username = username;
  }

  public final void setToken(final String token) {
    this.token = token;
  }

  public final String getToken() {
    return this.token;
  }

  public final void setUsername(final String username) {
    this.username = username;
  }

  public final String getUsername() {
    return this.username;
  }
    
  /**
  * Create a map of the User's Attributes from a JSONObject 
  *
  * @param JSONObject userProfile
  */
  public void setUserAttributes(JSONObject userProfile) {    
    Map<String,Object> userAttributes = new HashMap<String,Object>();
    
    try {
      if((userProfile.has("firstname"))&&(userProfile.has("lastname"))) {
        userAttributes.put("DisplayName", userProfile.get("firstname")+" "+userProfile.get("lastname"));
        userAttributes.put("FamilyName", userProfile.get("lastname"));
        userAttributes.put("GivenName", userProfile.get("firstname"));
      }
      if(userProfile.has("email")) {
        userAttributes.put("Email",userProfile.get("email"));
      }
      userAttributes.put("PreferredUsername", userProfile.get("username"));
    } 
    catch (JSONException e) {
      logger.error(e.getMessage());
    }
    
    this.userAttributes = userAttributes;
    logger.debug("userAttributes : {}", userAttributes);
      
  }

  public final Map<String, Object> getUserAttributes() {
    return this.userAttributes;
  }

  public String toString() {
    if (this.userAttributes != null && this.userAttributes.containsKey("PreferredUsername")){
      return (String) userAttributes.get("PreferredUsername");
    } else {
      return "[authentication token: " + this.username + ":" + this.token + "]";
    }
  }
}