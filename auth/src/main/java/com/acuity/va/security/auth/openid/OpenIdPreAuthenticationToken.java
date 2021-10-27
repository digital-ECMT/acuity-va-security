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

import com.google.api.client.auth.openidconnect.IdToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class OpenIdPreAuthenticationToken extends PreAuthenticatedAuthenticationToken {

    private IdToken token;

    public OpenIdPreAuthenticationToken(IdToken token) {
        super(token, null);
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return "password";
    }

    @Override
    public Object getPrincipal() {
        return token.getPayload().getSubject(); //prid
    }

    @Override
    public String getName() {
        return token.getPayload().getSubject();
    }
}
