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
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUser;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-all-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingSecurityAclServiceToMimicFullScenario {

    @Autowired
    AclService aclService;
    @Autowired
    SecurityAclService securityAclService;

    // Test fixtures
    Permission viewPermission = AcuityPermissions.VIEW_VISUALISATIONS;
    AcuitySidDetails user1Details = toUser("User1");
    AcuitySidDetails user2Details = toUser("User2");
    AcuitySidDetails user3Details = toUser("User3");

    // Acls
    DrugProgramme drugProgrammeA = new DrugProgramme(2L); // Drug A
    DrugProgramme drugProgrammeB = new DrugProgramme(3L); // Drug B
    ClinicalStudy study1 = new ClinicalStudy(4L); // Study 1
    ClinicalStudy study2 = new ClinicalStudy(5L); // Study 2
    ClinicalStudy study3 = new ClinicalStudy(6L); // Study 3
    Dataset vis1 = new AcuityDataset(7L); // Vis Instance 1
    Dataset vis2Study1 = new AcuityDataset(8L); // Vis Instance 2 S1
    Dataset vis2Study2 = new AcuityDataset(9L); // Vis Instance 2 S2
    Dataset vis3 = new AcuityDataset(10L); // Vis Instance 3

    @Test
    public void shouldHaveCorrectPermissionsFor_User1_DrugProgrammeA() {

        // Drug Programs
        assertThatIsGranted(drugProgrammeA, viewPermission, user1Details);
        assertThatIsNotGranted(drugProgrammeB, viewPermission, user1Details);

        // Studies
        assertThatIsGranted(study1, viewPermission, user1Details);
        assertThatIsGranted(study2, viewPermission, user1Details);
        assertThatIsNotGranted(study3, viewPermission, user1Details);

        // Vis
        assertThatIsGranted(vis1, viewPermission, user1Details);
        assertThatIsGranted(vis2Study1, viewPermission, user1Details);
        assertThatIsGranted(vis2Study2, viewPermission, user1Details);
        assertThatIsNotGranted(vis3, viewPermission, user1Details);
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User2_Study1() {

        // Drug Programs
        assertThatIsNotGranted(drugProgrammeA, viewPermission, user2Details);
        assertThatIsNotGranted(drugProgrammeB, viewPermission, user2Details);

        // Studies
        assertThatIsGranted(study1, viewPermission, user2Details);
        assertThatIsNotGranted(study2, viewPermission, user2Details);
        assertThatIsNotGranted(study3, viewPermission, user2Details);

        // Vis
        assertThatIsNotGranted(vis1, viewPermission, user2Details);
        assertThatIsNotGranted(vis2Study1, viewPermission, user2Details);
        assertThatIsNotGranted(vis2Study2, viewPermission, user2Details);
        assertThatIsNotGranted(vis3, viewPermission, user2Details);
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User3_Vis1() {

        // Drug Programs
        assertThatIsNotGranted(drugProgrammeA, viewPermission, user3Details);
        assertThatIsNotGranted(drugProgrammeB, viewPermission, user3Details);

        // Studies
        assertThatIsNotGranted(study1, viewPermission, user3Details);
        assertThatIsNotGranted(study2, viewPermission, user3Details);
        assertThatIsNotGranted(study3, viewPermission, user3Details);

        // Vis
        assertThatIsNotGranted(vis1, viewPermission, user3Details);
        assertThatIsNotGranted(vis2Study1, viewPermission, user3Details);
        assertThatIsNotGranted(vis2Study2, viewPermission, user3Details);
        assertThatIsGranted(vis3, viewPermission, user3Details);
    }

    private void assertThatIsGranted(AcuityObjectIdentityImpl acuityObjectIdentity, Permission p, AcuitySidDetails acuitySidDetails) {
        //assertThat(securityAclService.hasPermission(objectIdentity, p, user)).isTrue();
        assertThat(securityAclService.hasPermission(acuityObjectIdentity, p.getMask(), acuitySidDetails.toSids())).isTrue();
    }

    private void assertThatIsNotGranted(AcuityObjectIdentityImpl acuityObjectIdentity, Permission p, AcuitySidDetails acuitySidDetails) {
        // assertThat(securityAclService.hasPermission(objectIdentity, p, user)).isFalse();
        assertThat(securityAclService.hasPermission(acuityObjectIdentity, p.getMask(), acuitySidDetails.toSids())).isFalse();
    }
}
