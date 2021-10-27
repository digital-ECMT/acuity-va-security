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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.acuity.va.security.auth.common;

import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Glen
 */
public interface ISecurityResourceClient {

    /**
     * Gets all the AcuityObjectIdentityWithPermission for the user
     *
     * @param userId userId to get permissions
     * @return List<AcuityObjectIdentityWithPermission> that the user has access to
     */
    List<AcuityObjectIdentityWithPermission> getAclsForUser(String userId);

    /**
     * Checks if the user has permissionMask for the acuityObjectIdentity
     *
     * @param userId prid of the user
     * @param datasets list of datasets
     * @param permissionMask permissionMask of the permission to check for, ie 32 is ViewDataset
     * @return boolean has permission or not
     */
    @Cacheable(value = "AcuitySecurityResourceClient-hasPermissionForUser")
    boolean hasPermissionForUser(String userId, List<Dataset> datasets, Integer permissionMask);

    /**
     * Loads a AcuityUserDetails from the userId
     *
     * @param userId userId to load
     * @return AcuityUserDetails object of name, fullname and authorities
     */
    AcuitySidDetails loadUserByUsername(String userId) throws IllegalAccessException, IOException;

    List<AcuitySidDetails> getUsersForDataset(Dataset dataset) throws IllegalAccessException, IOException;

    List<AcuitySidDetails> getUsersForDatasets(List<Dataset> datasets) throws IllegalAccessException, IOException;
}

