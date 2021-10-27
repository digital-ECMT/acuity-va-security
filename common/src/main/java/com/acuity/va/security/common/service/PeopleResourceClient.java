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
import com.acuity.va.security.auth.azure.graph.MicrosoftGraphClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PeopleResourceClient {

    private static final Logger LOG = LoggerFactory.getLogger(PeopleResourceClient.class);

    @Autowired(required = false)
    private MicrosoftGraphClient microsoftGraphClient;

    /**
     * Searches the Azure Active Directory for a user's display name
     *
     * @param userId - id of the user (email or prid)
     * @return userIds display name
     */
    public String getFullName(String userId) {

        if (userId == null) {
            return null;
        }
        LOG.debug("Looking up fullname for {}", userId);

        if (microsoftGraphClient == null) {
            throw new RuntimeException("Azure profile is not enabled, unable to search users");
        }

        return microsoftGraphClient.getUserInfoByUserId(userId)
                .stream().findFirst().map(ActiveDirectoryRecord::getDisplayName)
                .orElseThrow(() -> new NoSuchElementException("Unknown user id: " + userId));
    }

    /**
     * Searches the Azure Active Directory for users matching the name passed in
     *
     * @param name - user's name/surname/display name/email address initial letters to search for
     *             (case insensitive)
     * @return list of users
     */
    public List<ActiveDirectoryRecord> searchUsersByName(String name) {
        LOG.debug("Looking up details for name {}", name);

        if (microsoftGraphClient == null) {
            LOG.debug("Azure profile is not enabled, unable to search users");
            return Collections.emptyList();
        }

        return microsoftGraphClient.getUserInfoByName(name);
    }
}
