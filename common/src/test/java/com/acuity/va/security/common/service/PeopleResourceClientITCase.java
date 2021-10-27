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

package com.acuity.va.security.common.service;

import com.acuity.va.security.acl.domain.ActiveDirectoryRecord;
import com.acuity.va.security.common.config.AzureActiveProfilesResolver;
import com.acuity.va.security.common.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@ActiveProfiles(resolver = AzureActiveProfilesResolver.class)
public class PeopleResourceClientITCase {

    private static final Logger LOG = LoggerFactory.getLogger(PeopleResourceClientITCase.class);

    @Autowired
    private PeopleResourceClient peopleResourceClient;

    @Test
    public void shouldGetFullName() {
        String fullName = peopleResourceClient.getFullName("yourOrg_user-name@domain");
        assertThat(fullName).isEqualTo("yourOrg_user-name");
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldntGetUsersFullname() {
        peopleResourceClient.getFullName("someNotExistingId");
    }

    @Test
    public void shouldSearchUsersByFirstLettersOfDisplayName() {
        List<ActiveDirectoryRecord> adrs = peopleResourceClient.searchUsersByName("yourOrg");
        LOG.debug("Found records {}", adrs);
        assertThat(adrs.size()).isEqualTo(1);
        assertAllNotNullAtts(adrs);
    }

    private void assertAllNotNullAtts(List<ActiveDirectoryRecord> adrs) {
        assertThat(adrs).extracting("prid").isNotNull();
        assertThat(adrs).extracting("displayName").isNotNull();
        assertThat(adrs).extracting("mail").isNotNull();
    }
}
