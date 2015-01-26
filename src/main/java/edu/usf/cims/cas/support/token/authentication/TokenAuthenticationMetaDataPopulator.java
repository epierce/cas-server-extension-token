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

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;

/**
 * Metadata populator for token authentication.
 *
 * @author Eric Pierce
 * @since 1.0.0
 */
public final class TokenAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    /***
     * Attribute to store the name of the token creator.
     */
    public static final String TOKEN_GENERATOR = "tokenGenerator";

    /***
     * Attribute to store the time of token generation.
     */
    public static final String TOKEN_GENERATION_TIME = "generated";

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        builder.addAttribute(TOKEN_GENERATOR, tokenCredential.getToken().getKeyGenerator());
        builder.addAttribute(TOKEN_GENERATION_TIME, new java.util.Date(tokenCredential.getToken().getGenerated()).toString());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof TokenCredential;
    }
}