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

package com.acuity.va.security.acl.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an active directory record/user from the Azure AD people service
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = "prid")
public class ActiveDirectoryRecord {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveDirectoryRecord.class);
    public static final String EXT = "#EXT#";

    private String prid;
    private String givenName;
    private String surname;
    private String displayName;
    private String mail;

    @JsonProperty("cn")
    public String getPrid() {
        return prid;
    }

    @JsonProperty("userPrincipalName")
    public void setPrid(String prid) {
        this.prid = prid.contains(EXT) ? mail : prid;
    }

    public ActiveDirectoryRecord(String prid, String givenName, String surname, String displayName, String mail) {
        this.givenName = givenName;
        this.surname = surname;
        this.displayName = displayName;
        this.mail = mail;
        setPrid(prid);
    }
}
