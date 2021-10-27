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

package com.acuity.va.security.acl.permissions;

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.PROACT_ONLY_USER;
import static com.google.common.collect.Lists.newArrayList;

@Component
public class AclPermissionCalculator {

    // enables us to build permissions from name and ints
    @Autowired
    private PermissionFactory permissionFactory;
    @Autowired
    private AcuityPermissionViewPackagesManager permissionViewPackagesManager;
    /**
     * Checks if the user has the permission for the particular acl
     */
    public boolean isGranted(Acl acl, List<Sid> sids, Permission permission) {
        List<Permission> permissions = newArrayList(permission);

        try {
            return acl.isGranted(permissions, sids, false);
        } catch (NotFoundException | UnloadedSidException ex) {
            return false;
        }
    }

    /**
     * Gets the permission of the user for the acl. One of AcuityCumulativePermissionsAsRoles.asHierarchy()
     * <p>
     * Now that DEV TEAM cant view dataset, need to carry on checking if they havent got permission on
     */
    public Permission getRolePermission(Acl acl, AcuitySidDetails user) {

        // check dev team first, because they could be AUTHORISED_USER aswell, DEVELOPMENT_TEAM overrides this
        if (isGranted(acl, user.toSids(), DEVELOPMENT_TEAM)) {
            return DEVELOPMENT_TEAM;
        }

        Permission lastGranted = null;

        //  check what role they have, first AUTHORISED_USER as most likely
        if (isGranted(acl, user.toSids(), AUTHORISED_USER)) {
            lastGranted = AUTHORISED_USER;

            // if the user is AUTHORISED_USER then check the higher roles in order
            for (Permission p : AcuityCumulativePermissionsAsRoles.asHierarchyWithOutAuthorisedUser()) {
                if (isGranted(acl, user.toSids(), p)) {
                    lastGranted = p;
                } else {
                    break;
                }
            }
        }

        // lastly if not found a role, check PROACT_ONLY_USER, most least likely
        if (lastGranted == null && isGranted(acl, user.toSids(), PROACT_ONLY_USER)) {
            lastGranted = PROACT_ONLY_USER;
        }

        return lastGranted;
    }

    /**
     * Assign the sum of view permissions looking at the inherited permission to.
     * This is done by doing a isGranted on all of the AcuityPermissions.getAllViewPackages()
     * permissions
     *
     * <code>
     * if DP = 7 and DS = 3 then the user has 1,2,4 = 7
     * if DP = 3 and DS = 11 then the user has 1,2,8 = 11
     * if DP = 7 and DS = 0 then the user has 1,2,4 = 7
     * if DP = 15 and DS = 3 then the user has 1,2,4,8 = 15
     * </code>
     * <p>
     * amlEnable says whether the Azure machine learning turned on or off
     */
    public Permission getViewPermission(Acl acl, AcuitySidDetails user) {
        List<Integer> grantedPermission = newArrayList();
        for (Permission p : permissionViewPackagesManager.getAllViewPackages()) {
            if (isGranted(acl, user.toSids(), p)) {
                grantedPermission.add(p.getMask());
            }
        }
        int sum = grantedPermission.stream().mapToInt(Integer::intValue).sum();
        return permissionFactory.buildFromMask(sum);
    }

}
