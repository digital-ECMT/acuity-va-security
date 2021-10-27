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
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AclPermissionCalculator;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUser;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.PROACT_ONLY_USER;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_BASE_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_ONCOLOGY_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_PROACT_PACKAGE;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingSecurityAclServiceViewModulesPermissions {

    @Autowired
    private SecurityAclService securityAclService;
    @Autowired
    private AclPermissionCalculator aclPermissionCalculator;

    // Test fixtures
    AcuitySidDetails user1Details = toUser("User1");

    @Test
    public void shouldSetNewViewModuleAcesForUser1() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        Dataset dataset = new AcuityDataset(2L);
        securityAclService.createAcl(dataset, "glen");
        securityAclService.addGrantingAce(dataset, PROACT_ONLY_USER.getMask(), user1Details.toSid());

        securityAclService.setViewPackagesAces(dataset, newArrayList(AUTHORISED_USER.getMask(), VIEW_ONCOLOGY_PACKAGE.getMask()), user1Details);

        MutableAcl acl = securityAclService.find(dataset);

        Permission viewPermission = aclPermissionCalculator.getViewPermission(acl, user1Details);
        Permission rolePermission = aclPermissionCalculator.getRolePermission(acl, user1Details);

        assertThat(viewPermission.getMask()).isEqualTo(VIEW_ONCOLOGY_PACKAGE.getMask() + AUTHORISED_USER.getMask());
        assertThat(rolePermission.getMask()).isEqualTo(AUTHORISED_USER.getMask());

        boolean hasViewSubjectsPermission = securityAclService.hasPermission(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSids());
        boolean hasViewTimelinePermission = securityAclService.hasPermission(dataset, VIEW_PROACT_PACKAGE.getMask(), user1Details.toSids());

        assertThat(hasViewSubjectsPermission).isTrue();
        assertThat(hasViewTimelinePermission).isFalse();
    }

    @Test
    public void shouldRemoveOldViewModuleAcesForUserAsProactOnly() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        Dataset dataset = new AcuityDataset(2L);
        securityAclService.createAcl(dataset, "glen");

        securityAclService.addGrantingAce(dataset, AUTHORISED_USER.getMask(), user1Details.toSid());
        // this should be removed when setting
        securityAclService.addGrantingAce(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSid());

        boolean hasViewSubjectsPermission = securityAclService.hasPermission(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSids());
        assertThat(hasViewSubjectsPermission).isTrue();

        securityAclService.setViewPackagesAces(dataset, newArrayList(VIEW_PROACT_PACKAGE.getMask()), user1Details);

        MutableAcl acl = securityAclService.find(dataset);

        Permission viewPermission = aclPermissionCalculator.getViewPermission(acl, user1Details);
        Permission rolePermission = aclPermissionCalculator.getRolePermission(acl, user1Details);

        assertThat(viewPermission.getMask()).isEqualTo(PROACT_ONLY_USER.getMask());
        assertThat(rolePermission.getMask()).isEqualTo(PROACT_ONLY_USER.getMask());

        hasViewSubjectsPermission = securityAclService.hasPermission(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSids());
        boolean hasViewTimelinePermission = securityAclService.hasPermission(dataset, VIEW_PROACT_PACKAGE.getMask(), user1Details.toSids());

        assertThat(hasViewSubjectsPermission).isFalse();
        assertThat(hasViewTimelinePermission).isTrue();
    }

    @Test
    public void shouldnotAddNoneViewModuleAcesForUser1() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        Dataset dataset = new AcuityDataset(2L);
        securityAclService.createAcl(dataset, "glen");

        securityAclService.addGrantingAce(dataset, AUTHORISED_USER.getMask(), user1Details.toSid());

        securityAclService.setViewPackagesAces(dataset, newArrayList(EDIT_AUTHORISED_USERS.getMask(), VIEW_ONCOLOGY_PACKAGE.getMask()), user1Details);

        boolean hasViewSubjectsPermission = securityAclService.hasPermission(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSids());
        boolean hasEditAuthUsersPermission = securityAclService.hasPermission(dataset, EDIT_AUTHORISED_USERS.getMask(), user1Details.toSids());

        assertThat(hasViewSubjectsPermission).isTrue();
        assertThat(hasEditAuthUsersPermission).isFalse();
    }

    @Autowired
    private PermissionFactory permissionFactory;

    //@Test
    public void test() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        Dataset dataset = new AcuityDataset(2L);

        int mask = AUTHORISED_USER.getMask() + VIEW_PROACT_PACKAGE.getMask() + VIEW_BASE_PACKAGE.getMask() + VIEW_ONCOLOGY_PACKAGE.getMask();

        Permission permission = permissionFactory.buildFromMask(mask);

        System.out.println(mask);
        System.out.println(permission);
    }

    @Test
    public void shouldOnlyHaveAccessToBothParentDGAndDatasetPermission() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        DrugProgramme dp = new DrugProgramme(200L);
        MutableAcl dpAcl = securityAclService.createAcl(dp, "glen");

        Dataset dataset = new AcuityDataset(201L);
        dataset.setParent(dp);

        MutableAcl datasetAcl = securityAclService.createAcl(dataset, "glen");

        securityAclService.addGrantingAce(dp, AUTHORISED_USER.getMask(), user1Details.toSid());
        securityAclService.setViewPackagesAces(dp, newArrayList(VIEW_ONCOLOGY_PACKAGE.getMask()), user1Details);

        securityAclService.addGrantingAce(dataset, AUTHORISED_USER.getMask(), user1Details.toSid());
        securityAclService.setViewPackagesAces(dataset, newArrayList(VIEW_ONCOLOGY_PACKAGE.getMask(), VIEW_PROACT_PACKAGE.getMask()), user1Details);
        //securityAclService.addRevokingAce(dataset, VIEW_TIMELINE.getMask(), user1Details.toSid());

        boolean hasLiverPermission = securityAclService.hasPermission(dataset, VIEW_ONCOLOGY_PACKAGE.getMask(), user1Details.toSids());
        assertThat(hasLiverPermission).isTrue();

        boolean hasTimelinePermission = securityAclService.hasPermission(dataset, VIEW_PROACT_PACKAGE.getMask(), user1Details.toSids());
        assertThat(hasTimelinePermission).isTrue();
    }
}
