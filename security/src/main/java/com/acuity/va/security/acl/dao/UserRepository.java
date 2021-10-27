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

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.UsernameFullNameAndLinkedAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRepository {

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    // Do not cache
    public List<String> listUsersByRole(String role) {
        return userRepositoryImpl.listUsersByRole(role);
    }

    // Do not cache
    public List<AcuitySidDetails> listAllEnabledUsers() {
        return userRepositoryImpl.listAllEnabledUsers();
    }

    @Cacheable(value = "UserRepository-getFullNameForUser", unless = "#result == null")
    public String getFullNameForUser(String userId) {
        return userRepositoryImpl.getFullNameForUser(userId);
    }

    // Do not cache    
    // Removed caching here, shouldnt affect performance too much
    //@Cacheable("UserRepository-getAllUsernameFullNameAndLinkedAccount")    
    public List<UsernameFullNameAndLinkedAccount> getAllUsernameFullNameAndLinkedAccount() {
        return userRepositoryImpl.getAllFullNameAndLinkedAccount();
    }

    //@CacheEvict(value = "UserRepository-getAllUsernameFullNameAndLinkedAccount")
    public void clearGetAllUsernameFullNameAndLinkedAccount() {
    }

    @CacheEvict(value = "UserRepository-getFullNameForUser", key = "#userId")
    public void clearGetFullNameForUserCache(String userId) {
    }
}
