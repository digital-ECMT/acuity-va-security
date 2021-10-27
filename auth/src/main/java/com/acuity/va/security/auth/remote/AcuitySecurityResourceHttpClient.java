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

package com.acuity.va.security.auth.remote;

import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DatasetsRequest;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.auth.common.ISecurityResourceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.acuity.va.security.common.Constants.ACTUATOR_ENV_KEY;

/**
 * Interacts with the remove acuity security web services
 *
 * @author Glen
 */
@Service
@Profile("!local-no-security")
public class AcuitySecurityResourceHttpClient implements ISecurityResourceClient {

    private static final Logger LOG = LoggerFactory.getLogger(AcuitySecurityResourceHttpClient.class);

    @Value("${acuity.vasecurity.url}")
    private String url;
    @Value("${acuity.vasecurity.username}")
    private String username;
    @Value("${acuity.vasecurity.password}")
    private String password;
    @Value("${env.name:}")
    private String thisEnvProfile;

    private static final String ENV_PROPERTIES_PATH = "info";
    private static final String PATH = "resources/security";

    private HttpClientContext context;
    private CloseableHttpClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void addBasicAuth() {
        LOG.debug("Adding basic auth details.  username: {}, url: {}", username, url);
        HttpHost targetHost = new HttpHost(url);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        client = HttpClients.createDefault();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Loads a AcuityUserDetails from the userId
     *
     * @param userId userId to load
     * @return AcuityUserDetails object of name, fullname and authorities
     */
    @Override
    public AcuitySidDetails loadUserByUsername(String userId) throws IllegalAccessException, IOException {
        LOG.debug("Looking up userId {} details", userId);

        final String resultAsString = executeHttpGet(url + "/" + PATH + "/loadUserByUsername/" + userId, userId);
        return objectMapper.readValue(resultAsString,
                AcuitySidDetails.class);
    }

    /**
     * Gets all the AcuityObjectIdentityWithPermission for the user
     *
     * @param userId userId to get permissions
     * @return List<AcuityObjectIdentityWithPermission> that the user has access to
     */
    @Override
    @SneakyThrows
    public List<AcuityObjectIdentityWithPermission> getAclsForUser(String userId) {
        LOG.debug("Get AcuityObjectIdentityWithPermissions for userId {}", userId);

        final String resultAsString = executeHttpGet(String.format("%s/%s/acls/%s", url, PATH, userId), userId);
        return objectMapper.readValue(resultAsString,
                new TypeReference<List<AcuityObjectIdentityWithPermission>>() {
                });
    }

    /**
     * Checks if the user has permissionMask for the acuityObjectIdentity
     *
     * @param userId         prid of the user
     * @param datasets       list of datasets
     * @param permissionMask permissionMask of the permission to check for, ie 32 is ViewDataset
     * @return boolean has permission or not
     */
    @Cacheable("AcuitySecurityResourceClient-hasPermissionForUser")
    @Override
    public boolean hasPermissionForUser(String userId, List<Dataset> datasets, Integer permissionMask) {
        LOG.debug("Checking hasPermissionForUser for user {} for {} with permission {}", userId, datasets, permissionMask);
        String url = this.url + "/" + PATH + "/acl/haspermission/" + permissionMask + "/" + userId;
        LOG.debug("URL " + url);

        try {
            HttpPost httpPost = preparePostRequest(url, datasets);
            int statusCode;
            try (CloseableHttpResponse response = client.execute(httpPost, context)) {
                statusCode = checkResponse(response, userId);
            }
            return statusCode == HttpStatus.ACCEPTED.value() || statusCode == HttpStatus.OK.value();
        } catch (Exception ex) {
            LOG.error("Exception calling hasPermissionForUser", ex);
            return false;
        }
    }

    /**
     * Gets a list of users for a dataset
     */
    @Override
    public List<AcuitySidDetails> getUsersForDataset(Dataset dataset) throws IllegalAccessException, IOException {
        LOG.debug("Get getUserForDataset for dataset {}", dataset);

        final String resultAsString = executeHttpGet(url + "/" + PATH + "/acl/" + dataset.getClass()
                .getSimpleName() + "/" + dataset.getId(), null);
        return objectMapper.readValue(resultAsString, new TypeReference<List<AcuitySidDetails>>() {
        });
    }


    /**
     * Gets a list of users for the datasets
     */
    @Override
    public List<AcuitySidDetails> getUsersForDatasets(List<Dataset> datasets) throws IOException, IllegalAccessException {
        LOG.debug("Get getUsersForDatasets for datasets {}", datasets);

        HttpPost httpPost = preparePostRequest(url + "/" + PATH + "/acl/datasets/listpermissions", datasets);
        return objectMapper.readValue(getResponseAsString(httpPost, null), new TypeReference<List<AcuitySidDetails>>() {
        });
    }

    private HttpPost preparePostRequest(String url, List<Dataset> datasets) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        DatasetsRequest datasetsRequest = new DatasetsRequest(datasets);
        StringEntity input = new StringEntity(objectMapper.writeValueAsString(datasetsRequest));
        input.setContentType(ContentType.APPLICATION_JSON.toString());
        httpPost.setEntity(input);
        return httpPost;
    }

    public void checkThisAppEnvProfileMatchesVaSecurityEnvProfile() throws IOException, IllegalAccessException {
        String vasecProfile;
        String toString = executeHttpGet((String.format("%s/%s", this.url, ENV_PROPERTIES_PATH)), null);
        try {
            vasecProfile = (String) JsonUtil.parseJson(toString).get(ACTUATOR_ENV_KEY);
        } catch (JoseException e) {
            throw new RuntimeException("Can't receive vasecurity's profile information");
        }
        if (!vasecProfile.equals(thisEnvProfile)) {
            throw new IllegalStateException(String.format("Vasecurity running with %s profile"
                    + " while you're attempting to run app in %s profile", vasecProfile, thisEnvProfile));
        }
    }

    private String executeHttpGet(String url, String userId) throws IOException, IllegalAccessException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.displayName());
        return getResponseAsString(httpGet, userId);
    }

    private String getResponseAsString(HttpRequestBase http, String userId) throws IOException, IllegalAccessException {
        CloseableHttpResponse response = null;
        String result;
        try {
            response = client.execute(http, context);
            checkResponse(response, userId);
            result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
                response.close();
            }
        }
        return result;
    }

    private int checkResponse(HttpResponse response, String userId)
            throws IllegalAccessException, UsernameNotFoundException, IllegalAccessError, IOException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 401) { // invalid username or password
            String responseAsString = EntityUtils.toString(response.getEntity());
            LOG.error("Username and/or password incorrect for remote server basic auth. Response {}", responseAsString);
            throw new IllegalAccessException("Username and/or password incorrect for remote server basic auth");
        } else if (statusCode == 404) { // user not found
            String responseAsString = EntityUtils.toString(response.getEntity());
            LOG.error("Username " + userId + " not found. Response {}", responseAsString);
            throw new UsernameNotFoundException("Username " + userId + " not found");
        } else if (statusCode == 406) { // no permission
            String responseAsString = EntityUtils.toString(response.getEntity());
            LOG.error("Username " + userId + " doesnt have permission. Response {}", responseAsString);
            return statusCode;
        } else if (statusCode >= 400) {
            String responseAsString = EntityUtils.toString(response.getEntity());
            LOG.error("Other error from server, code  {}. Response {}", statusCode, responseAsString);
            throw new IllegalStateException("Other error from server, code " + statusCode);
        }

        return statusCode;
    }
}
