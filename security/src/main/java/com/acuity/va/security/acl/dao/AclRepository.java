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

import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.acuity.va.auditlogger.service.AuditLoggerService;
import com.acuity.va.security.acl.domain.AclObject;
import com.acuity.va.security.acl.domain.AclRemoteLocation;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugStudyDataset;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithInitialLockDown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.SidWithPermissionMaskAndGranted;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.acuity.va.security.common.Constants.HOURLY_REFRESHABLE_CACHE;
import static com.acuity.va.security.common.Constants.REFRESHABLE_CACHE;
import static java.util.stream.Collectors.toList;

/**
 * Repository for Acl queries.
 *
 * @author Glen
 */
@Component
public class AclRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AclRepository.class);

    @Autowired
    private AclRepositoryImpl aclRepositoryImpl;

    @Autowired(required = false)
    private AuditLoggerService auditLoggerService;

    /**
     * Caches starting with REFRESHABLE_CACHE are automatically refreshed with a scheduled task
     */
    @Cacheable(HOURLY_REFRESHABLE_CACHE + "AclRepository-listObjectIdentities")
    public List<AcuityObjectIdentityWithPermissionAndLockDown> listObjectIdentities() {
        return aclRepositoryImpl.listObjectIdentities();
    }

    public List<AcuityObjectIdentityWithPermissionAndLockDown> listObjectIdentitiesNotCached() {
        return aclRepositoryImpl.listObjectIdentities();
    }

    @Async
    public void generateListObjectIdentitiesCache() {
        listObjectIdentities();
    }

    @CacheEvict(HOURLY_REFRESHABLE_CACHE + "AclRepository-listObjectIdentities")
    public void removeAllObjectIdentities() {
    }

    // do not cache
    public void removeAllAcesForSid(String userId) {
        aclRepositoryImpl.removeAllAcesForSid(userId);
    }

    /**
     * Gets the parent drug programme from the Dataset id and type, and uses that to lookup the drug programme in the db and check it exists and gets it
     */
    @Cacheable(REFRESHABLE_CACHE + "AclRepository-getDatasetParentDrugProgrammeFromDB")
    public AcuityObjectIdentity getDatasetParentDrugProgrammeFromDB(Dataset datasetInTheDB) {
        return aclRepositoryImpl.getDatasetParentDrugProgrammeFromDB(datasetInTheDB);
    }

    /**
     * Uses the drug programme name to lookup the parent
     */
    @Cacheable(REFRESHABLE_CACHE + "AclRepository-getDatasetParentDrugProgrammeFromObj")
    public AcuityObjectIdentity getDatasetParentDrugProgrammeFromObj(Dataset datasetNotInTheDB) {
        return aclRepositoryImpl.getDatasetParentDrugProgrammeFromObj(datasetNotInTheDB);
    }

    /**
     * Uses the drug programme name to lookup the parent
     */
    @Cacheable(REFRESHABLE_CACHE + "AclRepository-getClinicalStudyParentDrugProgrammeFromDB")
    public AcuityObjectIdentity getClinicalStudyParentDrugProgrammeFromDB(ClinicalStudy clinicalStudyInTheDB) {
        return aclRepositoryImpl.getClinicalStudyParentDrugProgrammeFromDB(clinicalStudyInTheDB);
    }

    /**
     * Gets the parent drug programme from the ClinicalStudy id and type, and uses that to lookup the drug programme in the db and check it exists and gets it
     */
    @Cacheable(REFRESHABLE_CACHE + "AclRepository-getClinicalStudyParentDrugProgrammeFromObj")
    public AcuityObjectIdentity getClinicalStudyParentDrugProgrammeFromObj(ClinicalStudy clinicalStudyNotInTheDB) {
        return aclRepositoryImpl.getClinicalStudyParentDrugProgrammeFromObj(clinicalStudyNotInTheDB);
    }

    // Do not cache
    public List<String> listUsersByPermissionMask(String aclClassName, Integer permissionsAsRoleMask) {
        return aclRepositoryImpl.listUsersByPermissionMask(aclClassName, permissionsAsRoleMask);
    }

    // Do not cache
    @Deprecated
    public List<Map<String, Object>> listAcls() {
        return aclRepositoryImpl.listAcls();
    }

    // Do not cache
    public List<SidWithPermissionMaskAndGranted> getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(
            String aclClassName, Long aclObjectIdentityId) {
        return aclRepositoryImpl
                .getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(aclClassName, aclObjectIdentityId);
    }

    @Cacheable(REFRESHABLE_CACHE + "AclRepository-getAclName")
    public String getAclName(String aclClassName, Long aclObjectIdentityId) {
        return aclRepositoryImpl.getAclName(aclClassName, aclObjectIdentityId);
    }

    @Cacheable(REFRESHABLE_CACHE + "AclRepository-listDrugsStudiesInstances")
    public List<DrugStudyDataset> listDrugsStudiesInstances() {
        return aclRepositoryImpl.listDrugsStudiesInstances();
    }

    // do not cache
    public List<AclRemoteLocation> listAllRemoteAclsLocations() {
        return aclRepositoryImpl.listAllRemoteAclsLocations();
    }

    // do not cache
    public List<AclRemoteLocation> listDetectRemoteAclsLocations() {
        return aclRepositoryImpl.listDetectRemoteAclsLocations();
    }

    public Long findAclObjectId(String aclClassName, Long aclObjectIdentity) {
        return aclRepositoryImpl.findAclObjectId(aclClassName, aclObjectIdentity);
    }

