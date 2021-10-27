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

import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;

public class AzureJwtAccessTokenConverter extends JwtAccessTokenConverter {

    private static final String NBF = "nbf";
    private static final String NAME = "unique_name";

    private final JsonParser jsonParser = JsonParserFactory.create();

    @Override
    protected Map<String, Object> decode(String token) {
        try {
            AzureJwt jwt = new AzureJwt(JwtHelper.decode(token));

            String content = jwt.getClaims();

            Map<String, Object> map = jsonParser.parseMap(content);

            if (map.containsKey(AUD)) {
                map.put(CLIENT_ID, map.get(AUD));
            }

            if (map.containsKey(EXP)) {
                Object intValue = map.get(EXP);
                map.put(EXP, new Long(intValue.toString()));
            }

            if (map.containsKey(NBF)) {
                Object intValue = map.get(NBF);
                map.put(NBF, new Long(intValue.toString()));
            }

            if (map.containsKey(NAME)) {
                map.put(UserAuthenticationConverter.USERNAME, map.get(NAME));
            }

            return map;
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }

}
