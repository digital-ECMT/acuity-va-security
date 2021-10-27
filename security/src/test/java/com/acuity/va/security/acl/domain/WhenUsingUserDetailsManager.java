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

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.service.CustomUserDetailsManager;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-gbac-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingUserDetailsManager {

    @Autowired
    private CustomUserDetailsManager customUserDetailsManager;

    @Test
    public void shouldCheckIfUserIsInDB() {

        boolean exists = customUserDetailsManager.userExists("user001");
        assertThat(exists).isTrue();

        exists = customUserDetailsManager.userExists("sdsd");
        assertThat(exists).isFalse();
    }

    @Test
    public void shouldAddUserWithCorrectAuthorities() {

        String USERID = "test";
        String FULLNAME = "test name";

        customUserDetailsManager.createUser(USERID, FULLNAME, newArrayList(new SimpleGrantedAuthority("TEST")));

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly("TEST");
    }

    @Test
    public void shouldFindAllGroups() {

        List<String> findAllGroups = customUserDetailsManager.findAllGroups();

        assertThat(findAllGroups).hasSize(3);
        assertThat(findAllGroups).contains("Administrators", "Users", "ACL_ADMINISTRATOR_GROUP");
    }

    @Test
    public void shouldFindUsersInGroups() {

        List<String> findUsersInGroup = customUserDetailsManager.findUsersInGroup("Users");

        assertThat(findUsersInGroup).hasSize(2);
        assertThat(findUsersInGroup).contains("kfgt12354", "ksdgh854");
    }

    @Test
    public void shouldloadUserByUsernameThatIsEnabled() {

        String USERID = "user001";

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getUsername()).isEqualTo(USERID);
        assertThat(user).isInstanceOf(AcuitySidDetails.class);
        assertThat(((AcuitySidDetails) user).getFullName()).isEqualTo("Glen D");
        assertThat(user.getAuthorities()).extracting("authority").containsOnly("ROLE_USER", "ROLE_USER_SINGLE", "ROLE_ADMIN");
    }

    @Test
    public void shouldRemoveUserFromGroup() {

        customUserDetailsManager.removeUserFromGroup("kfgt12354", "Users");

        assertThat(customUserDetailsManager.findUsersInGroup("Users")).hasSize(1);
    }

    @Test
    public void shouldDeleteUser() {

        customUserDetailsManager.deleteUser("kfgt12354");

        assertThat(customUserDetailsManager.userExists("kfgt12354")).isFalse();
    }
}
