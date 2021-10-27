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

package com.acuity.va.security.acl.dao;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.domain.AclRemoteLocation;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.SidWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.service.AuthenicationToken;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUserFromGroup;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_BASE_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.filter;

/**
 * @author Glen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenQueryingAclRepository {

    @Autowired
    AclRepository aclRepository;
    @Autowired
    AclRepositoryImpl aclObjectRepositoryImpl;
    @Autowired
    SecurityAclService securityAclService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void shouldListAll_10_AclObjects() {

        List<AcuityObjectIdentityWithPermissionAndLockDown> objectIdentities = aclRepository.listObjectIdentities();

        assertThat(objectIdentities).hasSize(12);
        assertThat(objectIdentities.get(0)).isInstanceOfAny(AcuityDataset.class, DrugProgramme.class, ClinicalStudy.class);
        assertThat(objectIdentities).extracting("name").isNotEmpty().isNotNull();
        assertThat(objectIdentities).extracting("id").isNotEmpty().isNotNull();

        // DrugProgramme should have null compound, AcuityDataset shouldnt be null
        assertThat(filter(objectIdentities).with("type").equalsTo(AcuityDataset.class.getName()).get()).extracting("clinicalStudyName").doesNotContainNull();
        assertThat(filter(objectIdentities).with("type").equalsTo(AcuityDataset.class.getName()).get()).extracting("clinicalStudyCode").doesNotContainNull();

        assertThat(filter(objectIdentities).with("type").equalsTo(AcuityDataset.class.getName()).get()).extracting("drugProgramme").doesNotContainNull();
        assertThat(filter(objectIdentities).with("type").equalsTo(ClinicalStudy.class.getName()).get()).extracting("drugProgramme").doesNotContainNull();

        assertThat(objectIdentities).extracting("type").isNotEmpty().isNotNull().
                containsOnly(DrugProgramme.class.getName(), DrugProgramme.class.getName(), ClinicalStudy.class.getName(), AcuityDataset.class.getName(), DetectDataset.class.getName());

        assertThat(objectIdentities).extracting("lockdown").contains(true, false);
        assertThat(objectIdentities).extracting("inherited").contains(true, false);
        objectIdentities.forEach(out::println);
    }

    @Test
    public void shouldListUsersBy_READ_PermissionForDrugProgrammes() {

        List<String> users = aclRepository.listUsersByPermissionMask(DrugProgramme.class.getName(), AUTHORISED_USER.getMask());

        assertThat(users).hasSize(1);
        assertThat(users).containsExactly("User1");
    }

    @Test
    public void shouldListUsersBy_READ_PermissionForClinicalStudies() {

        List<String> users = aclRepository.listUsersByPermissionMask(ClinicalStudy.class.getName(), AUTHORISED_USER.getMask());

        assertThat(users).hasSize(1);
        assertThat(users).containsExactly("User2");
    }

    @Test
    public void shouldListUsersBy_READ_PermissionForDataset() {

        List<String> users = aclRepository.listUsersByPermissionMask(AcuityDataset.class.getName(), AUTHORISED_USER.getMask());

        assertThat(users).hasSize(2);
        assertThat(users).containsOnly("User3", "Group1_AUTHORITY");
    }

    @Test
    public void shouldGetDatasetGroupSidWithPermissionMaskAndGranted() {

        List<SidWithPermissionMaskAndGranted> drugProgrammeSidsPermissions
                = aclRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(AcuityDataset.class.getName(), 22L);

        assertThat(drugProgrammeSidsPermissions).hasSize(2);
        assertThat(drugProgrammeSidsPermissions).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask(), VIEW_BASE_PACKAGE.getMask());
        assertThat(drugProgrammeSidsPermissions).extracting("granted").containsOnly(true);
        assertThat(drugProgrammeSidsPermissions).extracting("sid").contains("Group1_AUTHORITY");
        assertThat(drugProgrammeSidsPermissions).extracting("isuser").containsOnly(true, false);
    }

    @Test
    public void shouldGetDatasetUserSidWithPermissionMaskAndGranted() {

        List<SidWithPermissionMaskAndGranted> drugProgrammeSidsPermissions
                = aclRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(AcuityDataset.class.getName(), 7L);

        assertThat(drugProgrammeSidsPermissions).hasSize(1);
        assertThat(drugProgrammeSidsPermissions).extracting("permissionMask").containsOnly(AUTHORISED_USER.getMask());
        assertThat(drugProgrammeSidsPermissions).extracting("granted").containsOnly(false);
        assertThat(drugProgrammeSidsPermissions).extracting("sid").containsOnly("User2");
        assertThat(drugProgrammeSidsPermissions).extracting("isuser").containsOnly(true);
    }

    @Test
    public void shouldGetDatasetParentDrugProgrammeFromDB() {

        AcuityObjectIdentity drugProgramme = aclRepository.getDatasetParentDrugProgrammeFromDB(new AcuityDataset(7L));

        assertThat(drugProgramme.getType()).isEqualTo(DrugProgramme.class.getName());
        assertThat(drugProgramme.getId()).isEqualTo(2L);
    }

    @Test
    public void shouldGetDatasetParentDrugProgrammeFromObject() {

        AcuityDataset acuityDataset = new AcuityDataset(7L);
        acuityDataset.setDrugProgramme("STUDY0006");

        AcuityObjectIdentity drugProgramme = aclRepository.getDatasetParentDrugProgrammeFromObj(acuityDataset);

        assertThat(drugProgramme.getType()).isEqualTo(DrugProgramme.class.getName());
        assertThat(drugProgramme.getId()).isEqualTo(2L);
    }

    @Test
    public void shouldReturnNullForInvalidDataset_GetParentDrugProgrammeFromDB() {

        AcuityObjectIdentity drugProgramme = aclRepository.getDatasetParentDrugProgrammeFromDB(new AcuityDataset(4L));

        assertThat(drugProgramme).isNull();
    }

    @Test
    public void shouldReturnNullForInvalidDataset_GetParentDrugProgrammeFromObject() {

        AcuityDataset acuityDataset = new AcuityDataset(4L);
        acuityDataset.setDrugProgramme("STUDY0006sdsdsd");

        AcuityObjectIdentity drugProgramme = aclRepository.getDatasetParentDrugProgrammeFromObj(acuityDataset);

        assertThat(drugProgramme).isNull();
    }

    @Test
    public void shouldGetClinicalStudyParentDrugProgrammeFromDB() {

        AcuityObjectIdentity drugProgramme = aclRepository.getClinicalStudyParentDrugProgrammeFromDB(new ClinicalStudy(4L));

        assertThat(drugProgramme.getType()).isEqualTo(DrugProgramme.class.getName());
        assertThat(drugProgramme.getId()).isEqualTo(2L);
    }

    @Test
    public void shouldGetClinicalStudyParentDrugProgrammeFromObject() {

        ClinicalStudy acuityClinicalStudy = new ClinicalStudy(4L);
        acuityClinicalStudy.setDrugProgramme("STUDY0006");

        AcuityObjectIdentity drugProgramme = aclRepository.getClinicalStudyParentDrugProgrammeFromObj(acuityClinicalStudy);

        assertThat(drugProgramme.getType()).isEqualTo(DrugProgramme.class.getName());
        assertThat(drugProgramme.getId()).isEqualTo(2L);
    }

    @Test
    public void shouldReturnNullForInvalidClinicalStudy_GetParentDrugProgrammeFromDB() {

        AcuityObjectIdentity drugProgramme = aclRepository.getClinicalStudyParentDrugProgrammeFromDB(new ClinicalStudy(121214L));

        assertThat(drugProgramme).isNull();
    }

    @Test
    public void shouldReturnNullForInvalidClinicalStudy_GetParentDrugProgrammeFromObject() {
        ClinicalStudy acuityClinicalStudy = new ClinicalStudy(121214L);
        acuityClinicalStudy.setDrugProgramme("STUDY0006dfdfdf");

        AcuityObjectIdentity drugProgramme = aclRepository.getClinicalStudyParentDrugProgrammeFromObj(acuityClinicalStudy);

        assertThat(drugProgramme).isNull();
    }

    @Test
    public void shouldGetDatasetNameFromId() {

        Long id = 9L;

        String name = aclRepository.getAclName(AcuityDataset.class.getName(), id);

        assertThat(name).isEqualTo("ACUITY_Tolerability_STUDY0004_Studies_1_and_5");
    }

    @Test
    public void shouldListAcls() {

        List<Map<String, Object>> listAcls = aclRepository.listAcls();

        assertThat(listAcls).hasSize(12);
    }

    @Test
    public void shouldRemoveAllAcesForSid() {

        AcuitySidDetails rsd = AcuitySidDetails.toUser("User1");

        aclRepository.removeAllAcesForSid(rsd.getName());

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(rsd);

        assertThat(userObjectIdentities).isEmpty();
    }

    @Test
    public void shouldListAllAclRemoteLocations() {

        List<AclRemoteLocation> listAllRemoteAclsLocations = aclRepository.listAllRemoteAclsLocations();

        assertThat(listAllRemoteAclsLocations).hasSize(1);
        assertThat(listAllRemoteAclsLocations).extracting("name").containsOnly("vahub");
        assertThat(listAllRemoteAclsLocations).extracting("baseUrl").containsOnly("http://localhost:9090/resources/security");
        assertThat(listAllRemoteAclsLocations).extracting("enabled").containsOnly(true);
    }

    @Test
    public void shouldListOnlyEnabledAclRemoteLocations() {

        jdbcTemplate.execute("UPDATE acl_remote SET enabled = 0 WHERE name != 'vahub'");

        List<AclRemoteLocation> listAllRemoteAclsLocations = aclRepository.listAllRemoteAclsLocations();

        assertThat(listAllRemoteAclsLocations).hasSize(1);
        assertThat(listAllRemoteAclsLocations).extracting("name").containsOnly("vahub");
        assertThat(listAllRemoteAclsLocations).extracting("baseUrl").containsOnly("http://localhost:9090/resources/security");
        assertThat(listAllRemoteAclsLocations).extracting("enabled").containsOnly(true);
    }

    @Test
    public void shouldToggleLockdown() {

        Long findAclObjectId = aclObjectRepositoryImpl.findAclObjectId(DetectDataset.class.getName(), 50L);
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + findAclObjectId);

        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("lockdown")).isEqualTo(true);

        boolean worked = aclRepository.setLockdownStatus(new DetectDataset(50L), false);

        resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + findAclObjectId);

        assertThat(worked).isTrue();
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("lockdown")).isEqualTo(false);

        worked = aclRepository.setLockdownStatus(new DetectDataset(50L), true);

        resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + findAclObjectId);

        assertThat(worked).isTrue();
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("lockdown")).isEqualTo(true);
    }

    @Test
    public void shouldNotUpdateLockdown() {
        DetectDataset detectDataset = new DetectDataset(50L, "Detect_Lockdown");
        detectDataset.setDrugProgramme("STUDY0007");
        detectDataset.setLockdown(false);

        int updated = aclRepository.insertOrUpdateAclObject(110L, detectDataset);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + 110L);

        assertThat(resultList.get(0).get("lockdown")).isEqualTo(true);
    }

    //@Test
    public void shouldOnlyInsertLockdownNotUpdate() {
        DetectDataset detectDataset = new DetectDataset(501L, "Detect_Lockdown22221");
        detectDataset.setDrugProgramme("STUDY0007");
        detectDataset.setLockdown(false);
        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        MutableAcl createdAcl = securityAclService.createAcl(detectDataset, "owner");
        int updated = aclRepository.insertOrUpdateAclObject((Long) createdAcl.getId(), detectDataset);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + createdAcl.getId());

        assertThat(resultList.get(0).get("lockdown")).isEqualTo(false);

        detectDataset.setLockdown(true);
        updated = aclRepository.insertOrUpdateAclObject((Long) createdAcl.getId(), detectDataset);

        resultList = jdbcTemplate.queryForList("SELECT lockdown FROM acl_object WHERE ID = " + createdAcl.getId());

        assertThat(resultList.get(0).get("lockdown")).isEqualTo(false);
    }

    @Test
    public void shouldListGroupsInLockdown() {
        DetectDataset detectDataset = new DetectDataset(50L, "Detect_Lockdown2");

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        securityAclService.setLockdownStatus(detectDataset, true);
        securityAclService.addAce(detectDataset, VIEW_VISUALISATIONS.getMask(), toUserFromGroup("TEST").toSid(), true);

        List<String> listGroupsInLockdown = aclRepository.listGroupsInLockdown();

        assertThat(listGroupsInLockdown).containsOnly("TEST");
    }

    @Test
    public void shouldListGroupWithItsLockdownDatasets() {
        DetectDataset detectDataset = new DetectDataset(50L, "Detect_Lockdown");

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        securityAclService.setLockdownStatus(detectDataset, true);
        securityAclService.addAce(detectDataset, VIEW_VISUALISATIONS.getMask(), toUserFromGroup("TEST").toSid(), true);

        List<GroupWithItsLockdownDatasets> listGroupsInLockdown = aclRepository.listGroupWithItsLockdownDatasets();

        assertThat(listGroupsInLockdown).extracting("groupName").containsOnly("TEST");
        assertThat(listGroupsInLockdown).flatExtracting("datasetsInLockdown").containsOnly("Detect_Lockdown");
    }

    @Test
    public void shouldfindVasecurityIdFromStudyId() {
        String studyId = "STUDY0002";

        Long vasecurityIdFromStudyId = aclRepository.findVasecurityIdFromStudyId(studyId);

        assertThat(vasecurityIdFromStudyId).isEqualTo(9L);
    }

    @Test
    public void shouldnotfindVasecurityIdFromStudyId() {
        String studyId = "invalid";

        Long vasecurityIdFromStudyId = aclRepository.findVasecurityIdFromStudyId(studyId);

        assertThat(vasecurityIdFromStudyId).isNull();
    }
}
