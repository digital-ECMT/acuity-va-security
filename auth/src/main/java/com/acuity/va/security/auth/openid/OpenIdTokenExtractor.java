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

package com.acuity.va.security.auth.openid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * {@link TokenExtractor} that strips the authenticator from a bearer token request (with an Authorization header in the form "OpenId" <code><TOKEN></code>", or
 * as a request parameter if that fails). The access token is the principal in the authentication token that is extracted.
 *
 */
public class OpenIdTokenExtractor implements TokenExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenIdTokenExtractor.class);
    public static final List<String> OPENID_TOKENS = Arrays.asList("id_token", "OpenID");

    @Override
    public Authentication extract(HttpServletRequest request) {
        String tokenValue = extractToken(request);
        if (tokenValue != null) {
            PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(tokenValue, "");
            return authentication;
        }
        return null;
    }

    protected String extractToken(HttpServletRequest request) {
        // first check the header...
        String token = extractHeaderToken(request);

        // bearer type allows a request parameter as well
        if (token == null) {
            LOG.debug("Token not found in Authorization headers. Trying request headers/parameters: " + OPENID_TOKENS);

            for (String openidToken : OPENID_TOKENS) {
                token = request.getParameter(openidToken);
                if (token != null) {
                    LOG.debug("Found token for parameter " + openidToken);
                    return token;
                }
                token = request.getHeader(openidToken);
                if (token != null) {
                    LOG.debug("Found token for header " + openidToken);
                    return token;
                }
            }
            LOG.debug("OpenId Token not found in request headers/parameters.  Not an OAuth2 request.");
        }

        return token;
    }

    /**
     * Extract the OpenId bearer token from a header.
     *
     * @param request The request.
     * @return The token, or null if no OpenId authorization header was supplied.
     */
    protected String extractHeaderToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders("Authorization");
        while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
            String value = headers.nextElement();
            if ((value.toLowerCase().startsWith(OPENID_TOKENS.get(0).toLowerCase()))) {
                String authHeaderValue = value.substring(OPENID_TOKENS.get(0).length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return authHeaderValue;
            }
        }

        return null;
    }
}
