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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Invalid session strategy, that sends back a 419 error for ajax requests
 * and a redirect to the home page for other requests.
 * Ajax request must be preconfigured with the following header: 'X-Requested-With'='XMLHttpRequest'
 */
public class AjaxInvalidSessionStrategy implements InvalidSessionStrategy {
    private final Log logger = LogFactory.getLog(getClass());
    private final String destinationUrl;
    private final boolean createNewSession;

    public AjaxInvalidSessionStrategy(String invalidSessionUrl, boolean createNewSession) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(invalidSessionUrl),
                "url must start with '/' or with 'http(s)'");
        this.destinationUrl = invalidSessionUrl;
        this.createNewSession = createNewSession;
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        logger.debug("Invalid session detected. Starting new session (if required) and redirecting to "
                + destinationUrl + "'");
        if (createNewSession) {
            request.getSession();
        }
        if (isAjaxRequest(request)) {
            response.sendError(419, "Ajax Access Denied"); // time out ajax request, send back 419
            return;
        }
        response.sendRedirect(destinationUrl);
    }

    private boolean isAjaxRequest(HttpServletRequest hsr) {
        String header = hsr.getHeader("X-Requested-With");
        return header != null && header.equals("XMLHttpRequest");
    }
}
