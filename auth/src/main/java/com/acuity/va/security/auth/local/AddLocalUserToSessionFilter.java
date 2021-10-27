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

package com.acuity.va.security.auth.local;

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.SystemUtils.USER_NAME;

/**
 * Adds local user as the authenticated user spring session.
 *
 * This is for dev
 *
 * @author Glen
 */
public class AddLocalUserToSessionFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AddLocalUserToSessionFilter.class);

    private final List<String> defaultAuthorities = newArrayList("TRAINED_USER", "DEVELOPMENT_TEAM", "ACL_ADMINISTRATOR",
            "ROLE_TRAINED_USER", "ROLE_DEVELOPMENT_TEAM", "ROLE_ACL_ADMINISTRATOR",  "ROLE_ACTUATOR");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String lowerUsername = USER_NAME.toLowerCase();

        LOG.info("doFilter: added {} as logged on user with authorities {}", lowerUsername, defaultAuthorities);

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        List<SimpleGrantedAuthority> defaultSimpleGrantedAuthorities = defaultAuthorities.stream().map(SimpleGrantedAuthority::new).collect(toList());

        AcuitySidDetails user = new AcuitySidDetails(lowerUsername, lowerUsername, defaultSimpleGrantedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(user);

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
