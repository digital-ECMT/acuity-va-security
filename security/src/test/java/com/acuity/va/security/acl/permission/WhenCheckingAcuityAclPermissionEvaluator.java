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
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.ADMINISTRATOR;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_DATA_OWNERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_TRAINED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhenCheckingAcuityAclPermissionEvaluator extends AbstractPermissionEvaluator {

    // Test fixtures
    Permission viewPermission = VIEW_VISUALISATIONS; //1
    Permission editTrainedUsersPermission = EDIT_TRAINED_USERS; // 64
    Permission editDataOwnersPermission = EDIT_DATA_OWNERS; // 128
    Permission viewAndEditDataOwnersPermission = new CumulativePermission().set(AUTHORISED_USER).set(EDIT_AUTHORISED_USERS);
    Permission viewAndEditDataOwnersAndEditTrainedUsersPermission = new CumulativePermission().set(VIEW_VISUALISATIONS).set(EDIT_TRAINED_USERS).set(EDIT_TRAINED_USERS);
    Permission editDataOwnersAndEditTrainedUsersPermission = new CumulativePermission().set(EDIT_TRAINED_USERS).set(EDIT_DATA_OWNERS);

    String user1Sid = "User1";
    String user4Sid = "User4";
    String user5Sid = "User5";
    String group5Sid = "Group1";
    String unknownSid = "Unknown";

    // Acls
    Acl drugProgrammeA;
    Acl study1;

    @Before
    public void beforeClass() {
        drugProgrammeA = securityAclService.find(new DrugProgramme(2L)); // Drug A
        study1 = securityAclService.find(new ClinicalStudy(4L)); // Study 1
    }

    @Test
    public void shouldEvaluateCorrectPermissionsFor_User4_DrugProgrammeA() {
        // cant mvoe to 4.2 for dirties context BEFORE_CLASS, so do it manually
    }

    @Test
    public void shouldEvaluateCorrectPermissionsFor_User4_DrugProgrammeA_() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user4Sid);
        AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromGroup(group5Sid);

        hasPermission(acuityUserDetails, identity, viewPermission, true);
        hasPermission(acuityUserDetails, identity, editTrainedUsersPermission, false);
        hasPermission(acuityUserDetails, identity, editDataOwnersPermission, false);
        hasPermission(acuityUserDetails, identity, viewAndEditDataOwnersPermission, true);
        hasPermission(acuityGroupDetails, identity, viewPermission, false);
    }

    @Test
    public void shouldEvaluateCorrectPermissionsFor_User5_Study1_FromWriteCreatePermissionMask_Parent() {
        AcuityObjectIdentityImpl identity = new ClinicalStudy(4L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);
        AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromGroup(group5Sid);

        hasPermission(acuityUserDetails, identity, viewPermission, true);
        hasPermission(acuityUserDetails, identity, editTrainedUsersPermission, false);
        hasPermission(acuityUserDetails, identity, editDataOwnersPermission, false);
        hasPermission(acuityUserDetails, identity, editDataOwnersAndEditTrainedUsersPermission, false);
        hasPermission(acuityUserDetails, identity, viewAndEditDataOwnersAndEditTrainedUsersPermission, false);
        hasPermission(acuityGroupDetails, identity, viewPermission, false);
    }

    @Test
    public void shouldEvaluateCorrectPermissionsFor_Group1_Vis22_FromViewPermissionMask() {
        AcuityObjectIdentityImpl identity = new AcuityDataset(22L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);
        AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromGroup(group5Sid);

        hasPermission(acuityUserDetails, identity, viewPermission, false);
        hasPermission(acuityGroupDetails, identity, viewPermission, true);
    }

    /**
     * User 4 has permission role ADMINISTRATOR 2146990113, so they cant edit AUTHORISED_USER and AUTHORISERS
     */
    @Test
    public void shouldEvaluateAddPermissionFor_User4_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user4Sid);
        AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromGroup(group5Sid);

        boolean hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), AUTHORISED_USER.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), ADMINISTRATOR.getMask());
        assertThat(hasPermission).isFalse();

        hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), AUTHORISERS.getMask());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void shouldEvaluateRemovePermissionFor_User4_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user4Sid);
        AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromGroup(group5Sid);

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, AUTHORISED_USER.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, ADMINISTRATOR.getMask());
        assertThat(hasPermission).isFalse();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, AUTHORISERS.getMask());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void shouldEvaluateRemoveAllPermissionsFor_User4_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user4Sid);

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, null);
        assertThat(hasPermission).isFalse();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, 0);
        assertThat(hasPermission).isFalse();
    }

    /**
     * User 5 has permission role AUTHORISERS 2147188257, so they can edit AUTHORISED_USERS and ADMINISTRATOR but not AUTHORISERS
     */
    @Test
    public void shouldEvaluateAddPermissionFor_User5_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);

        boolean hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), AUTHORISED_USER.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), ADMINISTRATOR.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasAddPermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), AUTHORISERS.getMask());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void shouldEvaluateRemovePermissionFor_User5_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user5Sid, AUTHORISED_USER.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user5Sid, ADMINISTRATOR.getMask());
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user5Sid, AUTHORISERS.getMask());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void shouldEvaluateRemoveAllPermissionsFor_User5_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user5Sid, null);
        assertThat(hasPermission).isFalse();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user5Sid, 0);
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void shouldEvaluateRemoveAllPermissionsFor_UnknownUser_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(unknownSid); // no permission to delete

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), unknownSid, AUTHORISERS.getMask());
        assertThat(hasPermission).isFalse();
    }
    
    @Test
    public void shouldEvaluateRemovelPermissionFor_UnknownUser_DrugProgrammeA() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(unknownSid); // no permission to delete

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), unknownSid, null);
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), unknownSid, 0);
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void shouldEvaluateRemoveAllPermissionsFor_User4_DrugProgrammeA_By_User5() {
        AcuityObjectIdentity identity = new DrugProgramme(2L);
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(user5Sid);

        boolean hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, null);
        assertThat(hasPermission).isTrue();

        hasPermission = evaluator.
                hasRemovePermission(acuityUserDetails, identity.getId(), identity.getClass().getSimpleName(), user4Sid, 0);
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void shouldEvaluateIsTrainingDataFor_Vis_ACUITY_Oncology_STDY4321_Dummy_Instance() {

        boolean isTrainingData = evaluator.isTrainingData(22L);

        assertThat(isTrainingData).isTrue();
    }

    @Test
    public void shouldNotEvaluateIsTrainingDataFor_Vis_ACUITY_Tolerability_STDY1822_Studies_1_and_2() {

        boolean isTrainingData = evaluator.isTrainingData(10L);

        assertThat(isTrainingData).isFalse();
    }
}
