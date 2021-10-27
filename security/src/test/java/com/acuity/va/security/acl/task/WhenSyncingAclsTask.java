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

package com.acuity.va.security.acl.task;

import com.acuity.va.auditlogger.dao.AuditLoggerRepository;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.acuity.va.security.acl.annotation.TransactionalLoggingMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.dao.AclRepositoryImpl;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithInitialLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.service.VASecurityResourceClient;
import com.acuity.va.security.acl.service.VASecurityResourceFactory;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.DETECT;
import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.ACUITY;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalLoggingMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class WhenSyncingAclsTask {

    private static final Logger LOG = LoggerFactory.getLogger(WhenSyncingAclsTask.class);

    private final SyncAclsTask syncAclsTask = new SyncAclsTask();

    @Autowired
    private AclRepository aclObjectRepository;
    @Autowired
    private AclRepositoryImpl aclObjectRepositoryImpl;
    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    @Autowired
    private SecurityAclService securityAclService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger mockLogger;
    private VASecurityResourceClient mockClient;
    private List<AcuityObjectIdentityWithInitialLockDown> initialRois;

    @Before
    public void setup() {
        VASecurityResourceFactory mockVASecurityResourceFactory = mock(VASecurityResourceFactory.class);
        mockClient = mock(VASecurityResourceClient.class);
        when(mockVASecurityResourceFactory.getAll()).thenReturn(newArrayList(mockClient));
        mockLogger = mock(Logger.class);

        ReflectionTestUtils.setField(syncAclsTask, "aclObjectRepository", aclObjectRepository);
        ReflectionTestUtils.setField(syncAclsTask, "securityAclService", securityAclService);
        ReflectionTestUtils.setField(syncAclsTask, "vASecurityResourceFactory", mockVASecurityResourceFactory);
        ReflectionTestUtils.setField(syncAclsTask, "log", mockLogger);

        initialRois = ImmutableList.of(new DrugProgramme("STUDY0006"), new DrugProgramme("STUDY0008"));
        when(mockClient.loadRois()).thenReturn(initialRois);
        syncAclsTask.run(true);
    }

    @Test
    public void shouldInsert1RowThatHaveNoAcls() {
        DrugProgramme drugProgramme = new DrugProgramme("Test");
        addRoisToInitial(drugProgramme);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        Long findAclObjectId = aclObjectRepositoryImpl.findAclObjectId(DrugProgramme.class.getName(), drugProgramme.getId());
        assertThat(findAclObjectId).isNotNull();
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, acl_object_identity_id, default_vis FROM acl_object WHERE ID = " + findAclObjectId);
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("Test");
        assertThat((BigDecimal) resultList.get(0).get("ACL_OBJECT_IDENTITY_ID")).isEqualTo(new BigDecimal(findAclObjectId.intValue()));
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("DP");
        assertThat(resultList.get(0).get("DEFAULT_VIS")).isNull();
    }

    @Test
    public void shouldNotUpdate1Row_ForDrugProgramme() {
        DrugProgramme drugProgramme = new DrugProgramme("Updated name");
        addRoisToInitial(drugProgramme);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);
    }

    @Test
    public void shouldInsertThenUpdate1Row_ForClinicalStudy() {
        ClinicalStudy clinicalStudy = new ClinicalStudy("STUDY000111", "Name");
        clinicalStudy.setDrugProgramme("STUDY0006");
        addRoisToInitial(clinicalStudy);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, code, acl_object_identity_id FROM acl_object WHERE ID = ( select max(ID) from acl_object )");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("Name");
        assertThat(resultList.get(0).get("CODE")).isEqualTo("STUDY000111");
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("CS");

        //  clinical study now beed added change name
        ClinicalStudy updatedClinicalStudy = new ClinicalStudy("STUDY000111", "Updated name");
        updatedClinicalStudy.setDrugProgramme("STUDY0006");
        addRoisToInitial(updatedClinicalStudy);

        allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(1);

        syncAclsTask.run(true);

        allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        resultList = jdbcTemplate.queryForList("SELECT name, code, acl_object_identity_id FROM acl_object WHERE ID = ( select max(ID) from acl_object )");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("Updated name");
        assertThat(resultList.get(0).get("CODE")).isEqualTo("STUDY000111");
    }

    @Test
    public void shouldUpdate1Row_ForDataset() {
        AcuityDataset acuityDataset = new AcuityDataset(7L, "Name");
        acuityDataset.setDrugProgramme("STUDY0006");
        acuityDataset.setClinicalStudyName("STUDY0001 PhI MAD + expansion. Safety, tolerability, PK and preliminary activity updated");
        acuityDataset.setClinicalStudyCode("STUDY0001");
        addRoisToInitial(acuityDataset);

        syncAclsTask.run(true);
        acuityDataset.setName("Updated name");

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(1);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT name, parent_clinical_study, parent_clinical_study_code, acl_object_identity_id FROM acl_object WHERE acl_type = 'DS'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("Updated name");
        assertThat(resultList.get(0).get("PARENT_CLINICAL_STUDY")).isEqualTo("STUDY0001 PhI MAD + expansion. Safety, tolerability, PK and preliminary activity updated");
        assertThat(resultList.get(0).get("PARENT_CLINICAL_STUDY_CODE")).isEqualTo("STUDY0001");
    }

    @Test
    public void shouldInsert1Row_ForDataset() {
        AcuityDataset acuityDataset = new AcuityDataset(17L, "inserted name");
        acuityDataset.setDrugProgramme("STUDY0006");
        acuityDataset.setClinicalStudyName("CS");
        acuityDataset.setClinicalStudyCode("CS Code");
        addRoisToInitial(acuityDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, lockdown, acl_object_identity_id, "
                + " parent_clinical_study, parent_clinical_study_code, parent_drug_programme FROM acl_object WHERE name = 'inserted name'");

        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(false);
        assertThat(resultList.get(0).get("PARENT_DRUG_PROGRAMME")).isEqualTo("STUDY0006");
        assertThat(resultList.get(0).get("PARENT_CLINICAL_STUDY")).isEqualTo("CS");
        assertThat(resultList.get(0).get("PARENT_CLINICAL_STUDY_CODE")).isEqualTo("CS Code");
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("DS");

        assertThat(auditLoggerRepository.getAllLogOperations()).isEmpty();

        MutableAcl mutableAcl = securityAclService.find(acuityDataset);

        assertThat(mutableAcl.isEntriesInheriting()).isTrue();
    }

    @Test
    public void shouldInsert1Row_ForDetectDatasetInLockdown() {
        DetectDataset detectDataset = new DetectDataset(18L, "inserted name in lockdown");
        detectDataset.setDrugProgramme("STUDY0008");
        detectDataset.setClinicalStudyCode("CS1");
        detectDataset.setClinicalStudyName("CS1 name");
        detectDataset.setLockdown(true);
        addRoisToInitial(detectDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT name, acl_object_identity_id, lockdown FROM acl_object WHERE name = 'inserted name in lockdown'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name in lockdown");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(true);

        LogOperationEntity logOperation = auditLoggerRepository.getAllLogOperations().get(0);

        assertThat(logOperation.getName()).isEqualTo("PERMISSIONS_SET_LOCKDOWN");
        assertThat(logOperation.getOwner()).isEqualTo("SYNCING ACLS JOB");
        assertThat(logOperation.getSessionId()).isEqualTo("NO SESSION");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("ACL_CLASSNAME", "ACL_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly(detectDataset.getClass().getSimpleName(), detectDataset.getId());

        MutableAcl mutableAcl = securityAclService.find(detectDataset);

        assertThat(mutableAcl.isEntriesInheriting()).isFalse();
    }

    @Test
    public void shouldInsert1Row_ForDetectDatasetNotInLockdown() {
        DetectDataset detectDataset = new DetectDataset(18L, "inserted name not in lockdown");
        detectDataset.setDrugProgramme("STUDY0008");
        detectDataset.setClinicalStudyCode("CS1");
        detectDataset.setClinicalStudyName("CS1 name");
        detectDataset.setLockdown(false);
        addRoisToInitial(detectDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT name, acl_object_identity_id, lockdown FROM acl_object WHERE name = 'inserted name not in lockdown'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name not in lockdown");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(false);

        assertThat(auditLoggerRepository.getAllLogOperations()).isEmpty();

        MutableAcl mutableAcl = securityAclService.find(detectDataset);

        assertThat(mutableAcl.isEntriesInheriting()).isFalse();
    }

    @Test
    public void shouldNotUpdateLockdownFlagInsert1Row_ForDetectDatasetNotInLockdown() {
        DetectDataset detectDataset = new DetectDataset(18L, "inserted name in lockdown2");
        detectDataset.setDrugProgramme("STUDY0008");
        detectDataset.setClinicalStudyCode("CS1");
        detectDataset.setClinicalStudyName("CS1 name");
        detectDataset.setLockdown(true);
        addRoisToInitial(detectDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT name, acl_object_identity_id, lockdown FROM acl_object WHERE name = 'inserted name in lockdown2'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name in lockdown2");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(true);

        assertThat(auditLoggerRepository.getAllLogOperations()).isNotEmpty();

        MutableAcl mutableAcl = securityAclService.find(detectDataset);

        assertThat(mutableAcl.isEntriesInheriting()).isFalse();

        // run again set lockdown false, shouldnt update
        detectDataset.setLockdown(false);

        syncAclsTask.run(true);

        allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        resultList = jdbcTemplate.queryForList("SELECT name, acl_object_identity_id, lockdown FROM acl_object WHERE name = 'inserted name in lockdown2'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name in lockdown2");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(true);
    }

    @Test
    public void shouldInsert1RowAndUsers_ForDetectDatasetInLockdown() {
        DetectDataset detectDataset = new DetectDataset(18L, "inserted name in lockdown1");
        detectDataset.setDrugProgramme("STUDY0008");
        detectDataset.setClinicalStudyCode("CS1");
        detectDataset.setClinicalStudyName("CS1 name");

        AcuityObjectIdentityImpl roi = detectDataset;
        roi.setLockdown(true);
        addRoisToInitial(roi);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT name, acl_object_identity_id, lockdown FROM acl_object WHERE name = 'inserted name in lockdown1'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name in lockdown1");
        assertThat(resultList.get(0).get("LOCKDOWN")).isEqualTo(true);

        MutableAcl mutableAcl = securityAclService.find(detectDataset);

        assertThat(mutableAcl.isEntriesInheriting()).isFalse();
        assertThat(mutableAcl.getEntries()).hasSize(1);

        LogOperationEntity logOperation = auditLoggerRepository.getAllLogOperations().get(0);

        assertThat(logOperation.getName()).isEqualTo("PERMISSIONS_SET_LOCKDOWN");
        assertThat(logOperation.getOwner()).isEqualTo("SYNCING ACLS JOB");
        assertThat(logOperation.getSessionId()).isEqualTo("NO SESSION");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("ACL_CLASSNAME", "ACL_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly(roi.getClass().getSimpleName(), roi.getId());
    }

    @Test
    public void shouldntUpdate1Row_ForDatasetNoParent() {
        AcuityDataset acuityDataset = new AcuityDataset(35L, "Updated name");
        acuityDataset.setDrugProgramme("random");
        acuityDataset.setClinicalStudyCode("CS1");
        acuityDataset.setClinicalStudyName("CS1 name");
        addRoisToInitial(acuityDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        verify(mockLogger, times(1)).error(anyString());
    }

    @Test
    public void shouldUpdateAcuityClinicalStudy() {
        ClinicalStudy acuityClinicalStudy = new ClinicalStudy("D4620C11", "acuity name");
        acuityClinicalStudy.setDrugProgramme("STUDY0006");
        acuityClinicalStudy.setOrigin(ACUITY);
        ClinicalStudy detectClinicalStudy = new ClinicalStudy("D4620C11", "detect name");
        detectClinicalStudy.setDrugProgramme("STUDY0006");
        detectClinicalStudy.setOrigin(DETECT);
        addRoisToInitial(acuityClinicalStudy, detectClinicalStudy);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(1);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, code, acl_object_identity_id FROM acl_object WHERE ID = ( select max(ID) from acl_object )");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("acuity name");
        assertThat(resultList.get(0).get("CODE")).isEqualTo("D4620C11");
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("CS");
    }

    @Test
    public void shouldUpdateBothClinicalStudy() {
        ClinicalStudy acuityClinicalStudy = new ClinicalStudy("D4620C11", "acuity name");
        acuityClinicalStudy.setDrugProgramme("STUDY0006");
        acuityClinicalStudy.setOrigin(ACUITY);
        ClinicalStudy detectClinicalStudy = new ClinicalStudy("D4620C12", "detect name");
        detectClinicalStudy.setDrugProgramme("STUDY0006");
        detectClinicalStudy.setOrigin(DETECT);
        addRoisToInitial(acuityClinicalStudy, detectClinicalStudy);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(2);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, code, acl_object_identity_id FROM acl_object WHERE ID = ( select max(ID) from acl_object )");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("detect name");
        assertThat(resultList.get(0).get("CODE")).isEqualTo("D4620C12");
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("CS");
    }

    @Test
    public void shouldUpdateBothDatasetFromAcuityClinicalStudy() {
        ClinicalStudy acuityClinicalStudy = new ClinicalStudy("DC123", "acuity name");
        acuityClinicalStudy.setDrugProgramme("STUDY0006");
        acuityClinicalStudy.setOrigin(ACUITY);
        ClinicalStudy detectClinicalStudy = new ClinicalStudy("DC123", "detect name");
        detectClinicalStudy.setDrugProgramme("STUDY0006");
        detectClinicalStudy.setOrigin(DETECT);
        DetectDataset detectDataset = new DetectDataset(130L, "inserted name for swapping");
        detectDataset.setDrugProgramme("STUDY0006");
        detectDataset.setClinicalStudyCode("DC123");
        detectDataset.setClinicalStudyName("CS1 name");
        addRoisToInitial(acuityClinicalStudy, detectClinicalStudy, detectDataset);

        int allAclsBefore = getAddedAclCount();
        assertThat(allAclsBefore).isEqualTo(0);

        syncAclsTask.run(true);

        int allAcls = getAddedAclCount();
        assertThat(allAcls).isEqualTo(2);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT acl_type, name, code, parent_clinical_study, parent_clinical_study_code"
                + " FROM acl_object WHERE name = 'inserted name for swapping'");
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).get("NAME")).isEqualTo("inserted name for swapping");
        assertThat(resultList.get(0).get("parent_clinical_study_code")).isEqualTo("DC123");
        assertThat(resultList.get(0).get("parent_clinical_study")).isEqualTo("CS1 name");
        assertThat(resultList.get(0).get("ACL_TYPE")).isEqualTo("DS");
    }

    private void addRoisToInitial(AcuityObjectIdentityWithInitialLockDown... elements) {
        List<AcuityObjectIdentityWithInitialLockDown> rois = newArrayList(elements);
        rois.addAll(initialRois);
        when(mockClient.loadRois()).thenReturn(rois);
    }

    private int getAddedAclCount() {
        List<AcuityObjectIdentityWithPermissionAndLockDown> allObjectIdentities = aclObjectRepository.listObjectIdentities();
        int allAclCount = (int) allObjectIdentities.stream()
                .map(o -> securityAclService.find(o))
                .map(mutableAcl -> assertThat(mutableAcl).isNotNull())
                .count();
        LOG.debug("AcuityObjectIdentities " + allObjectIdentities.size() + " , with Acls " + allAclCount);
        return allAclCount - initialRois.size();
    }
}
