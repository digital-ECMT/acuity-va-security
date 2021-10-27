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

package com.acuity.va.security.acl.permissions;

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.service.SecurityAclService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import org.springframework.security.acls.model.Sid;

/**
 * This overrides the AclPermissionEvaluator in spring and is used in the @PreAuthorize() part of the rest web services
 *
 *
 * @author Glen
 */
public class AcuityAclPermissionEvaluator extends AclPermissionEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AcuityAclPermissionEvaluator.class);

    @Autowired
    protected PermissionFactory permissionFactory;
    @Autowired
    protected SecurityAclService securityAclService;
    @Autowired
    protected AclRepository aclRepository;

    public AcuityAclPermissionEvaluator(AclService aclService) {
        super(aclService);
    }

    /**
     * Overrides the hasPermission method to allow generating of the AcuityObjectIdentity from the targetId and targetType. Then follows the same pattern as
     * normal after this.
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType classname of the acl, ie DrugProgramme, ClinicalStudy, Dataset
     * @param permission requested permission for the acl ( acl = (targetId,targetType)
     * @return has permission been granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        LOG.debug("hasPermission user {} on {} id {} with {}", authentication.getName(), targetType, targetId, permission);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        return hasPermission(authentication, acuityAclObjectIdentity, permission);
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Drug Programme
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasDPPermission(authentication, #drugProgramme, #objectIdentityId)")
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'DrugPrgramme', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetType ie Detect or AcuityDrugProgramme
     * @param targetId id of the acl
     * @return has permission been granted
     */
    public boolean hasDPPermission(Authentication authentication, String targetType, Serializable targetId) {
        LOG.debug("hasDPPermission user {} on {} id {} with {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        return hasPermission(authentication, acuityAclObjectIdentity, AcuityPermissions.VIEW_VISUALISATIONS);
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Clinical Study
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasStudyPermission(authentication, #study, #objectIdentityId)")
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'ClinicalStudy', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetType ie Detect or AcuityClinicalStudy
     * @param targetId id of the acl
     * @return has permission been granted
     */
    public boolean hasStudyPermission(Authentication authentication, String targetType, Serializable targetId) {
        LOG.debug("hasStudyPermission user {} on {} id {} with {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        return hasPermission(authentication, acuityAclObjectIdentity, AcuityPermissions.VIEW_VISUALISATIONS);
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Dataset
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasStudyPermission(authentication, #dataset, #objectIdentityId)")
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'Dataset', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetType ie Detect or AcuityDataset
     * @param targetId id of the acl
     * @return has permission been granted
     */
    public boolean hasVisPermission(Authentication authentication, String targetType, Serializable targetId) {
        LOG.debug("hasVisPermission user {} on {} id {} with {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        return hasPermission(authentication, acuityAclObjectIdentity, AcuityPermissions.VIEW_VISUALISATIONS);
    }

    /**
     * Checks if the current user has access to add a user with newPermissionMask to the AcuityObjectId as (targetId, targetType)
     * <p>
     * ie can the current user (user001) add a new user with permission mask newPermissionMask
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType classname of the acl, ie DrugProgramme, ClinicalStudy, Dataset
     * @param newPermissionMask mask of one of AUTHORISED_USER, ADMINISTRATOR, AUTHORISERS, DRUG_PROGRAMME_DATA_OWNER, DEVELOPMENT_TEAM
     * @return has permission been granted
     */
    public boolean hasAddPermission(Authentication authentication, Serializable targetId, String targetType, Integer newPermissionMask) {
        LOG.debug("hasAddPermission user {} on {} id {} with {}", authentication.getName(), targetType, targetId, newPermissionMask);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        Permission newPermission = permissionFactory.buildFromMask(newPermissionMask);

        // requiredPermission for adding a user with new permission newPermission
        Permission requiredPermission = AcuityCumulativePermissionsAsRoles.getIndividualPermissionForPermissionsAsRole(newPermission);

        LOG.info("hasEditPermission for {}, so required permission check is for {}", newPermission, requiredPermission);

        return hasPermission(authentication, acuityAclObjectIdentity, requiredPermission);
    }

    /**
     * See hasRemovePermission(Authentication authentication, Serializable targetId, String targetType, String userId, Integer newPermissionMask)
     */
    public boolean hasRemovePermission(Authentication authentication, Serializable targetId, String targetType, String userId) {
        return hasRemovePermission(authentication, targetId, targetType, userId, null);
    }

    /**
     * Checks if the current user has access to remove a user with newPermissionMask to the AcuityObjectId as (targetId, targetType)
     * <p>
     * ie can the current user (user001) remove a new user with permission mask newPermissionMask
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType classname of the acl, ie DrugProgramme, ClinicalStudy, Dataset
     * @param sid sid of the permission(s) that will be removed
     * @param newMaxPermissionMask mask of one of AUTHORISED_USER, ADMINISTRATOR, AUTHORISERS, DRUG_PROGRAMME_DATA_OWNER, DEVELOPMENT_TEAM
     * @return has permission been granted
     */
    public boolean hasRemovePermission(Authentication authentication, Serializable targetId, String targetType, String sid, Integer newMaxPermissionMask) {
        LOG.debug("hasRemovePermission user {} on {} id {} with {} for {}", authentication.getName(), targetType, targetId, newMaxPermissionMask, sid);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(targetType, ((Number) targetId).longValue());

        Permission requiredPermission = null; //required permission to do this operation

        if (null == newMaxPermissionMask || newMaxPermissionMask == 0) {
            // remove all permissions operation on the acuityAclObjectIdentity for a user.
            // Need to find maximum permission for that user which we are going to delete

            // find highest mask in the security db
            MutableAcl mutableAcl = securityAclService.find(acuityAclObjectIdentity);
            Sid principalSid = AcuitySidDetails.toUser(sid).toSid();
            Sid groupSid = AcuitySidDetails.toUserFromAuthority(sid).toSid();
            newMaxPermissionMask = mutableAcl.getEntries().stream()
                    .filter(e -> e.getSid().equals(principalSid) || e.getSid().equals(groupSid))
                    .mapToInt(e -> e.getPermission().getMask()).max().orElse(0);

            LOG.info("Max permission mask for {} is {}", acuityAclObjectIdentity, newMaxPermissionMask);

            if (newMaxPermissionMask == 0) { //  no permissions to remove so allow
                return true;
            }
        }

        // requiredPermission for removing a user with new permission newPermission
        Permission newPermissionRole = AcuityCumulativePermissionsAsRoles.getPermissionRoleFromPermissionMask(newMaxPermissionMask);
        requiredPermission = AcuityCumulativePermissionsAsRoles.getIndividualPermissionForPermissionsAsRole(newPermissionRole);

        LOG.info("hasRemovePermission for {}, so required permission check is for {}", newPermissionRole, requiredPermission);

        return hasPermission(authentication, acuityAclObjectIdentity, requiredPermission);
    }

    /**
     * Checks if this dataset id is training data (ie name ends with STDY4321_Dummy_Instance
     *
     * @param targetId id of the dataset acl
     * @return has permission been granted
     */
    public boolean isTrainingData(Serializable targetId) {
        LOG.debug("isTrainingData Dataset id {}", targetId);

        String name = aclRepository.getAclName(AcuityDataset.class.getName(), ((Number) targetId).longValue());
        boolean isTraining = name != null && name.endsWith("STDY4321_Dummy_Instance");

        LOG.info("Returning {} - isTrainingData for Vis: {} name {}", isTraining, targetId, name);

        return isTraining;
    }

    /**
     * Checks if this drug id is training data (ie name ends with STDY4321) JUST FOR ACUITY AT THE MOMENT
     *
     * @param targetId id of the drug acl
     * @return has permission been granted
     */
    public boolean isTrainingDrug(Serializable targetId) {
        LOG.debug("isTrainingDrug Dashboard id {}", targetId);

        String name = aclRepository.getAclName(DrugProgramme.class.getName(), ((Number) targetId).longValue());
        boolean isTraining = (name != null && name.endsWith("STDY4321"));

        LOG.info("Returning {} - isTrainingDrug for Dashboard: {} name {}", isTraining, targetId, name);

        return isTraining;
    }
}
