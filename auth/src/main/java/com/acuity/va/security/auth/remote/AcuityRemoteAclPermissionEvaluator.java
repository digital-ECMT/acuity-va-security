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

package com.acuity.va.security.auth.remote;

import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.auth.common.ISecurityResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.List;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;


/**
 * This overrides the AclPermissionEvaluator in spring and is used in the @PreAuthorize() part of the rest web services
 *
 * @author Glen
 */
public class AcuityRemoteAclPermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AcuityRemoteAclPermissionEvaluator.class);

    protected ISecurityResourceClient securityResourceClient;
    protected PermissionFactory permissionFactory;

    public void setPermissionFactory(PermissionFactory permissionFactory) {
        this.permissionFactory = permissionFactory;
    }

    public void setSecurityResourceClient(ISecurityResourceClient securityResourceClient) {
        this.securityResourceClient = securityResourceClient;
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
        throw new NoSuchMethodError("Method not implemeneted");
    }

    /**
     * Overrides the hasPermission method to allow generating of the AcuityObjectIdentity from the targetId and targetType. Then follows the same pattern as
     * normal after this.
     *
     * @param authentication security user
     * @param targetDomainObject list of datasets for the acl
     * @param permission requested permission for the dataset
     * @return has permission been granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return hasViewDatasetPermission(authentication, targetDomainObject, permission);
    }

    /**
     * Checks for view permission and the extra permission on the datasets
     *
     * @param authentication security user
     * @param targetDomainObject list of datasets for the acl
     * @param extraPermission requested extra permission for the dataset
     * @return has permission been granted
     */
    public boolean hasViewDatasetWithExtraPermission(Authentication authentication, Object targetDomainObject, Object extraPermission) {
        LOG.debug("hasViewDatasetWithExtraPermission user {} on {} with permission {}", authentication.getName(), targetDomainObject, extraPermission);

        boolean hasViewVisPermission = hasViewDatasetPermission(authentication, targetDomainObject, AUTHORISED_USER.getMask());

        boolean hasExtraViewPermission;
        if (extraPermission == null) {
            hasExtraViewPermission = true;
        } else {
            hasExtraViewPermission = true; //securityResourceClient.
            //hasPermissionForUser(authentication.getName(), (List<Dataset>) targetDomainObject, resolvePermission(extraPermission).getMask());
        }

        return (hasViewVisPermission && hasExtraViewPermission);
    }
    
    /**
     * Checks for view permission on the datasets
     *
     * @param authentication security user
     * @param targetDomainObject list of datasets for the acl
     * @return has permission been granted
     */
    public boolean hasViewDatasetPermission(Authentication authentication, Object targetDomainObject) {
        LOG.debug("hasViewDatasetPermission user {} on {} with permission {}", authentication.getName(), targetDomainObject);

        return hasViewDatasetPermission(authentication, targetDomainObject, AUTHORISED_USER.getMask());
    }

    /**
     * Checks for view permission on the datasets
     *
     * @param authentication security user
     * @param targetDomainObject list of datasets for the acl
     * @param permission requested permission for the dataset
     * @return has permission been granted
     */
    public boolean hasViewDatasetPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        LOG.debug("hasViewDatasetPermission user {} on {} with permission {}", authentication.getName(), targetDomainObject, permission);

        return securityResourceClient.
                hasPermissionForUser(authentication.getName(), (List<Dataset>) targetDomainObject, resolvePermission(permission).getMask());
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
        LOG.error("Unsupported permission {}", permission);
        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }
}
