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
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUser;
import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUserFromGroup;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the order of permissions.
 * <p>
 * Currently the order in which you are added makes a difference, and also user permissions are more important that group permissions.
 *
 * <code>
 * User > Group
 * Within User or Group that lower the order (added in) the more important the permission
 * Local beats parent
 * </code>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-all-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingSecurityAclServiceToMimicDenyInheritance {

    @Autowired
    AclService aclService;
    @Autowired
    SecurityAclService securityAclService;

    // Test fixtures
    Permission viewPermission = AcuityPermissions.VIEW_VISUALISATIONS;
    AcuitySidDetails userTestDetails = toUser("UserTest");
    AcuitySidDetails group1Details = toUserFromGroup("Group1");

    // Acls
    DrugProgramme drugProgrammeA = new DrugProgramme(3L);
    Dataset vis3 = new AcuityDataset(10L);

    @Before
    public void before() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        userTestDetails.setAuthoritiesAsString(newArrayList(group1Details.getAuthoritiesAsString()));
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForUserDifferetnOrder() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsNotGranted(drugProgrammeA, viewPermission, userTestDetails); // first wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForUserAndGroup() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());

        assertThatIsNotGranted(drugProgrammeA, viewPermission, userTestDetails); // user permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForUserAndGroup1() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsNotGranted(drugProgrammeA, viewPermission, userTestDetails); // user permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForUserAndGroup2() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());

        assertThatIsGranted(drugProgrammeA, viewPermission, userTestDetails); // user permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForUserAndGroup3() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsGranted(drugProgrammeA, viewPermission, userTestDetails); // user permission wins
    }

    @Test
    public void shouldTestDeniedPermissonForGroup() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());

        assertThatIsNotGranted(drugProgrammeA, viewPermission, userTestDetails);
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), group1Details.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // local permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup1() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // local permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup2() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addRevokingAce(vis3, viewPermission.getMask(), group1Details.toSid());

        assertThatIsNotGranted(vis3, viewPermission, userTestDetails); // local permission wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup3() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), group1Details.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // local over parent wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup4() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addRevokingAce(vis3, viewPermission.getMask(), group1Details.toSid());

        assertThatIsNotGranted(vis3, viewPermission, userTestDetails); // local over parent wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup5() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // parent wins
    }

    @Test
    public void shouldTestDeniedPermissonOveridesGrantingForInheritanceUserAndGroup6() {

        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), group1Details.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // local wins
    }

    @Test
    public void shouldTestDeniedPermissonAll() {

        securityAclService.addRevokingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addRevokingAce(vis3, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsGranted(vis3, viewPermission, userTestDetails); // local user wins
    }

    @Test
    public void shouldTestDeniedPermissonAll2() {

        securityAclService.addGrantingAce(drugProgrammeA, viewPermission.getMask(), userTestDetails.toSid());
        securityAclService.addGrantingAce(vis3, viewPermission.getMask(), group1Details.toSid());
        securityAclService.addRevokingAce(vis3, viewPermission.getMask(), userTestDetails.toSid());

        assertThatIsNotGranted(vis3, viewPermission, userTestDetails); // local user wins
    }

    private void assertThatIsGranted(AcuityObjectIdentityImpl acuityObjectIdentity, Permission p, AcuitySidDetails acuitySidDetails) {
        assertThat(securityAclService.hasPermission(acuityObjectIdentity, p.getMask(), acuitySidDetails.toSids())).isTrue();
    }

    private void assertThatIsNotGranted(AcuityObjectIdentityImpl acuityObjectIdentity, Permission p, AcuitySidDetails acuitySidDetails) {
        assertThat(securityAclService.hasPermission(acuityObjectIdentity, p.getMask(), acuitySidDetails.toSids())).isFalse();
    }
}
