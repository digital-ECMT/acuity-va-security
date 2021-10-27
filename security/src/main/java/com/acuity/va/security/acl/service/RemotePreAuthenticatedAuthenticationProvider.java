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

package com.acuity.va.security.acl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Implements the AuthenticationProvider so this can be used to authenticate a user using spring authentication-manager
 *
 * This sends the request off to a remote acuity server
 *
 * @author Glen
 */
@Service
public class RemotePreAuthenticatedAuthenticationProvider implements AuthenticationProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(RemotePreAuthenticatedAuthenticationProvider.class);

    @Autowired
    private AcuitySecurityResourceClient acuitySecurityResourceClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LOG.debug("authenticating user {}", authentication.getName());

        try {
            return acuitySecurityResourceClient.loadUserByUsername(authentication.getName());
        } catch (IllegalAccessException ex) {
            throw new AuthenticationServiceException("Error with the remote server", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
