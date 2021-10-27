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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple filter that checks if the users session has timed out.
 * <p>
 * If it has, it sends back a 419 error
 *
 * @author Glen
 *
 * This service is deprecated, use AjaxInvalidSessionStrategy instead
 */
@Deprecated
public class SessionTimedOutFilter implements Filter, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(SessionTimedOutFilter.class);

    private Environment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!isDevProfile()) {
            LOG.debug("Checking session to see if its timed out");

            if (isSessionTimedOut(request) && isAjaxRequest(request)) {
                LOG.debug("Session timed out for request" + RequestPrinter.debugString(request, true));
                response.sendError(419, "Ajax Access Denied"); // time out ajax request, send back 419
                return;
            }
        }

        chain.doFilter(request, response);
    }

    public boolean isAjaxRequest(HttpServletRequest hsr) {
        String header = hsr.getHeader("X-Requested-With");
        return header != null && header.equals("XMLHttpRequest");
    }

    protected boolean isSessionTimedOut(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();
        return sessionId != null && !request.isRequestedSessionIdValid();
    }

    protected boolean isDevProfile() {
        return environment != null && environment.acceptsProfiles("dev");
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
