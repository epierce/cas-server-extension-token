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
package edu.usf.cas.support.token.authentication;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import edu.usf.cas.support.token.authentication.principal.TokenCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a meta data populator for authentication using Janrain Engage. 
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class TokenAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationMetaDataPopulator.class);
    
    public Authentication populateAttributes(Authentication authentication, Credentials credentials) {
        if (credentials instanceof TokenCredentials) {
          TokenCredentials tokenCredentials = (TokenCredentials) credentials;
          final Principal simplePrincipal = new SimplePrincipal(authentication.getPrincipal().getId(),
                                                                  tokenCredentials.getUserAttributes());
            final MutableAuthentication mutableAuthentication = new MutableAuthentication(simplePrincipal,
                                                                                          authentication
                                                                                              .getAuthenticatedDate());
				logger.debug("attributes : {}",simplePrincipal.getAttributes());

            mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
            return mutableAuthentication;
        }
        return authentication;
    }
}
