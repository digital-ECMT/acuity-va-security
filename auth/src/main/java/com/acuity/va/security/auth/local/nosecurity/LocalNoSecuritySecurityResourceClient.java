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

package com.acuity.va.security.auth.local.nosecurity;

import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.auth.common.ISecurityResourceClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.SystemUtils.USER_NAME;

@Service
@Profile("local-no-security")
public class LocalNoSecuritySecurityResourceClient implements ISecurityResourceClient {

    @Override
    public AcuitySidDetails loadUserByUsername(String userId) {
        AcuitySidDetails toUser = AcuitySidDetails.toUser(userId);
        toUser.setAuthenticated(true);
        toUser.setAuthoritiesAsString(newArrayList(
                "ROLE_ACL_ADMINISTRATOR",
                "ROLE_TRAINED_USER",
                "ROLE_DEVELOPMENT_TEAM",
                "ACTUATOR"
        ));

        return toUser;
    }

    @Override
    public List<AcuityObjectIdentityWithPermission> getAclsForUser(String userId) {
        throw new UnsupportedOperationException("The operation is unsupported with the local-no-security profile");
    }

    @Override
    public boolean hasPermissionForUser(String userId, List<Dataset> datasets, Integer permissionMask) {
        return true;
    }

    @Override
    public List<AcuitySidDetails> getUsersForDataset(Dataset dataset) {
        return newArrayList(AcuitySidDetails.toUser(USER_NAME.toLowerCase()));
    }

    @Override
    public List<AcuitySidDetails> getUsersForDatasets(List<Dataset> datasets) {
        return newArrayList(AcuitySidDetails.toUser(USER_NAME.toLowerCase()));
    }
}
