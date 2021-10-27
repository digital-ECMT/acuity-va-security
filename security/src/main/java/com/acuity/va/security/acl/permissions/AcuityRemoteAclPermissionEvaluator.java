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

import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;

import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import com.acuity.va.security.acl.service.AcuitySecurityResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.PermissionFactory;

/**
 * This overrides the AclPermissionEvaluator in spring and is used in the @PreAuthorize() part of the rest web services
 *
 *
 * @author Glen
 */
public class AcuityRemoteAclPermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AcuityRemoteAclPermissionEvaluator.class);

    @Autowired
    protected AcuitySecurityResourceClient acuitySecurityResourceClient;
    //@Autowired
    protected PermissionFactory permissionFactory;

    public void setPermissionFactory(PermissionFactory permissionFactory) {
        this.permissionFactory = permissionFactory;
    }

    public void setAcuitySecurityResourceClient(AcuitySecurityResourceClient acuitySecurityResourceClient) {
        this.acuitySecurityResourceClient = acuitySecurityResourceClient;
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

        return acuitySecurityResourceClient.
                hasPermissionForUser(authentication.getName(), acuityAclObjectIdentity, resolvePermission(permission).getMask());
    }

    /**
     * Overrides the hasPermission method to allow generating of the AcuityObjectIdentity from the targetId and targetType. Then follows the same pattern as
     * normal after this.
     *
     * @param authentication security user
     * @param targetDomainObject the acl, ie DrugProgramme, ClinicalStudy, Dataset
     * @param permission requested permission for the acl ( acl = (targetId,targetType)
     * @return has permission been granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        LOG.debug("hasPermission user {} on {} id {} with {}", authentication.getName(), targetDomainObject, permission);

        return acuitySecurityResourceClient.
                hasPermissionForUser(authentication.getName(), (AcuityObjectIdentity) targetDomainObject, resolvePermission(permission).getMask());
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Clinical Study
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasStudyPermission(authentication, #objectIdentityId)") is a shortcut fot
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'ClinicalStudy', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType ie Detect or DrugProgramme
     * @return has permission been granted
     */
    public boolean hasStudyPermission(Authentication authentication, String targetType, Serializable targetId) {
        LOG.debug("hasStudyPermission user {} on {} id {} with {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS);

        ClinicalStudy clinicalStudy = AcuityObjectIdentityImpl.createClinicalStudy(targetType, ((Number) targetId).longValue());

        return acuitySecurityResourceClient.
                hasPermissionForUser(authentication.getName(), clinicalStudy, VIEW_VISUALISATIONS.getMask());
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Dataset
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasStudyPermission(authentication, #objectIdentityId)") is a shortcut fot
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'Dataset', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType ie Detect or DrugProgramme
     * @return has permission been granted
     */
    public boolean hasVisPermission(Authentication authentication, String targetType, Serializable targetId) {
        LOG.debug("hasVisPermission user {} on {} id {} with {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS);

        Dataset dataset = AcuityObjectIdentityImpl.createDataset(targetType, ((Number) targetId).longValue());

        return acuitySecurityResourceClient.hasPermissionForUser(authentication.getName(), dataset, VIEW_VISUALISATIONS.getMask());
    }

    /**
     * Shortcut format for checking if a user has atleast VIEW_VISUALISATIONS on a Dataset aswell as the extra permission
     * <p>
     * <p>
     * This      <code>
     * PreAuthorize("@pe.hasStudyPermission(authentication, #objectIdentityId)") is a shortcut fot
     * Shortcut for
     * PreAuthorize("hasPermission(#objectIdentityId, 'Dataset', 'VIEW_VISUALISATIONS')")
     * </code>
     *
     * @param authentication security user
     * @param targetId id of the acl
     * @param targetType ie Detect or DrugProgramme
     * @param extraPermission extra permission needed
     * @return has permission been granted
     */
    public boolean hasVisPermission(Authentication authentication, String targetType, Serializable targetId, Object extraPermission) {
        LOG.debug("hasVisPermission user {} on {} id {} with {} and {}",
                authentication.getName(), targetType, targetId, AcuityPermissions.VIEW_VISUALISATIONS, extraPermission);

        Dataset dataset = AcuityObjectIdentityImpl.createDataset(targetType, ((Number) targetId).longValue());

        boolean hasViewVisPermission = acuitySecurityResourceClient.hasPermissionForUser(authentication.getName(), dataset, VIEW_VISUALISATIONS.getMask());

        boolean hasExtraViewPermission = acuitySecurityResourceClient.
                hasPermissionForUser(authentication.getName(), dataset, resolvePermission(extraPermission).getMask());

        return (hasViewVisPermission && hasExtraViewPermission);
    }

    private Permission resolvePermission(Object permission) {
        if (permission instanceof Integer) {
            return permissionFactory.buildFromMask(((Integer) permission));
        }

        if (permission instanceof Permission) {
            return (Permission) permission;
        }

        if (permission instanceof String) {
            String permString = (String) permission;
            Permission p;

            try {
                p = permissionFactory.buildFromName(permString);
            } catch (IllegalArgumentException notfound) {
                p = permissionFactory.buildFromName(permString.toUpperCase());
            }

            if (p != null) {
                return p;
            }
        }

        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }
}
