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

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.acls.model.Permission;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.ADMINISTRATOR;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.getIndividualPermissionForPermissionsAsRole;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.getPermissionRoleFromPermissionMask;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_ADMINISTRATORS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_ONCOLOGY_PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
public class WhenUsingCumulativePermissions extends AbstractPermissionEvaluator {

    @Test
    public void shouldHaveCorrectMaskForCumulativePermissions() {

        assertThat(AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM.getMask()).isEqualTo(522240);
        assertThat(AcuityCumulativePermissionsAsRoles.AUTHORISERS.getMask()).isEqualTo(442383);
        assertThat(AcuityCumulativePermissionsAsRoles.ADMINISTRATOR.getMask()).isEqualTo(32783);
        assertThat(AcuityCumulativePermissionsAsRoles.AUTHORISED_USER.getMask()).isEqualTo(3);
    }

    @Test
    public void developmentTeamShouldHaveViewVisPermission() {

        assertThat(AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM.getMask()).isEqualTo(522240);
    }

    @Test
    public void shouldHaveCorrectMaskFromCreatingFromPermissionMask() {

        Permission permission = permissionFactory.buildFromMask(DEVELOPMENT_TEAM.getMask());
        assertThat(permission.getMask()).isEqualTo(DEVELOPMENT_TEAM.getMask());
        assertThat(permission).isEqualTo(DEVELOPMENT_TEAM);

        permission = permissionFactory.buildFromMask(AUTHORISED_USER.getMask());
        assertThat(permission.getMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(permission).isEqualTo(AUTHORISED_USER);

    }

    @Test
    public void shouldHaveCorrectIndividualPermissionsForEditingRolePermissions() {

        Permission individualPermissionForPermissionsAsRole = getIndividualPermissionForPermissionsAsRole(ADMINISTRATOR);
        assertThat(individualPermissionForPermissionsAsRole).isEqualTo(EDIT_ADMINISTRATORS);

        individualPermissionForPermissionsAsRole = getIndividualPermissionForPermissionsAsRole(AUTHORISERS);
        assertThat(individualPermissionForPermissionsAsRole).isEqualTo(EDIT_AUTHORISERS);

        individualPermissionForPermissionsAsRole = getIndividualPermissionForPermissionsAsRole(AUTHORISED_USER);
        assertThat(individualPermissionForPermissionsAsRole).isEqualTo(EDIT_AUTHORISED_USERS);
    }

    @Test
    public void shouldHaveCorrectGetPermissionRoleFromPermissonMask() {

        Permission individualPermissionForPermissionsAsRole = getPermissionRoleFromPermissionMask(ADMINISTRATOR.getMask());
        assertThat(individualPermissionForPermissionsAsRole.getMask()).isEqualTo(ADMINISTRATOR.getMask());

        individualPermissionForPermissionsAsRole = getPermissionRoleFromPermissionMask(AUTHORISERS.getMask());
        assertThat(individualPermissionForPermissionsAsRole.getMask()).isEqualTo(AUTHORISERS.getMask());

        individualPermissionForPermissionsAsRole = getPermissionRoleFromPermissionMask(AUTHORISED_USER.getMask());
        assertThat(individualPermissionForPermissionsAsRole.getMask()).isEqualTo(AUTHORISED_USER.getMask());

        individualPermissionForPermissionsAsRole = getPermissionRoleFromPermissionMask(AUTHORISED_USER.getMask() + VIEW_ONCOLOGY_PACKAGE.getMask());
        assertThat(individualPermissionForPermissionsAsRole.getMask()).isEqualTo(AUTHORISED_USER.getMask());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorIfPassInInvalidPermissionForEditingRolePermissions() {

        getIndividualPermissionForPermissionsAsRole(DEVELOPMENT_TEAM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorIfPassInWrongPermissionForEditingRolePermissions() {

        getIndividualPermissionForPermissionsAsRole(EDIT_ADMINISTRATORS);
    }
}