// do not cache
    public int insertOrUpdateAclObject(Long roiPKId, AcuityObjectIdentityWithInitialLockDown roi) {
        Long id = aclRepositoryImpl.findAclObjectId(roi.getType(), roi.getId());
        AclObject aclObject = null;

        if (roi.thisDrugProgrammeType()) {
            aclObject = AclObject.createDP(id, roi.getName(), roiPKId);
        } else if (roi.thisClinicalStudyType()) {
            ClinicalStudy clinicalStudy = (ClinicalStudy) roi;

            if (StringUtils.isEmpty(clinicalStudy.getDrugProgramme())) {
                LOG.error("SYNC ERROR " + clinicalStudy + " must have a parent drug programme");
                return 0;
            }
            if (StringUtils.isEmpty(clinicalStudy.getCode())) {
                LOG.error("SYNC ERROR " + clinicalStudy + " must have a code");
                return 0;
            }

            aclObject = AclObject.createCS(id, clinicalStudy.getCode(),
                    clinicalStudy.getName(), roiPKId, clinicalStudy.getDrugProgramme());
        } else if (roi.thisDatasetType()) {
            Dataset dataset = (Dataset) roi;

            if (StringUtils.isEmpty(dataset.getDrugProgramme())) {
                LOG.error("SYNC ERROR " + dataset + " must have a parent drug programme");
                return 0;
            }
            if (StringUtils.isEmpty(dataset.getClinicalStudyCode())) {
                LOG.error("SYNC ERROR " + dataset + " must have a parent clinical study code");
                return 0;
            }
            if (StringUtils.isEmpty(dataset.getClinicalStudyName())) {
                LOG.error("SYNC ERROR " + dataset + " must have a parent clinical study name");
                return 0;
            }
            aclObject = AclObject.createDataset(id, dataset.getName(), roiPKId, dataset.getDrugProgramme(),
                    dataset.getClinicalStudyCode(), dataset.getClinicalStudyName());

        }

        if (id == null) {

            if (roi.thisDetectType() && roi.isLockdown()) {

                // lockdown add entry 
                // manually add set lockdown 
                LogOperationEntity logOperationEntity = new LogOperationEntity("PERMISSIONS_SET_LOCKDOWN",
                        com.acuity.va.security.acl.dao.AclRepository.class.getName(),
                        "SYNCING ACLS JOB", "NO SESSION");
                logOperationEntity.addLogArg(new LogArgEntity("ACL_CLASSNAME", roi.getClass().getSimpleName()));
                logOperationEntity.addLogArg(new LogArgEntity("ACL_ID", roi.getId()));

                if (auditLoggerService != null) {
                    auditLoggerService.save(logOperationEntity);
                }

                // if detect type, always check if lockdown 
                aclObject.setLockdown(roi.isLockdown());
            }

            return aclRepositoryImpl.insertAclObject(aclObject);
        } else if (roi.thisDatasetType() || roi.thisClinicalStudyType()) {
            return aclRepositoryImpl.updateAclObject(aclObject);
        } else {
            return 0;
        }
    }

    // do not cache
    public boolean setLockdownStatus(DetectDataset vis, boolean lockdownStatus) {
        Long id = aclRepositoryImpl.findAclObjectId(vis.getType(), vis.getId());

        if (id != null) {
            int updatedRows = aclRepositoryImpl.setLockdown(id, lockdownStatus);
            return updatedRows != 0;
        } else {
            throw new IllegalArgumentException(vis + " not found");
        }
    }

    // do not cache
    public boolean isLockdown(DetectDataset vis) {
        Long id = aclRepositoryImpl.findAclObjectId(vis.getType(), vis.getId());
        return aclRepositoryImpl.isLockdown(id);
    }

    public List<String> listGroupsInLockdown() {
        return aclRepositoryImpl.listGroupsInLockdown().stream()
                                .map(g -> AcuitySidDetails.toUserFromAuthority(g).getName())
                                .collect(toList());
    }

    public List<GroupWithItsLockdownDatasets> listGroupWithItsLockdownDatasets() {
        return aclRepositoryImpl.listGroupsAndTheirDatasetsInLockdown()
                .stream().peek(gw -> {
                    String groupName = AcuitySidDetails.toUserFromAuthority(gw.getGroupName()).getName();
                    gw.setGroupName(groupName);
                }).collect(toList());
    }

    public Long findVasecurityIdFromStudyId(String studyId) {
        return aclRepositoryImpl.findVasecurityIdFromStudyId(studyId);
    }

    /**
     * Removes all objects that are related to ACLs
     *
     * @param rois - a list of objects that identify the ACLs to delete
     */
    public void removeAclRelatedObjects(List<AcuityObjectIdentityWithPermissionAndLockDown> rois) {
        if (!rois.isEmpty()) {
            List<Long> aclObjectIdentityIds
                    = rois.stream()
                          .map(roi -> aclRepositoryImpl
                                  .findAclObjectIdentityId(roi.getType(), roi.getId()))
                          .collect(toList());
            aclRepositoryImpl.removeEntries(aclObjectIdentityIds);
            aclRepositoryImpl.removeObjects(aclObjectIdentityIds);
            aclRepositoryImpl.removeObjectIdentities(aclObjectIdentityIds);
        }
    }
}
