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
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.ADMINISTRATOR;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.PROACT_ONLY_USER;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_ONCOLOGY_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_PROACT_PACKAGE;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingAcls {

    @Autowired
    private SecurityAclService securitytAclService;
    @Autowired
    private AclPermissionCalculator aclPermissionCalculator;

    private MutableAcl acl;
    private DrugProgramme newDrugProgramme;
    private AcuitySidDetails user;

    @Before()
    public void create() {
        if (acl == null) {
            newDrugProgramme = new DrugProgramme(3011L);
            PrincipalSid owner = new PrincipalSid("unit_test3");
            user = AcuitySidDetails.toUser("glen");
            SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

            securitytAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        }
    }

    private MutableAcl getAcl() {
        return securitytAclService.find(newDrugProgramme);
    }

    @Test
    public void shouldGetRolePermissionForDevTeam() {

        securitytAclService.addGrantingAce(newDrugProgramme, DEVELOPMENT_TEAM.getMask(), user.toSid());

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(DEVELOPMENT_TEAM);
    }

    @Test
    public void shouldGetRolePermissionForAuthoriser() {

        securitytAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), user.toSid());

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISERS);
    }

    @Test
    public void shouldGetRolePermissionForAdministrator() {

        securitytAclService.addGrantingAce(newDrugProgramme, ADMINISTRATOR.getMask(), user.toSid());

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(ADMINISTRATOR);
    }

    @Test
    public void shouldGetRolePermissionForAuthUser() {

        securitytAclService.addGrantingAce(newDrugProgramme, AUTHORISED_USER.getMask(), user.toSid());

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISED_USER);
    }

    @Test
    public void shouldGetRolePermissionForAuthUser2() {

        securitytAclService.setViewPackagesAces(newDrugProgramme, newArrayList(AUTHORISED_USER.getMask()), user);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISED_USER);
    }

    @Test
    public void shouldGetRolePermissionForAuthUserWithOncology() {

        securitytAclService.setViewPackagesAces(newDrugProgramme, newArrayList(AUTHORISED_USER.getMask(), VIEW_ONCOLOGY_PACKAGE.getMask()), user);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISED_USER);
    }

    @Test
    public void shouldGetRolePermissionForAuthUserWithAllPackages() {

        securitytAclService.setViewPackagesAces(newDrugProgramme, newArrayList(AUTHORISED_USER.getMask(), PROACT_ONLY_USER.getMask(), VIEW_ONCOLOGY_PACKAGE.getMask()), user);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISED_USER);
    }

    @Test
    public void shouldGetRolePermissionForProactUser() {

        securitytAclService.setViewPackagesAces(newDrugProgramme, newArrayList(PROACT_ONLY_USER.getMask()), user);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(PROACT_ONLY_USER);
    }

    @Test
    public void shouldGetRolePermissionForAuthUserWithProactPackage() {

        securitytAclService.setViewPackagesAces(newDrugProgramme, newArrayList(AUTHORISED_USER.getMask(), VIEW_PROACT_PACKAGE.getMask()), user);

        Permission rolePermission = aclPermissionCalculator.getRolePermission(getAcl(), user);

        assertThat(rolePermission).isEqualTo(AUTHORISED_USER);
    }
}
