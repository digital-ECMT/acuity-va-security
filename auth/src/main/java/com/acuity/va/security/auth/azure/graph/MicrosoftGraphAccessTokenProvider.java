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

package com.acuity.va.security.auth.azure.graph;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Profile("azure-sso")
@Service
public class MicrosoftGraphAccessTokenProvider {

    @Value("${azure.client.clientId:}")
    private String clientId;

    @Value("${azure.client.clientSecret:}")
    private String clientSecret;

    @Value("${azure.authorityUri:}")
    private String authorityUri;

    @Value("${azure.graphUri:}")
    private String graphUri;

    /**
     * Acquire access token for microsoft graph v.1.0 API using client credentials flow
     * @return access token
     */
    public String getAccessToken() throws MalformedURLException, ServiceUnavailableException, ExecutionException, InterruptedException {

        AuthenticationContext context;
        AuthenticationResult result;
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            context = new AuthenticationContext(authorityUri, true, service);

            ClientCredential clientCredential = new ClientCredential(clientId, clientSecret);
            Future<AuthenticationResult> future = context.acquireToken(
                    graphUri, clientCredential, null);
            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException(
                    "Authentication result was null");
        }
        return result.getAccessToken();
    }
}
