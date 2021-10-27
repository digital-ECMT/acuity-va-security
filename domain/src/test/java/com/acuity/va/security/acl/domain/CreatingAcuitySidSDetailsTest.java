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

import org.junit.Test;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;

import static com.acuity.va.security.common.Constants.DEVELOPMENT_GROUP;
import static com.acuity.va.security.common.Constants.DEVELOPMENT_TEAM_AUTHORITY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author glen
 */
public class CreatingAcuitySidSDetailsTest {

    @Test
    public void shouldCreateAcuitySidDetailsForUser() {
        String USER1 = "User1";
        AcuitySidDetails user = AcuitySidDetails.toUser(USER1);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(USER1);
        assertThat(user.getUsername()).isEqualTo(USER1);
        assertThat(user.getUserId()).isEqualTo(USER1);
        assertThat(user.toSid()).isEqualTo(new PrincipalSid(USER1));
        assertThat(user.toSids()).contains(new PrincipalSid(USER1));
    }

    @Test
    public void shouldCreateAcuitySidDetailsForUser2() {
        String USER1 = "User1";
        AcuitySidDetails user = new AcuitySidDetails(USER1);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(USER1);
        assertThat(user.getUsername()).isEqualTo(USER1);
        assertThat(user.getUserId()).isEqualTo(USER1);
        assertThat(user.toSid()).isEqualTo(new PrincipalSid(USER1));
        assertThat(user.toSids()).contains(new PrincipalSid(USER1));
    }

    @Test
    public void shouldCreateAcuitySidDetailsForGroup() {
        String GROUP1 = "GROUP1";
        String GROUP1_AUTHORITY = "GROUP1_AUTHORITY";
        AcuitySidDetails user = AcuitySidDetails.toUserFromGroup(GROUP1);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(GROUP1);
        assertThat(user.getUsername()).isEqualTo(GROUP1);
        assertThat(user.getUserId()).isEqualTo(GROUP1);
        assertThat(user.toSid()).isEqualTo(new GrantedAuthoritySid(GROUP1_AUTHORITY));
        assertThat(user.toSids()).contains(new GrantedAuthoritySid(GROUP1_AUTHORITY));
        assertThat(user.getAuthoritiesAsString()).contains(GROUP1_AUTHORITY);
    }
    
    @Test
    public void shouldCreateAcuitySidDetailsForGroupAuthority() {
        String GROUP1 = "GROUP1";
        String GROUP1_AUTHORITY = "GROUP1_AUTHORITY";
        AcuitySidDetails user = AcuitySidDetails.toUserFromAuthority(GROUP1_AUTHORITY);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(GROUP1);
        assertThat(user.getUsername()).isEqualTo(GROUP1);
        assertThat(user.getUserId()).isEqualTo(GROUP1);
        assertThat(user.toSid()).isEqualTo(new GrantedAuthoritySid(GROUP1_AUTHORITY));
        assertThat(user.toSids()).contains(new GrantedAuthoritySid(GROUP1_AUTHORITY));
        assertThat(user.getAuthoritiesAsString()).contains(GROUP1_AUTHORITY);
    }
    
    @Test
    public void shouldCreateAcuitySidDetailsForDevGroup() {
        AcuitySidDetails user = AcuitySidDetails.toUserFromGroup(DEVELOPMENT_GROUP);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.getUsername()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.getUserId()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.toSid()).isEqualTo(new GrantedAuthoritySid(DEVELOPMENT_TEAM_AUTHORITY));
        assertThat(user.toSids()).contains(new GrantedAuthoritySid(DEVELOPMENT_TEAM_AUTHORITY));
        assertThat(user.getAuthoritiesAsString()).contains(DEVELOPMENT_TEAM_AUTHORITY);
    }
    
    @Test
    public void shouldCreateAcuitySidDetailsForDevAuthority() {
        AcuitySidDetails user = AcuitySidDetails.toUserFromAuthority(DEVELOPMENT_TEAM_AUTHORITY);

        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(user.getSidAsString()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.getUsername()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.getUserId()).isEqualTo(DEVELOPMENT_GROUP);
        assertThat(user.toSid()).isEqualTo(new GrantedAuthoritySid(DEVELOPMENT_TEAM_AUTHORITY));
        assertThat(user.toSids()).contains(new GrantedAuthoritySid(DEVELOPMENT_TEAM_AUTHORITY));
        assertThat(user.getAuthoritiesAsString()).contains(DEVELOPMENT_TEAM_AUTHORITY);
    }
}
