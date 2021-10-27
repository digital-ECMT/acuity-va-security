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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

import java.io.IOException;
import java.util.LinkedHashMap;

public class AzureJwt implements Jwt {

    private final Jwt jwt;
    private final JwtHeader header;

    public AzureJwt(Jwt jwt) throws IOException {
        this.jwt = jwt;
        this.header = new JwtHeader(jwt);
    }

    public JwtHeader getHeader() {
        return header;
    }

    @Override
    public String getClaims() {
        return jwt.getClaims();
    }

    @Override
    public String getEncoded() {
        return jwt.getEncoded();
    }

    @Override
    public void verifySignature(SignatureVerifier verifier) {
        jwt.verifySignature(verifier);
    }

    @Override
    public byte[] bytes() {
        return jwt.bytes();
    }

    public static final class JwtHeader {

        private final LinkedHashMap<String, String> value;

        private JwtHeader(Jwt jwt) throws IOException {
            String json = jwt.toString();
            value = getPropertiesMap(json);
        }

        private LinkedHashMap<String, String> getPropertiesMap(String json) throws IOException {
            return new ObjectMapper().readValue(json, new TypeReference<LinkedHashMap<String, String>>() {
            });
        }

        public String getTyp() {

            return value.get("typ");
        }

        public String getAlg() {

            return value.get("alg");
        }

        public String getKid() {

            return value.get("kid");
        }
    }
}
