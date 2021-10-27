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

package com.acuity.va.security.auth.azure;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class AzureAccessToken extends DefaultOAuth2AccessToken {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Date refreshTokenExpiration;
    private Date notBefore;
    private AzureProfile profile;

    public AzureAccessToken(OAuth2AccessToken accessToken) throws IOException {

        super(accessToken);

        DefaultOAuth2AccessToken token;

        if (accessToken instanceof DefaultOAuth2AccessToken) {
            token = (DefaultOAuth2AccessToken) accessToken;
        } else {
            token = new DefaultOAuth2AccessToken(accessToken);
        }

        parseTokenInfo(token);
    }

    private void parseTokenInfo(DefaultOAuth2AccessToken accessToken) throws IOException {
        long now = System.currentTimeMillis();
        Map<String, Object> info = accessToken.getAdditionalInformation();

        if (info != null && info.size() > 0) {

            String idToken = (String) info.get("id_token");

            if (idToken != null) {
                this.setValue(idToken);
            }

            if (this.getExpiration() == null) {
                String expiresInStr = (String) info.get("id_token_expires_in");

                if (expiresInStr != null) {
                    long seconds = Long.parseLong(expiresInStr);
                    long milliSeconds = seconds * 1000;
                    Date expiration = new Date(now + milliSeconds);
                    accessToken.setExpiration(expiration);
                }
            }

            String refreshExpiresInStr = (String) info.get("refresh_token_expires_in");

            if (refreshExpiresInStr != null) {
                long seconds = Long.parseLong(refreshExpiresInStr);
                long milliSeconds = seconds * 1000;
                this.refreshTokenExpiration = new Date(now + milliSeconds);
            }

            String notBeforeStr = (String) info.get("not_before");

            if (notBeforeStr != null) {
                long seconds = Long.parseLong(notBeforeStr);
                long milliSeconds = seconds * 1000;
                this.notBefore = new Date(now + milliSeconds);
            }

            String profile64Encoded = (String) info.get("profile_info");

            if (profile64Encoded != null) {
                byte[] profileBytes = Base64.decode(profile64Encoded.getBytes());

                try {
                    this.profile = OBJECT_MAPPER.readValue(profileBytes, AzureProfile.class);
                } catch (JsonParseException e) {
                    this.profile = null;
                }
            }
        }
    }

    public Date getRefreshTokenExpiration() {

        return refreshTokenExpiration;
    }

    public AzureProfile getProfile() {

        return profile;
    }

    public Date getNotBefore() {

        return notBefore;
    }
}
