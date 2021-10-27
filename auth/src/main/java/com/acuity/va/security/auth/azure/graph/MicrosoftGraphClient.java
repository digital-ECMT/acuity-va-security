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

import com.acuity.va.security.acl.domain.ActiveDirectoryRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

@Profile("azure-sso")
@Service
public class MicrosoftGraphClient {

    private static final String BASE_URL = "https://graph.microsoft.com/v1.0/%s";

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftGraphClient.class);
    private static final String PARAMETERS = "parameters";

    private RestOperations restTemplate = new RestTemplate();

    @Autowired
    private MicrosoftGraphAccessTokenProvider microsoftGraphAccessTokenProvider;

    /**
     * Searches the Azure Active Directory for a users by email or principal name
     *
     * @param userId - id of the user (email or prid)
     * @return graph response parsed to list of {@link ActiveDirectoryRecord}
     */
    public List<ActiveDirectoryRecord> getUserInfoByUserId(String userId) {

        String query = String.format("users?$filter=startswith(mail,'%s') "
                + "or startswith(userPrincipalName,'%<s')", userId);

        return queryMicrosoftGraphForUsers(query);
    }

    /**
     * Searches the Azure Active Directory for users matching the name passed in
     *
     * @param name - user's name/surname/display name/email address initial letters to search for
     *             (case insensitive)
     * @return graph response parsed to list of {@link ActiveDirectoryRecord}
     */
    public List<ActiveDirectoryRecord> getUserInfoByName(String name) {

        String query = String.format("users?$filter=startswith(givenName,'%s') "
                + "or startswith(displayName,'%<s') "
                + "or startswith(surname,'%<s') "
                + "or startswith(mail,'%<s') "
                + "or startswith(userPrincipalName,'%<s')", name);

        return queryMicrosoftGraphForUsers(query);
    }

    /**
     * Query Microsoft Graph v.1.0 users endpoint
     * @param query - string to query Microsoft Graph v.1.0 endpoint (base URL part omitted).
     *              To query users, the string must start with "users/"
     *              OData request parameters are supported.
     *              See https://docs.microsoft.com/en-us/graph/api/overview?view=graph-rest-1.0 for details
     * @return graph response parsed to list of {@link ActiveDirectoryRecord}
     */
    private List<ActiveDirectoryRecord> queryMicrosoftGraphForUsers(String query) {
        String accessToken;
        try {
            accessToken = microsoftGraphAccessTokenProvider.getAccessToken();
        } catch (ServiceUnavailableException | ExecutionException
                | InterruptedException | MalformedURLException e) {
            LOG.warn("Unable to get access token. {}", e.getMessage());
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, String.format("%s %s", BEARER_TYPE, accessToken));
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(PARAMETERS, headers);
        ResponseEntity<String> result = restTemplate.exchange(String.format(BASE_URL, query), GET,
                entity, String.class);
        return parseActiveDirectoryRecordsFromResponse(result);
    }

    private List<ActiveDirectoryRecord> parseActiveDirectoryRecordsFromResponse(ResponseEntity<String> result) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            final JsonNode arrNode = objectMapper.readTree(result.getBody()).get("value");
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ActiveDirectoryRecord>>() {
            });
            return reader.readValue(arrNode);
        } catch (IOException e) {
            LOG.warn("Unable to get list of users. {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
