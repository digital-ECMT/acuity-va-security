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

package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AclPermissionCalculator;
import com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUser;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_TRAINED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingSecurityAclServiceForDevTeam {

    @Autowired
    private SecurityAclService securitytAclService;
    @Autowired
    private AclPermissionCalculator aclPermissionCalculator;

    // Test fixtures
    AcuitySidDetails devTeamUserDetails = toUser("User1");
    AcuitySidDetails user2Details = toUser("User2");
    AcuitySidDetails devPlusViewUserDetails = toUser("DevTeamPlusViewPermission");

    @Test
    public void shouldFindDevTeamPermissionForUser1() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        DrugProgramme drugProgramme = new DrugProgramme(2L);
        securitytAclService.addAce(drugProgramme, AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM.getMask(), devTeamUserDetails.toSid(), true);

        MutableAcl acl = securitytAclService.find(drugProgramme);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(acl, devTeamUserDetails);
        Permission viewPermission = aclPermissionCalculator.getViewPermission(acl, devTeamUserDetails);

        assertThat(rolePermission).isEqualTo(AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM);
        assertThat(viewPermission).isEqualTo(AcuityCumulativePermissionsAsRoles.AUTHORISED_USER);
    }

    @Test
    public void shouldFindAuthorisedUserPermissionForUser2() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        DrugProgramme drugProgramme = new DrugProgramme(2L);
        securitytAclService.addAce(drugProgramme, AcuityCumulativePermissionsAsRoles.AUTHORISED_USER.getMask(), user2Details.toSid(), true);

        MutableAcl acl = securitytAclService.find(drugProgramme);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(acl, user2Details);
        Permission viewPermission = aclPermissionCalculator.getViewPermission(acl, user2Details);

        assertThat(rolePermission).isEqualTo(AcuityCumulativePermissionsAsRoles.AUTHORISED_USER);
        assertThat(viewPermission).isEqualTo(AcuityCumulativePermissionsAsRoles.AUTHORISED_USER);
    }

    @Test
    public void shouldAllDevAndViewTogetherAndTestForViewAccessPermission() {

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        MutableAcl acl = securitytAclService.find(drugProgramme);

        securitytAclService.addAce(drugProgramme, DEVELOPMENT_TEAM.getMask(), devPlusViewUserDetails.toSid(), true);

        boolean hasViewPermission = securitytAclService.hasPermission(drugProgramme, VIEW_VISUALISATIONS.getMask(), devPlusViewUserDetails.toSids());
        boolean hasEditPermission = securitytAclService.hasPermission(drugProgramme, EDIT_TRAINED_USERS.getMask(), devPlusViewUserDetails.toSids());
        assertThat(hasViewPermission).isFalse();
        assertThat(hasEditPermission).isTrue();

        securitytAclService.addAce(drugProgramme, VIEW_VISUALISATIONS.getMask(), devPlusViewUserDetails.toSid(), true);

        hasViewPermission = securitytAclService.hasPermission(drugProgramme, VIEW_VISUALISATIONS.getMask(), devPlusViewUserDetails.toSids());
        hasEditPermission = securitytAclService.hasPermission(drugProgramme, EDIT_TRAINED_USERS.getMask(), devPlusViewUserDetails.toSids());
        assertThat(hasViewPermission).isTrue();
        assertThat(hasEditPermission).isTrue();

        MutableAcl newAcl = securitytAclService.find(drugProgramme);
        Permission rolePermission = aclPermissionCalculator.getRolePermission(newAcl, devPlusViewUserDetails);
        Permission viewPermission = aclPermissionCalculator.getViewPermission(newAcl, devPlusViewUserDetails);

        assertThat(rolePermission).isEqualTo(DEVELOPMENT_TEAM);
        assertThat(viewPermission.getMask()).isEqualTo(1);
    }
}
