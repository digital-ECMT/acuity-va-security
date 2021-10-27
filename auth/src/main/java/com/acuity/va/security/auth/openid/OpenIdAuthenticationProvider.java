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

package com.acuity.va.security.auth.openid;

import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Clock;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Date;

import static com.google.api.client.auth.openidconnect.IdTokenVerifier.DEFAULT_TIME_SKEW_SECONDS;

@Service("openIdAuthenticationProvider")
public class OpenIdAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenIdAuthenticationProvider.class);

    private String requiredIssuer;
    private String requiredAudience;
    private String jsonWebKeySetUrl;
    private boolean verifySignature = false;

    public OpenIdAuthenticationProvider(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    public OpenIdAuthenticationProvider() {
    }

    public void setRequiredIssuer(String requiredIssuer) {
        this.requiredIssuer = requiredIssuer;
    }

    public void setRequiredAudience(String requiredAudience) {
        this.requiredAudience = requiredAudience;
    }

    public void setJsonWebKeySetUrl(String jsonWebKeySetUrl) {
        this.jsonWebKeySetUrl = jsonWebKeySetUrl;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        IdToken token = null;
        try {
            token = IdToken.parse(new JacksonFactory(), auth.getPrincipal().toString());
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid token " + token.toString(), ex);
        }

        IdTokenVerifier verifier = new IdTokenVerifier.Builder().setIssuer(requiredIssuer)
                .setAudience(Collections.singletonList(requiredAudience)).build();

        LOG.debug("Verifying with requiredAudience: " + requiredAudience + ", requiredIssuer: " + requiredIssuer);

        if (verifier.getIssuer() != null && !token.verifyIssuer(requiredIssuer)) {
            LOG.error("Issue isnt " + requiredIssuer + " for " + verifier.getIssuer());
        }
        if (verifier.getAudience() != null && !token.verifyAudience(Collections.singletonList(requiredAudience))) {
            LOG.error("Audience isnt one of " + requiredAudience + " for " + verifier.getAudience());
        }
        if (!token.verifyTime(Clock.SYSTEM.currentTimeMillis(), DEFAULT_TIME_SKEW_SECONDS)) {
            LOG.error("Time skew is invalid");
            LOG.error("Current time: " + Clock.SYSTEM.currentTimeMillis() + " "
                    + new Date(Clock.SYSTEM.currentTimeMillis()) + " SKEW " + DEFAULT_TIME_SKEW_SECONDS);
            LOG.error("ExpirationTimeSeconds: " + token.getPayload().getExpirationTimeSeconds() + " "
                    + new Date(token.getPayload().getExpirationTimeSeconds() * 1000));
            LOG.error("IssuedAtTimeSeconds: " + token.getPayload().getIssuedAtTimeSeconds() + " "
                    + new Date(token.getPayload().getIssuedAtTimeSeconds() * 1000));
        }

        boolean verify = verifier.verify(token);
        if (!verify) {
            throw new BadCredentialsException("Invalid " + token.toString());
        }
        if (verifySignature) {
            String jsonWebKeySetString = null;
            JsonWebKeySet jsonWebKeySet;

            try {
                jsonWebKeySetString = downloadJsonWebKeySet();
                // Create a new JsonWebKeySet object with the JWK Set JSON
                jsonWebKeySet = new JsonWebKeySet(jsonWebKeySetString);
            } catch (Exception ex) {
                throw new AuthenticationCredentialsNotFoundException("Unable to create JsonWebKeySet from " + jsonWebKeySetString, ex);
            }

            // The JWS header contains a Key ID, which  is a hint indicating which key
            // was used to secure the JWS. In this case (as will hopefully often be the case) the JWS Key ID
            // corresponds directly to   the Key ID in the JWK Set.
            // Find a JWK from the JWK Set that has the same Key ID, uses the same Key Type (EC)
            // and is designated to be used for signatures.
            JsonWebKey jwk = jsonWebKeySet.findJsonWebKey(token.getHeader().getKeyId(), "RSA", Use.SIGNATURE, null);

            if (jwk == null) {
                throw new AuthenticationCredentialsNotFoundException("Unable to find JsonWebKey "
                        + token.getHeader().getKeyId() + "from " + jsonWebKeySetString);
            }

            try {
                boolean verifiedSign = token.verifySignature((PublicKey) jwk.getKey());
                if (!verifiedSign) {
                    throw new BadCredentialsException("Invalidly signed token " + token.toString());
                }
            } catch (Exception ex) {
                throw new BadCredentialsException("Invalidly signed token " + token.toString(), ex);
            }
        }

        return new OpenIdPreAuthenticationToken(token);
    }

    private String downloadJsonWebKeySet() {
        try {
            return IOUtils.toString(new URL(jsonWebKeySetUrl).openStream());
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("Unable to download JsonWebKeySet from " + jsonWebKeySetUrl, ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (OpenIdPreAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
