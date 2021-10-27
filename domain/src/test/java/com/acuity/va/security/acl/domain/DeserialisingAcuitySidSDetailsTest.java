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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.acls.domain.PrincipalSid;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author glen
 */
public class DeserialisingAcuitySidSDetailsTest {

    private static ObjectMapper mapper;

    @Before
    public void beforeClass() {
        mapper = new ObjectMapper();
    }

    @Test
    public void shouldCreateAcuitySidDetails() throws IOException {
        String json = "{\"sidAsString\":\"Glen\"}";

        AcuitySidDetails readValue = mapper.readValue(json, AcuitySidDetails.class);

        assertThat(readValue).isInstanceOf(AcuitySidDetails.class);
        assertThat(readValue.getSidAsString()).isEqualTo("Glen");        
        assertThat(readValue.toSids()).contains(new PrincipalSid("Glen"));        
    }
    
    @Test
    public void shouldCreateAcuitySidDetailsForGroup() throws IOException {
        AcuitySidDetails toUserFromGroup = AcuitySidDetails.toUserFromGroup("Group1");
        String json = "{\"sidAsString\":\"Group1\", \"group\":true}";

        AcuitySidDetails readValue = mapper.readValue(json, AcuitySidDetails.class);

        assertThat(readValue).isInstanceOf(AcuitySidDetails.class);
        assertThat(readValue.getSidAsString()).isEqualTo(toUserFromGroup.getSidAsString()).isEqualTo("Group1");        
        assertThat(readValue.toSids()).isEqualTo(toUserFromGroup.toSids());        
    }
    
    @Test
    public void shouldSerialiseAcuitySidDetailsForGroup() throws IOException {
        AcuitySidDetails toUserFromGroup = AcuitySidDetails.toUserFromGroup("Group1");

        String wroteValue = mapper.writeValueAsString(toUserFromGroup);

        String sidAsString = JsonPath.read(wroteValue, "$.sidAsString");
        String fullName = JsonPath.read(wroteValue, "$.fullName");
        String userId = JsonPath.read(wroteValue, "$.userId");
        assertThat(sidAsString).isEqualTo(fullName).isEqualTo(userId).isEqualTo("Group1");
        Boolean group = JsonPath.read(wroteValue, "$.group");
        assertThat(group).isTrue();
        List<String> authorities = JsonPath.read(wroteValue, "$.authoritiesAsString");
        assertThat(authorities).containsOnly("Group1_AUTHORITY");
    }
    
    @Test
    public void shouldSerialiseAcuitySidDetailsForUser() throws IOException {
        AcuitySidDetails toUser = AcuitySidDetails.toUser("Glen");

        String wroteValue = mapper.writeValueAsString(toUser);

        String sidAsString = JsonPath.read(wroteValue, "$.sidAsString");
        String fullName = JsonPath.read(wroteValue, "$.fullName");
        String userId = JsonPath.read(wroteValue, "$.userId");
        assertThat(sidAsString).isEqualTo(fullName).isEqualTo(userId).isEqualTo("Glen");
        Boolean group = JsonPath.read(wroteValue, "$.group");
        assertThat(group).isFalse();
        List<String> authorities = JsonPath.read(wroteValue, "$.authoritiesAsString");
        assertThat(authorities).isEmpty();
    }
}
