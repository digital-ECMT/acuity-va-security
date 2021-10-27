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

package com.acuity.va.security.acl.dao;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.UsernameFullNameAndLinkedAccount;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 *
 * @author Glen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-all-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenQueryingUserRepository {

    @Autowired
    UserRepository userRepository;

    @Test
    public void shouldListAllUsersByRole_Users() {

        List<String> users = userRepository.listUsersByRole("ROLE_USER");

        assertThat(users).hasSize(4);
        assertThat(users).contains("kfgt12354", "ksdgh854", "user001", "ksdf5465667");
    }

    @Test
    public void subsequentSelectsShouldNotBeCached() {

        List<String> users1 = userRepository.listUsersByRole("ROLE_USER");

        assertThat(users1).hasSize(4);
        assertThat(users1).contains("kfgt12354", "ksdgh854", "user001", "ksdf5465667");

        List<String> users2 = userRepository.listUsersByRole("ROLE_USER");

        assertThat(users2).hasSize(4);
        assertThat(users2).contains("kfgt12354", "ksdgh854", "user001", "ksdf5465667");

        assertThat(users1).isNotSameAs(users2);
    }

    @Test
    public void shouldListNoUsersByInvalidRole() {

        List<String> users = userRepository.listUsersByRole("invalid");

        assertThat(users).isEmpty();
    }

    @Test
    public void shouldListAllUsers() {

        List<AcuitySidDetails> users = userRepository.listAllEnabledUsers();

        assertThat(users).hasSize(9);
        assertThat(users).extracting("userId").contains("user001");
        assertThat(users).extracting("fullName").contains("Glen D");
    }
    
    @Test
    public void shouldGetFullNameForUser() {

        String fullName = userRepository.getFullNameForUser("User6@securepass.mail.com");

        assertThat(fullName).isEqualTo("External User");
    }
    
    @Test
    public void shouldGetAllUsernameFullNameAndLinkedAccounts() {

        List<UsernameFullNameAndLinkedAccount> allusers = userRepository.getAllUsernameFullNameAndLinkedAccount();

        assertThat(allusers).hasSize(9);
        assertThat(allusers).extracting("username", "fullname", "linkedUsername").contains(
            tuple("User6@securepass.mail.com", "External User" ,"user001"),
            tuple("user001", "Ste D" , null)
        );
    }
}
