/*
 * Copyright 2021 The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acuity.va.security.auth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Adds the ability to inject an AuthenticationProvider into OAuth2ClientAuthenticationProcessingFilter so that you can use the AuthenticationProvider to
 * authenticate the user from the token brought back from ping
 * <p>
 * PING
 *
 * @author Glen
 */
public class OAuth2ClientAuthenticationWithProviderProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2ClientAuthenticationWithProviderProcessingFilter.class);

    private AuthenticationProvider authenticationProvider;

    public OAuth2ClientAuthenticationWithProviderProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        LOG.info("attemptAuthentication()");

        // add extra logging
        Authentication attemptAuthentication;
        try {
            attemptAuthentication = super.attemptAuthentication(request, response);
        } catch (Exception ex) {
            LOG.error("Error running super.attemptAuthentication: {} in {} class", ex.getClass().getSimpleName(), this.getClass().getSimpleName());
            throw ex;
        }

        if (attemptAuthentication != null) {
            LOG.info("Found {}, trying to authenticate user", attemptAuthentication);
            Authentication authenticatedUser = authenticationProvider.authenticate(attemptAuthentication);
            LOG.info("User {}, now authenticated", authenticatedUser);

            return authenticatedUser;
        }

        return attemptAuthentication;
    }
}
