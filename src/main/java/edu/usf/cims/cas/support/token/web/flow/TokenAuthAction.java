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
package edu.usf.cims.cas.support.token.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;

import edu.usf.cims.cas.support.token.authentication.principal.TokenCredentials;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class represents an action in the webflow to retrieve user information from an AES128 encrypted token. If the auth_token 
 * parameter exists in the web request, it is used to create a new TokenCredential.
 * 
 * @author Eric Pierce
 * @since 0.1
 */
public final class TokenAuthAction extends AbstractAction {
    
    private static final String TOKEN_PARAMETER = "auth_token";

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthAction.class);
    
    @NotNull 
    private CentralAuthenticationService centralAuthenticationService;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        HttpSession session = request.getSession();
        
        // get token and username values
        String authTokenValue = request.getParameter(TOKEN_PARAMETER);
        String username = request.getParameter("username");

        // Token exists
        if ( StringUtils.isNotBlank(authTokenValue) && StringUtils.isNotBlank(username)) {

            logger.debug("Got an authentication token: {} for username {}.", authTokenValue, username);

            // get credential
            @SuppressWarnings("unchecked")
            TokenCredentials credential = new TokenCredentials(username,authTokenValue);
            
            // put service in session from flow scope
            Service service = (Service) context.getFlowScope().get("service");
            session.setAttribute("service", service);
                       
            try {
                WebUtils.putTicketGrantingTicketInRequestScope(context, this.centralAuthenticationService
                    .createTicketGrantingTicket(credential));
                return success();
            } catch (final TicketException e) {
                return error();
            }
        } 
        return error();
    }
    
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
