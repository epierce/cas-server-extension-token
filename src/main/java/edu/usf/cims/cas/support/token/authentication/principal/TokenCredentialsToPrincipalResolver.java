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

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;

/**
 * This class resolves the principal from the token credential 
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class TokenCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    
    @Override
    protected String extractPrincipalId(final Credentials credentials) {
        TokenCredentials tokenCredentials = (TokenCredentials) credentials;
        String principal = tokenCredentials.getUsername();
        return principal;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null && (TokenCredentials.class.isAssignableFrom(credentials.getClass()));
    }
}
