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
import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.dao.AclRepositoryImpl;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.DrugStudyDatasetWithPermission;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMask;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUser;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.ADMINISTRATOR;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISERS;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.EDIT_AUTHORISED_USERS;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_BASE_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingSecurityAclService {

    @Autowired
    private SecurityAclService securityAclService;

    @Autowired
    private CustomUserDetailsManager customUserDetailsManager;

    @Autowired
    private AclRepository aclRepository;

    @Autowired
    private AclRepositoryImpl aclRepositoryImpl;

    // Test fixtures
    AcuitySidDetails user1Details = toUser("User1");
    AcuitySidDetails user2Details = toUser("User2");
    AcuitySidDetails user3Details = toUser("User3");
    AcuitySidDetails user4Details = toUser("User4");
    AcuitySidDetails group1Details = AcuitySidDetails.toUserFromGroup("Group1");

    @Test
    public void shouldFindObjectIdentities() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        MutableAcl acl = securityAclService.find(drugProgramme);

        assertThat(acl).isNotNull();
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User1_DrugProgrammeA() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(user1Details);

        assertThat(userObjectIdentities).hasSize(7);
        assertThat(userObjectIdentities).extracting("id").containsExactly(2L, 4L, 5L, 7L, 8L, 9L, 49L);
        assertThat(userObjectIdentities).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities).extracting("viewPermissionMask").containsOnly(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities).extracting("canView").containsOnly(true);
        userObjectIdentities.forEach(out::println);
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User2_Study1() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(user2Details);

        assertThat(userObjectIdentities).hasSize(1);
        assertThat(userObjectIdentities.get(0).getIdentifier()).isEqualTo(4L);
        assertThat(userObjectIdentities.get(0)).isExactlyInstanceOf(ClinicalStudy.class);
        assertThat(userObjectIdentities.get(0).getRolePermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getViewPermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getCanView()).isTrue();
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User3_Vis1() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(user3Details);

        assertThat(userObjectIdentities).hasSize(1);
        assertThat(userObjectIdentities.get(0)).isEqualToComparingOnlyGivenFields(new AcuityDataset(10L), "id");
        assertThat(userObjectIdentities.get(0)).isExactlyInstanceOf(AcuityDataset.class);
        assertThat(userObjectIdentities.get(0).getRolePermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getViewPermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getCanView()).isTrue();
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_Group1_Vis21() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(group1Details);

        assertThat(userObjectIdentities).hasSize(1);
        assertThat(userObjectIdentities.get(0)).isEqualToComparingOnlyGivenFields(new AcuityDataset(22L), "id");
        assertThat(userObjectIdentities.get(0)).isExactlyInstanceOf(AcuityDataset.class);
        assertThat(userObjectIdentities.get(0).getRolePermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getViewPermissionMask()).isEqualTo(AUTHORISED_USER.getMask());
        assertThat(userObjectIdentities.get(0).getCanView()).isTrue();
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_User4_DrugProgramA() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(user4Details);

        assertThat(userObjectIdentities).hasSize(6);
        assertThat(userObjectIdentities).extracting("identifier").containsExactly(2L, 4L, 5L, 7L, 8L, 9L);
        assertThat(userObjectIdentities).extracting("permissionMask").containsOnly(ADMINISTRATOR.getMask());
        assertThat(userObjectIdentities).extracting("viewPermissionMask").containsOnly(15);
        assertThat(userObjectIdentities).extracting("canView").containsOnly(true);
    }

    @Test
    public void shouldHaveCorrectPermissionsFor_DevTeam_DrugProgramA() {

        // given
        DrugProgramme drugProgramme = new DrugProgramme(2L);
        AcuitySidDetails owner = toUser("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        securityAclService.addGrantingAce(drugProgramme, DEVELOPMENT_TEAM.getMask(), owner.toSid());

        // when
        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(owner);

        // then
        assertThat(userObjectIdentities).extracting("canView").containsOnly(false);
    }

    @Test
    public void shouldGetSidsAceFor_User4_DrugProgramA() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        AccessControlEntry ace = securityAclService.getSidsAceForAcl(drugProgramme, user4Details.toSid());

        assertThat(ace.getPermission()).isEqualTo(AcuityCumulativePermissionsAsRoles.ADMINISTRATOR);
    }

    @Test
    public void shouldntGetSidsPermissonFor_User2_DrugProgramA() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        AccessControlEntry ace = securityAclService.getSidsAceForAcl(drugProgramme, user2Details.toSid());

        assertThat(ace).isNull();
    }

    @Test
    public void shouldntGetSidsPermissonFor_Group1_Dataset22() {

        Dataset dataset = new AcuityDataset(22L);

        AccessControlEntry ace = securityAclService.getSidsAceForAcl(dataset, group1Details.toSid());

        assertThat(ace.getPermission()).isEqualTo(AcuityCumulativePermissionsAsRoles.AUTHORISED_USER);
    }

    @Test
    public void shouldAddAclToDatabase() {

        DrugProgramme newDrugProgramme = new DrugProgramme(200L);
        String owner = "unit_test1";
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner);

        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl).isNotNull();
        assertThat(((PrincipalSid) newDrugProgrammeAcl.getOwner()).getPrincipal()).isEqualTo(owner);
    }

    @Test
    public void shouldAddRevokingAceToAclToDatabase() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(201L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());

        // when
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);

        AccessControlEntry entry = newDrugProgrammeAcl.getEntries().get(0);
        assertThat(entry.getSid()).isEqualTo(owner);
        assertThat(entry.isGranting()).isFalse();
        assertThat(entry.getPermission()).isEqualTo(VIEW_VISUALISATIONS);
    }

    @Test
    public void shouldAddRevokingAceToAclToDatabaseForGroup() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(201L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        GrantedAuthoritySid addingGA = new GrantedAuthoritySid("GA");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());

        // when
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);

        AccessControlEntry entry = newDrugProgrammeAcl.getEntries().get(0);
        assertThat(entry.getSid()).isEqualTo(addingGA);
        assertThat(entry.isGranting()).isFalse();
        assertThat(entry.getPermission()).isEqualTo(VIEW_VISUALISATIONS);
    }

    @Test
    public void shouldRemoveRevokingAceFromAclToDatabase() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(212L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // when
        securityAclService.removeRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();

        // given        
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // when
        securityAclService.removeAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveRevokingAceFromAclToDatabaseForGroup() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(212L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        GrantedAuthoritySid addingGA = new GrantedAuthoritySid("GA");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // when
        securityAclService.removeRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();

        // given        
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // when
        securityAclService.removeAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveRevokingAceFromAclToDatabaseButLeaveOthersIn() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(213L);
        PrincipalSid authoriser = new PrincipalSid("unit_test2");
        PrincipalSid otherAuthoriser = new PrincipalSid("unit_test3");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entries
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authoriser.getPrincipal());
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), otherAuthoriser);

        // when
        securityAclService.removeRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);

        // when
        securityAclService.removeGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), otherAuthoriser);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveRevokingAceFromAclToDatabaseButLeaveOthersIn2() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(213L);
        PrincipalSid authoriser = new PrincipalSid("unit_test2");
        PrincipalSid otherAuthoriser= new PrincipalSid("unit_test3");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entries
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authoriser.getPrincipal());
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), otherAuthoriser);

        // when
        securityAclService.removeAceWithGranting(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser, false);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);

        // givem
        securityAclService.addRevokingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);

        // when
        securityAclService.removeAce(newDrugProgramme, AUTHORISERS.getMask(), otherAuthoriser);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);
    }

    @Test
    public void shouldAddAceToAclToDatabase() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(201L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());

        // when
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);

        AccessControlEntry entry = newDrugProgrammeAcl.getEntries().get(0);
        assertThat(entry.getSid()).isEqualTo(owner);
        assertThat(entry.getPermission()).isEqualTo(VIEW_VISUALISATIONS);
        assertThat(entry.isGranting()).isTrue();
    }

    @Test
    public void shouldAddAceToAclToDatabaseForGroup() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(201L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        GrantedAuthoritySid addingGA = new GrantedAuthoritySid("GA");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());

        // when
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);

        AccessControlEntry entry = newDrugProgrammeAcl.getEntries().get(0);
        assertThat(entry.getSid()).isEqualTo(addingGA);
        assertThat(entry.getPermission()).isEqualTo(VIEW_VISUALISATIONS);
        assertThat(entry.isGranting()).isTrue();
    }

    @Test
    public void shouldAddAceToAclToDatabaseWithParent() {

        // given
        DrugProgramme existingDrugProgramme = new DrugProgramme(2L);

        ClinicalStudy newClinicalStudy = new ClinicalStudy(220L);
        newClinicalStudy.setParent(existingDrugProgramme);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        MutableAcl acl = securityAclService.createAcl(newClinicalStudy, owner.getPrincipal());

        // when
        securityAclService.addGrantingAce(newClinicalStudy, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        Acl newClinicalStudyAcl = securityAclService.find(newClinicalStudy);

        AccessControlEntry entry = newClinicalStudyAcl.getEntries().get(0);
        assertThat(entry.getSid()).isEqualTo(owner);
        assertThat(entry.getPermission()).isEqualTo(VIEW_VISUALISATIONS);
        assertThat(entry.isGranting()).isTrue();
        assertThat(newClinicalStudyAcl.getParentAcl().getObjectIdentity().getIdentifier()).isEqualTo(2L);
    }

    @Test
    public void shouldRemoveAceFromAclToDatabase() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(212L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // when
        securityAclService.removeGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveAceFromAclToDatabaseForGroup() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(212L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        GrantedAuthoritySid addingGA = new GrantedAuthoritySid("GA");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // when
        securityAclService.removeGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), addingGA);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveAceFromAclToDatabaseButLeaveOthersIn() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(213L);
        PrincipalSid authorisedUser = new PrincipalSid("unit_test2");
        PrincipalSid otherAuthorisedUser = new PrincipalSid("unit_test3");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entries
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authorisedUser.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authorisedUser);
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISED_USER.getMask(), otherAuthorisedUser);

        // when
        securityAclService.removeGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authorisedUser);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);

        // when
        securityAclService.removeGrantingAce(newDrugProgramme, AUTHORISED_USER.getMask(), otherAuthorisedUser);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveAcesFromAclToDatabase() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(202L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);
        securityAclService.addGrantingAce(newDrugProgramme, EDIT_AUTHORISED_USERS.getMask(), owner);

        // when
        securityAclService.removeAces(newDrugProgramme, owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).isEmpty();
    }

    @Test
    public void shouldRemoveAcesFromAclToDatabaseAndLeaveOthersUsers() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(222L);
        PrincipalSid owner = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);
        securityAclService.addRevokingAce(newDrugProgramme, EDIT_AUTHORISED_USERS.getMask(), owner);
        securityAclService.addGrantingAce(newDrugProgramme, EDIT_AUTHORISED_USERS.getMask(), new GrantedAuthoritySid("Do not remove user"));

        // when
        securityAclService.removeAces(newDrugProgramme, owner);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);
    }

    @Test
    public void shouldRemoveAllAcesForDataOwnerFromAclToDatabaseAndLeaveOthersPermissionMasks() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(222L);
        PrincipalSid authoriser = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authoriser.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), authoriser);
        securityAclService.addRevokingAce(newDrugProgramme, AUTHORISERS.getMask(), new GrantedAuthoritySid("other1"));
        securityAclService.addGrantingAce(newDrugProgramme, EDIT_AUTHORISED_USERS.getMask(), new PrincipalSid("other"));

        // when
        securityAclService.removeAllAces(newDrugProgramme, AUTHORISERS.getMask());

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(2);
    }

    @Test
    public void shouldRemoveAllAcesForDataOwnerFromAclToDatabaseAndLeaveOthersPermissionMasks2() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(222L);
        PrincipalSid authoriser = new PrincipalSid("unit_test2");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authoriser.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), new PrincipalSid("delete"));
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), authoriser);
        securityAclService.addGrantingAce(newDrugProgramme, AUTHORISERS.getMask(), new PrincipalSid("delete"));
        securityAclService.addRevokingAce(newDrugProgramme, AUTHORISERS.getMask(), new GrantedAuthoritySid("delete 2"));

        // when
        securityAclService.removeAllAces(newDrugProgramme, AUTHORISERS.getMask());

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);
        assertThat(newDrugProgrammeAcl.getEntries().get(0).getSid()).isEqualTo(authoriser);
    }

    @Test
    public void shouldReplaceAcesFromAclToDatabaseAndLeaveOthersPermissionMasks() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(222L);
        PrincipalSid authoriser = new PrincipalSid("owner");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, authoriser.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), new PrincipalSid("other"));
        securityAclService.addGrantingAce(newDrugProgramme, ADMINISTRATOR.getMask(), authoriser);

        // when
        securityAclService.replaceAce(newDrugProgramme, AUTHORISERS.getMask(), authoriser, true);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(2);
        assertThat(newDrugProgrammeAcl.getEntries().get(1).getSid()).isEqualTo(authoriser);
        assertThat(newDrugProgrammeAcl.getEntries().get(1).isGranting()).isTrue();
        assertThat(newDrugProgrammeAcl.getEntries().get(1).getPermission().getMask()).isEqualTo(AUTHORISERS.getMask());
    }

    @Test
    public void shouldSwapGrantingAceFromAclToDatabaseAndLeaveOthersPermissionMasks() {

        // given
        DrugProgramme newDrugProgramme = new DrugProgramme(222L);
        PrincipalSid owner = new PrincipalSid("owner");
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // create entry
        MutableAcl acl = securityAclService.createAcl(newDrugProgramme, owner.getPrincipal());
        securityAclService.addGrantingAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner);

        // when
        securityAclService.replaceAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner, false);

        // then
        Acl newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);
        assertThat(newDrugProgrammeAcl.getEntries().get(0).getSid()).isEqualTo(owner);
        assertThat(newDrugProgrammeAcl.getEntries().get(0).isGranting()).isFalse();
        assertThat(newDrugProgrammeAcl.getEntries().get(0).getPermission().getMask()).isEqualTo(VIEW_VISUALISATIONS.getMask());

        // when
        securityAclService.replaceAce(newDrugProgramme, VIEW_VISUALISATIONS.getMask(), owner, true);

        // then
        newDrugProgrammeAcl = securityAclService.find(newDrugProgramme);
        assertThat(newDrugProgrammeAcl.getEntries()).hasSize(1);
        assertThat(newDrugProgrammeAcl.getEntries().get(0).getSid()).isEqualTo(owner);
        assertThat(newDrugProgrammeAcl.getEntries().get(0).isGranting()).isTrue();
        assertThat(newDrugProgrammeAcl.getEntries().get(0).getPermission().getMask()).isEqualTo(VIEW_VISUALISATIONS.getMask());
    }

    @Test
    public void shouldGetDrugProgrammeUserInfo() {
        int usersAmount = securityAclService.getGrantedUsersAmountForDrugProgramme(new DrugProgramme(2L));
        assertThat(usersAmount).isEqualTo(3);
    }

    @Test
    public void shouldListDrugProgrammeUsers() {

        List<AcuitySidDetailsWithPermissionMask> users = securityAclService.getGrantedUsersForAcl(new DrugProgramme(2L));

        int usersCount = (int) users.stream().filter(u -> u.getPermissionMask() == AUTHORISED_USER.getMask()).count();
        int authorisorsCount = (int) users.stream().filter(u -> u.getPermissionMask() == AUTHORISERS.getMask()).count();
        int administratorsCount = (int) users.stream().filter(u -> u.getPermissionMask() == ADMINISTRATOR.getMask()).count();

        assertThat(authorisorsCount).isEqualTo(1);
        assertThat(administratorsCount).isEqualTo(1);
        assertThat(usersCount).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetNothingForNoneExistentDrugProgramme() {

        securityAclService.getGrantedUsersAmountForDrugProgramme(new DrugProgramme(1111L));
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetNothingForNoneExistentDrugProgrammeWithClinicalStudyId() {

        securityAclService.getGrantedUsersAmountForDrugProgramme(new DrugProgramme(3111L));
    }

    @Test
    public void shouldListClinicalStudyUsers() {

        List<AcuitySidDetailsWithPermissionMask> users = securityAclService.getGrantedUsersForAcl(new ClinicalStudy(4L));

        int usersCount = (int) users.stream().filter(u -> u.getPermissionMask() == AUTHORISED_USER.getMask()).count();
        int authorisorsCount = (int) users.stream().filter(u -> u.getPermissionMask() == AUTHORISERS.getMask()).count();
        int administratorsCount = (int) users.stream().filter(u -> u.getPermissionMask() == ADMINISTRATOR.getMask()).count();

        assertThat(authorisorsCount).isEqualTo(1);
        assertThat(administratorsCount).isEqualTo(1);
        assertThat(usersCount).isEqualTo(2);
    }

    @Test
    public void shouldGetDatasetUserInfo() {
        int usersAmount = securityAclService.getGrantedUsersAmountForDataset(new AcuityDataset(10L));
        assertThat(usersAmount).isEqualTo(1);
    }

    @Test
    public void shouldListDatasetUsers() {

        List<AcuitySidDetailsWithPermissionMask> users = securityAclService.getGrantedUsersForAcl(new AcuityDataset(10L));

        int usersCount = (int) users.stream().filter(u -> u.getPermissionMask() == AUTHORISED_USER.getMask()).count();
        int administratorsCount = (int) users.stream().filter(u -> u.getPermissionMask() == ADMINISTRATOR.getMask()).count();

        assertThat(administratorsCount).isEqualTo(0);
        assertThat(usersCount).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetNothingForNoneExistentDataset() {

        securityAclService.getGrantedUsersAmountForDataset(new AcuityDataset(322L));
    }

    ///////////////////////////////////////
    ///  get all permissions           ////
    ///////////////////////////////////////
    @Test
    public void shouldGetAllThePermissionsFor_DrugProgramme2() {

        List<AcuitySidDetailsWithPermissionMaskAndGranted> allPermissionForAcl = securityAclService.getAllPermissionForAcl(new DrugProgramme(2L));

        assertThat(allPermissionForAcl).hasSize(3);
        assertThat(allPermissionForAcl).extracting("user.userId").containsOnly("User1", "User4", "User5");
        assertThat(allPermissionForAcl).extracting("user.fullName").containsOnly("Fullname user1", "Fullname user4", "Fullname user5");
        assertThat(allPermissionForAcl).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask(), ADMINISTRATOR.getMask(), AUTHORISERS.getMask());
        assertThat(allPermissionForAcl).extracting("granted").containsOnly(true);
    }

    @Test
    public void shouldGetAllThePermissionsFor_ClinicalStudy4() {

        List<AcuitySidDetailsWithPermissionMaskAndGranted> allPermissionForAcl = securityAclService.getAllPermissionForAcl(new ClinicalStudy(4L));

        assertThat(allPermissionForAcl).hasSize(1);
        assertThat(allPermissionForAcl).extracting("user.userId").containsOnly("User2");
        assertThat(allPermissionForAcl).extracting("user.fullName").containsOnly("Fullname user2");
        assertThat(allPermissionForAcl).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask());
        assertThat(allPermissionForAcl).extracting("granted").containsOnly(true);
    }

    @Test
    public void shouldGetAllThePermissionsFor_Dataset10_WithoutGettingThe_isGranted_False() {

        List<AcuitySidDetailsWithPermissionMaskAndGranted> allPermissionForAcl = securityAclService.getAllPermissionForAcl(new AcuityDataset(10L));

        assertThat(allPermissionForAcl).hasSize(2);
        assertThat(allPermissionForAcl).extracting("user.userId").contains("User3");
        assertThat(allPermissionForAcl).extracting("user.fullName").contains("Fullname user3");
        assertThat(allPermissionForAcl).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask());
        assertThat(allPermissionForAcl).extracting("granted").containsOnly(false, true);
    }

    @Test
    public void shouldGetAllThePermissionsFor_Dataset21_WithGroups() {

        List<AcuitySidDetailsWithPermissionMaskAndGranted> allPermissionForAcl = securityAclService.getAllPermissionForAcl(new AcuityDataset(22L));

        assertThat(allPermissionForAcl).hasSize(2);
        assertThat(allPermissionForAcl).extracting("user.userId").contains("User1", "Group1");
        assertThat(allPermissionForAcl).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask(), VIEW_BASE_PACKAGE.getMask());
        assertThat(allPermissionForAcl).extracting("granted").containsOnly(true, true);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionFor_InvalidDrugProgramme() {

        securityAclService.getAllPermissionForAcl(new DrugProgramme(212121L));
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotFind_InvalidClinicalStudy() {

        securityAclService.find(new ClinicalStudy(121214L));
    }

    @Test
    public void shouldRemoveAllPermissionsForAUser() {

        AcuitySidDetails user = AcuitySidDetails.toUser("User1");
        DrugProgramme newDrugProgramme = new DrugProgramme(2L);
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));

        // check has permissions beforehand
        assertThat(securityAclService.getUserObjectIdentities(user1Details)).isNotEmpty();
        assertThat(securityAclService.getAllPermissionForAcl(newDrugProgramme)).extracting("user.userId").contains(user.getSidAsString());

        securityAclService.removeAllAcesForSid(user.getSidAsString());

        assertThat(securityAclService.getUserObjectIdentities(user1Details)).isEmpty();
        assertThat(securityAclService.getAllPermissionForAcl(newDrugProgramme)).extracting("user.userId").doesNotContain(user.getSidAsString());
    }

    @Test
    public void shouldSetLockdownstatus() {
        boolean worked = securityAclService.setLockdownStatus(new DetectDataset(50L), false);

        assertThat(worked).isTrue();
    }

    @Test
    public void shouldSetLockdownstatusFull() {

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));
        DetectDataset detectDataset = new DetectDataset(50L);
        boolean permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());

        assertThat(permission).isFalse();

        boolean worked = securityAclService.setLockdownStatus(new DetectDataset(50L), false);

        assertThat(worked).isTrue();
        boolean isInLockdown = aclRepository.isLockdown(detectDataset);
        assertThat(isInLockdown).isFalse();

        permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());
        assertThat(permission).isFalse();

        worked = securityAclService.setLockdownStatus(new DetectDataset(50L), true);
        assertThat(worked).isTrue();

        isInLockdown = aclRepository.isLockdown(detectDataset);
        assertThat(isInLockdown).isTrue();

        permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());
        assertThat(permission).isFalse();
    }

    @Test
    public void shouldInheritPermissions() {

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("current_spring_user"));
        DetectDataset detectDataset = new DetectDataset(50L);
        boolean permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());

        assertThat(permission).isFalse();

        boolean worked = securityAclService.setInheritPermissions(new DetectDataset(50L), true);

        assertThat(worked).isTrue();

        permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());
        assertThat(permission).isTrue();

        worked = securityAclService.setInheritPermissions(new DetectDataset(50L), false);

        assertThat(worked).isTrue();

        permission = securityAclService.hasPermission(detectDataset, AUTHORISED_USER.getMask(), user1Details.toSids());
        assertThat(permission).isFalse();
    }

    @Test
    public void shouldGetRolePermissionForUser1() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        Permission permission = securityAclService.getRolePermissionForUser(drugProgramme, user1Details);

        assertThat(permission.getMask()).isEqualTo(AUTHORISED_USER.getMask());
    }

    @Test
    public void shouldGetRolePermissionForUser4() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        Permission permission = securityAclService.getRolePermissionForUser(drugProgramme, user4Details);

        assertThat(permission.getMask()).isEqualTo(AcuityCumulativePermissionsAsRoles.ADMINISTRATOR.getMask());
    }

    @Test
    public void shouldGetViewPermissionForUser1() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        Permission permission = securityAclService.getViewPermissionForUser(drugProgramme, user1Details);

        assertThat(permission.getMask()).isEqualTo(AUTHORISED_USER.getMask());
    }

    @Test
    public void shouldGetViewPermissionForUser4() {

        DrugProgramme drugProgramme = new DrugProgramme(2L);

        Permission permission = securityAclService.getViewPermissionForUser(drugProgramme, user4Details);

        assertThat(permission.getMask()).isEqualTo(15);
    }

    @Test
    public void shouldGetAllUsersPermissions() {

        List<DrugStudyDatasetWithPermission> userDatasetPermissions = securityAclService.getUserDatasetPermissions(user4Details);

        assertThat(userDatasetPermissions).hasSize(6);
        assertThat(userDatasetPermissions).extracting("hasPermission").contains(true, true, true, false, false, false);
        userDatasetPermissions.forEach(System.out::println);
    }
    
    @Test
    public void shouldCheckHasPermissionForStudyId() {

        AcuitySidDetails user = AcuitySidDetails.toUser("User1");
        
        Optional<AcuityObjectIdentityWithPermissionAndLockDown> roiFromStudyId = securityAclService.getRoiFromStudyId(user, "STUDY0002");
        
        System.out.println(roiFromStudyId);
        assertThat(roiFromStudyId.get().getId()).isEqualTo(9L);
        assertThat(roiFromStudyId.get().getName()).isEqualTo("ACUITY_Tolerability_STUDY0004_Studies_1_and_5");
        assertThat(roiFromStudyId.get().getViewPermissionMask()).isEqualTo(3);
        assertThat(roiFromStudyId.get().getCanView()).isTrue();
    }
    
    @Test
    public void shouldCheckHasntPermissionForStudyId() {

        AcuitySidDetails user = AcuitySidDetails.toUser("User2");
        
        Optional<AcuityObjectIdentityWithPermissionAndLockDown> roiFromStudyId = securityAclService.getRoiFromStudyId(user, "STUDY0002");

        assertThat(roiFromStudyId.isPresent()).isFalse();
    }
    
    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionCheckPermissionForStudyId() {

        AcuitySidDetails user = AcuitySidDetails.toUser("User1");
        
        securityAclService.getRoiFromStudyId(user, "invalid");
    }
}
