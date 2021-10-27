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

package com.acuity.va.security.acl.permission;

import com.acuity.va.security.acl.assertion.AcuityAssertions;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.acl.permissions.AcuityAclPermissionEvaluator;
import com.acuity.va.security.acl.service.SecurityAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class used to check all the current ways into the security system to check a permission
 *
 * @author Glen
 */
public class AbstractPermissionEvaluator {

    @Autowired
    protected SecurityAclService securityAclService;
    @Autowired
    protected AcuityAclPermissionEvaluator evaluator;
    @Autowired
    protected PermissionFactory permissionFactory;

    protected void hasPermission(AcuitySidDetails user, AcuityObjectIdentity objectIdentity, Permission requestedPermission, boolean assertPermission) {
        boolean hasPermission = evaluator.hasPermission(user, objectIdentity, requestedPermission);
        assertThat(hasPermission).isEqualTo(assertPermission);

        hasPermission = evaluator.hasPermission(user, objectIdentity.getId(), objectIdentity.getClass().getSimpleName(), requestedPermission);
        assertThat(hasPermission).isEqualTo(assertPermission);

        Acl acl = securityAclService.find(objectIdentity);
        AcuityAssertions.assertThat(acl).isGranted(user.toSids(), requestedPermission).isEqualTo(assertPermission);

        if (requestedPermission == AcuityPermissions.VIEW_VISUALISATIONS) {
            if (objectIdentity.thisDrugProgrammeType()) {
                hasPermission = evaluator.hasDPPermission(user, objectIdentity.getClass().getSimpleName(), objectIdentity.getIdentifier());
                assertThat(hasPermission).isEqualTo(assertPermission);

            } else if (objectIdentity.thisClinicalStudyType()) {
                hasPermission = evaluator.hasStudyPermission(user, objectIdentity.getClass().getSimpleName(), objectIdentity.getIdentifier());
                assertThat(hasPermission).isEqualTo(assertPermission);

            } else if (objectIdentity.thisDatasetType()) {
                hasPermission = evaluator.hasVisPermission(user, objectIdentity.getClass().getSimpleName(), objectIdentity.getIdentifier());
                assertThat(hasPermission).isEqualTo(assertPermission);
            }
        }
    }
}
