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

import com.google.common.collect.Lists;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;

import java.util.List;
import java.util.stream.Collector;

import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_ADMINISTRATORS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_CLINICAL_STUDIES;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_VISUALISATIONS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_BASE_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_ONCOLOGY_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_PROACT_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;

/**
 * These are combined permissions as a kind of role (not implemented as a role in RBAC/GBAC sense). These are write only permissions to the ACL database as a
 * mask in table acl_entry.
 * <p>
 * insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) values(1, 30, 1, 20, 1, 262112, 1, 1); --
 * DEVELOPMENT_TEAM permission, ie all acuity permissions
 * <p>
 * These represent combined list of permissions. For a simple example. Spring gives READ 1 and WRITE 2. So a READ/WRITE permission would be mask 3. In this
 * sense its a READ_WRITE_ROLE mask 3 so that 3 can be inserted into the acl db
 * <p>
 * Write only means only checks on the AcuityPermissions will be done in the
 *
 * @author glen
 * @PreAuthroise(hasPermission(AcuityPermissions.EDIT_TRAINED_USERS) and as we have overriden the PermissionEvaluator as in
 * <p>
 * http://stackoverflow.com/questions/9018264/how-do-i-use-the-mask-field-in-acl-entry-table-in-spring-security-3-1
 * <p>
 * This is to solve the problem with springs own DefaultPermissionGrantingStrategy that for example user with a role of mask 3 for * read/write, but the user is
 * denied access to an object with a permission mask of 1 for read.
 * <p>
 * So with our custom DefaultPermissionGrantingStrategy, as user with permission mask 96 (hence has VIEW_VISUALISATIONS (32) * EDIT_TRAINED_USERS (64)) will
 * return true for permission check VIEW_VISUALISATIONS (32) and EDIT_TRAINED_USERS (64) with the code
 * <p>
 * <code>
 * ((96 | 32) == 96) is true
 * ((96 | 64) == 96) is true
 * </code>
 * <p>
 * and if they as for say READ permission 1 (which we haven't implemented) then or a higher permission
 * <p>
 * <code>
 * ((96 | 1) == 96) is false
 * ((96 | 128) == 96) is false for
 * </code>
 */
public final class AcuityCumulativePermissionsAsRoles {

    private AcuityCumulativePermissionsAsRoles() {
    }

    /**
     * This will be attached as a role to a group and users added to this group for DEVELOPMENT_TEAM.
     * <p>
     * == mask 522240 (was 262080)
     * <p>
     * Uses a custom collector to run CumulativePermission.set on each of the Permissions set in AcuityPermissions
     */
    public static final Permission DEVELOPMENT_TEAM = AcuityPermissions.getAllEditPermissions().stream()
            .collect(Collector.of(CumulativePermission::new, CumulativePermission::set, (left, right) -> left));

    // == mask 442383 (was 2147450400)
    public static final Permission AUTHORISERS = new CumulativePermission().
            set(VIEW_VISUALISATIONS).
            set(EDIT_ADMINISTRATORS).
            set(EDIT_AUTHORISED_USERS).
            set(EDIT_CLINICAL_STUDIES).
            set(EDIT_VISUALISATIONS).
            set(VIEW_BASE_PACKAGE).
            set(VIEW_ONCOLOGY_PACKAGE).
            set(VIEW_PROACT_PACKAGE);

    //  == mask 32783 (was 2147253280 (was 2147252256))
    public static final Permission ADMINISTRATOR = new CumulativePermission().
            set(VIEW_VISUALISATIONS).
            set(EDIT_AUTHORISED_USERS).
            set(VIEW_BASE_PACKAGE).
            set(VIEW_ONCOLOGY_PACKAGE).
            set(VIEW_PROACT_PACKAGE);

    //  == mask 3 (was 32)
    public static final Permission AUTHORISED_USER = new CumulativePermission().
            set(VIEW_VISUALISATIONS).
            set(VIEW_BASE_PACKAGE);

    //  == mask 8 
    public static final Permission PROACT_ONLY_USER = new CumulativePermission().
            set(VIEW_PROACT_PACKAGE);

    /**
     * List the PermissionsAsRole in order of importance, lowest to highest
     */
    public static List<Permission> asHierarchyWithOutAuthorisedUser() {
        return Lists.newArrayList(ADMINISTRATOR, AUTHORISERS, DEVELOPMENT_TEAM);
    }

    /**
     * Gets the individual permission for modifying a acuityCumulativePermissionsAsRole object.
     * <p>
     * ie. if i wanted to add a user as AUTHORISERS 2147188257, then the permission for that is EDIT_AUTHORISERS 256 ie. if i wanted to add a user as
     * DRUG_PROGRAMME_DATA_OWNER 2147188513, then the permission for that is EDIT_DATA_OWNERS 128
     *
     * @param acuityCumulativePermissionsAsRole one of AUTHORISED_USER, ADMINISTRATOR, AUTHORISERS, DRUG_PROGRAMME_DATA_OWNER, DEVELOPMENT_TEAM
     * @return AcuityPermission needed to add/remove user for that acuityCumulativePermissionsAsRole
     */
    public static Permission getIndividualPermissionForPermissionsAsRole(Permission acuityCumulativePermissionsAsRole) {
        if (acuityCumulativePermissionsAsRole.equals(AUTHORISED_USER)) {
            return EDIT_AUTHORISED_USERS;
        } else if (acuityCumulativePermissionsAsRole.equals(ADMINISTRATOR)) {
            return EDIT_ADMINISTRATORS;
        } else if (acuityCumulativePermissionsAsRole.equals(AUTHORISERS)) {
            return EDIT_AUTHORISERS;
        } else {
            throw new IllegalArgumentException("Not allowed to edit this permission " + acuityCumulativePermissionsAsRole);
        }
    }

    public static Permission getPermissionRoleFromPermissionMask(Integer acuityCumulativePermissionMask) {
        if (checkPermission(AUTHORISERS.getMask(), acuityCumulativePermissionMask)) {
            return AUTHORISERS;
        } else if (checkPermission(ADMINISTRATOR.getMask(), acuityCumulativePermissionMask)) {
            return ADMINISTRATOR;
        } else if (checkPermission(AUTHORISED_USER.getMask(), acuityCumulativePermissionMask)) {
            return AUTHORISED_USER;
        } else if (checkPermission(PROACT_ONLY_USER.getMask(), acuityCumulativePermissionMask)) {
            return PROACT_ONLY_USER;
        } else {
            throw new IllegalArgumentException("Not allowed to edit this permission " + acuityCumulativePermissionMask);
        }
    }

    public static boolean checkPermission(Integer required, Integer userPermission) {
        return (userPermission | required) == userPermission;
    }
}
