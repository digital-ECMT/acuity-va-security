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

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Combines both the Access token and OpenId token
 *
 */
public class OAuth2OpenIdPreAuthenticationToken extends PreAuthenticatedAuthenticationToken {

    private final Authentication openIdTokenAuthentication;
    private final Authentication oauth2TokenAuthentication;

    public OAuth2OpenIdPreAuthenticationToken(Authentication openIdTokenAuthentication, Authentication oauth2TokenAuthentication) {
        super(openIdTokenAuthentication, oauth2TokenAuthentication);
        this.openIdTokenAuthentication = openIdTokenAuthentication;
        this.oauth2TokenAuthentication = oauth2TokenAuthentication;
    }

    public Authentication getOpenIdTokenAuthentication() {
        return openIdTokenAuthentication;
    }

    public Authentication getAccessTokenAuthentication() {
        return oauth2TokenAuthentication;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return openIdTokenAuthentication.getPrincipal();
    }

    @Override
    public String getName() {
        return openIdTokenAuthentication.getName();
    }
}
