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

package com.acuity.va.security.rest.resources.services;

import com.acuity.va.auditlogger.annotation.LogArg;
import com.acuity.va.auditlogger.annotation.LogOperation;
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_CLASSNAME;
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_ID;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.rest.security.Security;
import com.acuity.va.security.acl.service.SecurityAclService;
import static com.acuity.va.security.rest.resources.AclResource.AUDIT_LOGGING_PREFIX;
import com.acuity.va.security.rest.util.UserPermission;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Common acl service so that both SecurityResource and AclReource can share the same code but have different authentication to the services.
 *
 * Ie one is browser based (ping etc) and one is code based (basic auth)
 *
 * @author Glen
 */
@Service
public class AclRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AclRestService.class);

    @Autowired
    private SecurityAclService securityAclService;

    @Autowired
    protected Security security;

    public Response createAcl(UriInfo uriInfo, String ownerId, AcuityObjectIdentityImpl acuityObjectIdentityWithParent) {

        LOG.debug("POST: adding acl " + acuityObjectIdentityWithParent);

        try {
            ownerId = StringUtils.isEmpty(ownerId) ? security.getUser() : ownerId;

            securityAclService.createAcl(acuityObjectIdentityWithParent, ownerId);

            return Response.created(uriInfo.getAbsolutePathBuilder().
                    path(acuityObjectIdentityWithParent.getClass().getSimpleName()).
                    path("" + acuityObjectIdentityWithParent.getIdentifier()).build()).build();
        } catch (AlreadyExistsException aee) {
            LOG.error("Unable createAcl " + acuityObjectIdentityWithParent, aee);
            throw new WebApplicationException(acuityObjectIdentityWithParent + " already exists", Response.Status.CONFLICT);
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable createAcl " + acuityObjectIdentityWithParent, nfe);
            throw new WebApplicationException(acuityObjectIdentityWithParent + " not found", Response.Status.NOT_FOUND);
        }
    }

    @LogOperation(name = AUDIT_LOGGING_PREFIX + "ADD_ACE", value = {
        @LogArg(arg = 1, name = ACL_CLASSNAME),
        @LogArg(arg = 2, name = ACL_ID),
        @LogArg(arg = 4, name = "ACE_USER", expression = "acuitySidDetails.getSidAsString()"),
        @LogArg(arg = 4, name = "ACE_PERMISSION", expression = "permissionMask"),
        @LogArg(arg = 4, name = "ACE_GRANTING", expression = "getGrantingAsInt()"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", expression = "acuitySidDetails.isGroup()"),
        @LogArg(arg = 3, name = "OVERWRITE_ALL_FOR_USER", staticClass = "org.apache.commons.lang3.BooleanUtils", expression = "toInteger()")
    })
    public Response addPermissionForASid(
            UriInfo uriInfo,
            String objectIdentityClassname,
            Long objectIdentityId,
            boolean overwrite,
            UserPermission up) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("POST: add permission " + up.getPermissionMask() + " granting? " + up.isGranting() + " for "
                + "User " + up.getAcuitySidDetails().getSidAsString() + " to " + acuityAclObjectIdentity);

        try {
            if (overwrite) {
                LOG.debug("replacing all aces for " + up.getAcuitySidDetails().getSidAsString() + " to " + acuityAclObjectIdentity);
                securityAclService.replaceAce(acuityAclObjectIdentity, up.getPermissionMask(), up.getAcuitySidDetails().toSid(), up.isGranting());
            } else {
                securityAclService.addAce(acuityAclObjectIdentity, up.getPermissionMask(), up.getAcuitySidDetails().toSid(), up.isGranting());
            }

            return Response.created(uriInfo.getAbsolutePathBuilder().
                    queryParam("permissionMask", up.getPermissionMask()).
                    queryParam("userId", up.getAcuitySidDetails().getSidAsString()).build()).
                    build();
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable to add permission " + up.getPermissionMask() + " for User " + up.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException("Unable to add permission " + up.getPermissionMask() + " for User " + up.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, Response.Status.NOT_FOUND);
        }
    }
}
