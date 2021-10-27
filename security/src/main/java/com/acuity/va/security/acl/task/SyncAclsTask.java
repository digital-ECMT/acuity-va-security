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

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithInitialLockDown;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithParent;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.task.AclAdminstratorAuthenticationToken;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.service.VASecurityResourceClient;
import com.acuity.va.security.acl.service.VASecurityResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Sync Populates
 *
 * @author Glen
 */
@Component
public class SyncAclsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncAclsTask.class);

    @Autowired
    private AclRepository aclObjectRepository;
    @Autowired
    private SecurityAclService securityAclService;
    @Autowired
    private VASecurityResourceFactory vASecurityResourceFactory;

    @Transactional(value = "security")
    public void run() {
        run(true);
    }

    @Transactional(value = "security")
    public void run(boolean checkEnabled) {
        LOGGER.info("Running SyncAclsTask checkEnabled = {} ...", checkEnabled);

        // When adding createDefaultAcl() then the spring acl code looks for SecurityContextHolder.getContext().getAuthentication()
        // so because this is running as a task we needs to add a user into the thread local for spring
        SecurityContextHolder.getContext().setAuthentication(new AclAdminstratorAuthenticationToken(AcuityPermissions.ACUITY_SERVER_USER));

        List<VASecurityResourceClient> remotes = checkEnabled ? vASecurityResourceFactory.getAll() : vASecurityResourceFactory.getDetect();

        remotes.forEach(vaSecurityResourceClient -> {
            LOGGER.info("Querying VAHub at {} for list of rois ...", vaSecurityResourceClient.getUrl());

            try {
                Collection<AcuityObjectIdentityWithInitialLockDown> allRemoteObjectIdentitiesSorted = createAndSortRois(vaSecurityResourceClient.loadRois());
                removeAllRoisNotInTheCollection(allRemoteObjectIdentitiesSorted);
                allRemoteObjectIdentitiesSorted.forEach(this::saveRoi);
            } catch (Exception ex) {
                LOGGER.error("Unable to sync rois with VAHub (url {})", vaSecurityResourceClient.getUrl(), ex);
            }
        });

        LOGGER.info("Finished SyncAclsTask");
    }

    private void saveRoi(AcuityObjectIdentityWithInitialLockDown roi) {
        try {
            LOGGER.info("Processing " + roi);
            Long aclId = null;
            try {
                aclId = aclObjectRepository.findAclObjectId(roi.getType(), roi.getId());
                if (aclId == null) {
                    LOGGER.info(roi + " not found (is null), creating new Acl adding DEVELOPMENT_TEAM as an ace");
                }
            } catch (NotFoundException nfe) {
                LOGGER.info(roi + " not found, creating new Acl adding DEVELOPMENT_TEAM as an ace");
            }

            if (aclId == null) {
                AcuityObjectIdentityWithParent roiWithParent = (AcuityObjectIdentityWithParent) roi;

                Optional<MutableAcl> mutableAcl = createDefaultAcl(roiWithParent);
                aclId = mutableAcl.map(MutableAcl::getId).map(id -> (Long) id).orElse(null);

                if (roiWithParent.thisDatasetType() && roiWithParent.thisDetectType()) {
                    securityAclService.setInheritPermissions((DetectDataset) roiWithParent, false);
                }
            }

            if (aclId != null) {
                aclObjectRepository.insertOrUpdateAclObject(aclId, roi);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to sync rois ", ex);
        }
    }

    private List<AcuityObjectIdentityWithInitialLockDown> createAndSortRois(List<AcuityObjectIdentityWithInitialLockDown> allObjectIdentities) {
        // need to remove their ids if they have one, dp and cs are done by name auto ids
        Stream<DrugProgramme> dp = allObjectIdentities.stream().filter(AcuityObjectIdentity::thisDrugProgrammeType)
                .map(dpi -> new DrugProgramme(dpi.getName()))
                .distinct();

        Stream<ClinicalStudy> cs = allObjectIdentities.stream().filter(AcuityObjectIdentity::thisClinicalStudyType)
                .sorted((r1, r2) -> r1.thisAcuityType() ? -1 : 1)// sort acuity first so detect get removed on distinct if 2 the same (but different names)
                .map(csi -> (ClinicalStudy) csi)
                .map(csi -> {
                    ClinicalStudy csi1 = new ClinicalStudy(csi.getCode(), csi.getName());
                    csi1.setDrugProgramme(csi.getDrugProgramme());
                    csi1.setOrigin(csi.getOrigin());
                    return csi1;
                })
                .distinct();

        Stream<AcuityObjectIdentityWithInitialLockDown> dsd = allObjectIdentities.stream()
                .filter(AcuityObjectIdentity::thisDatasetType)
                .filter(AcuityObjectIdentity::thisDetectType);

        Stream<AcuityObjectIdentityWithInitialLockDown> dsr = allObjectIdentities.stream()
                .filter(AcuityObjectIdentity::thisDatasetType)
                .filter(AcuityObjectIdentity::thisAcuityType);


        Stream<AcuityObjectIdentityWithInitialLockDown> identityStream = concat(concat(dp, cs), concat(dsd, dsr));

        List<AcuityObjectIdentityWithInitialLockDown> identities = identityStream.collect(toList());

        LOGGER.info("Identities returned (total {}): {}", identities.size(), identities);

        return identities;
    }

    /**
     * Create the Acl and then add a single ace for DEVELOPMENT_TEAM role.
     * <p/>
     * This also adds the parent Drug Programme to the clinical study or datasetIdentity
     *
     * @param identity to add the acl and ace to.
     */
    private Optional<MutableAcl> createDefaultAcl(AcuityObjectIdentityWithParent identity) {
        LOGGER.info("Creating an acl for " + identity);

        AcuityObjectIdentity parentDrugProgramme = null;
        if (!identity.thisDrugProgrammeType()) {
            if (identity.thisDatasetType()) {
                parentDrugProgramme = aclObjectRepository.getDatasetParentDrugProgrammeFromObj((Dataset) identity);
            } else if (identity.thisClinicalStudyType()) {
                parentDrugProgramme = aclObjectRepository.getClinicalStudyParentDrugProgrammeFromObj((ClinicalStudy) identity);
            }

            if (parentDrugProgramme == null) {
                LOGGER.error("SYNC ERROR " + identity + " must have a parent drug programme");
                return Optional.empty();
            }

            LOGGER.info("Setting " + parentDrugProgramme + " as parent to " + identity);

            identity.setParent(parentDrugProgramme);
        }

        MutableAcl newAcl = securityAclService.createAcl(identity, AcuityPermissions.ACUITY_SERVER_USER);

        securityAclService.addRoleAce(identity,
                AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM.getMask(), new GrantedAuthoritySid(AcuityPermissions.DEVELOPMENT_TEAM_ROLE));

        return Optional.of(newAcl);
    }

    /**
     * Removes all objects which are not in the list
     * Due to constraints in the database, this method first removes child-objects, and then parent-objects
     *
     * @param aliveRois - a list of objects that should not be deleted
     */
    private void removeAllRoisNotInTheCollection(Collection<AcuityObjectIdentityWithInitialLockDown> aliveRois) {
        LOGGER.info("Alive rois from vahub: (total {}): {}", aliveRois.size(), aliveRois);
        List<AcuityObjectIdentityWithPermissionAndLockDown> roiList = aclObjectRepository
                .listObjectIdentitiesNotCached();
        LOGGER.info("All current rois in vasecurity: (total {}): {}", roiList.size(), roiList);
        roiList.removeIf(acl -> (aliveRois.stream().anyMatch(roi -> roi.getId().equals(acl.getId()))));
        LOGGER.info("Rois for deletion: (total {}): {}", roiList.size(), roiList);
        Map<Boolean, List<AcuityObjectIdentityWithPermissionAndLockDown>> parentAndChildObjects
                = roiList.stream().collect(partitioningBy(AcuityObjectIdentity::thisDrugProgrammeType));
        aclObjectRepository.removeAclRelatedObjects(parentAndChildObjects.get(false));
        aclObjectRepository.removeAclRelatedObjects(parentAndChildObjects.get(true));
        securityAclService.clearAclCache();
    }
}
