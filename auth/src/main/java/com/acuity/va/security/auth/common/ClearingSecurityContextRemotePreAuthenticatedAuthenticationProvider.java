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

import com.acuity.va.security.auth.remote.RemotePreAuthenticatedAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


/**
 * Extends RemotePreAuthenticatedAuthenticationProvider to clear and add the security context after authenticating

* @author Glen
 */
@Service("clearingAuthenticationProvider")
public class ClearingSecurityContextRemotePreAuthenticatedAuthenticationProvider extends RemotePreAuthenticatedAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClearingSecurityContextRemotePreAuthenticatedAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LOG.debug("authenticating user {}", authentication.getName());

        Authentication authenticatedUser = super.authenticate(authentication);

        LOG.debug("clearing context and adding {} to context", authenticatedUser.getName());
        // now clear the context set by OAuth2ClientAuthenticationProcessingFilter and set it as AcuitySidDetails
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        
        return authenticatedUser;
    }
}
