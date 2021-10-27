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

package com.acuity.va.security.rest.resources;

import com.acuity.va.auditlogger.annotation.LogArg;
import com.acuity.va.auditlogger.annotation.LogOperation;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DatasetsRequest;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMask;
import com.acuity.va.security.acl.task.SyncAclsTask;
import com.acuity.va.security.rest.resources.services.AclRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_CLASSNAME;
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_ID;
import static java.util.stream.Collectors.toList;

/**
 * Rest resource for security operations for the distributed security service
 *
 * @author Glen
 */
@Component
@Path("/security")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class SecurityResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityResource.class);
    private static final String AUDIT_LOGGING_PREFIX = "REMOTE_SECURITY_";

    @Autowired
    private AclRestService aclService;

    @Autowired
    private SyncAclsTask syncAclsTask;

    /*
     * Methods:
     * GET:
     *      /security/loadUserByUsername/{userId}                loads the user from the user db
     *      /security/acls/{userId}                              get the list of user acls
     *      /security/acl/{objectIdentityClassname}
     *           /{objectIdentityId}/{permissionMask}/{userId}    checks if userId has permissionMask permission for objectIdentityClassname objectIdentityId
     * POST:
     *     /security/acl/ownerId/dsdsd Json object AcuityObjectIdentityWithParent. creates new acl
     */
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "LOAD_USER_BY_USERNAME", value = {
            @LogArg(arg = 0, name = "PRID")
    })
    @GET
    @Path("/loadUserByUsername/{userId}")
    public AcuitySidDetails loadUserByUsername(@PathParam("userId") String userId) {

        LOG.info("GET: loadUserByUsername = " + userId);

        try {
            return (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);
        } catch (UsernameNotFoundException nfe) {
            LOG.error("User " + userId + " not found", nfe);
            throw new WebApplicationException(userId + " not found", Response.Status.NOT_FOUND);
        }
    }

    @LogOperation(name = AUDIT_LOGGING_PREFIX + "GET_ACLS_FOR_USER", value = {
            @LogArg(arg = 0, name = "PRID")
    })
    @GET
    @Path("/acls/{userId}")
    public List<AcuityObjectIdentityWithPermission> getAclsForUser(@PathParam("userId") String userId) {

        LOG.info("GET: getAclsForUser = " + userId);

        try {
            AcuitySidDetails acuitySidDetails = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);
            List<AcuityObjectIdentityWithPermissionAndLockDown> allUserObjectIdentities = securityAclService.getUserObjectIdentities(acuitySidDetails);

            List<AcuityObjectIdentityWithPermission> viewVisPermissionObjectIdentities = allUserObjectIdentities.stream().
                    filter(AcuityObjectIdentityWithPermission::getCanView)
                    .collect(toList()); // only list ones user can view

            // log the results by numbers
            long drugProgrammes = viewVisPermissionObjectIdentities.stream()
                    .filter(AcuityObjectIdentity::thisDrugProgrammeType)
                    .count();
            long datasets = viewVisPermissionObjectIdentities.stream()
                    .filter(AcuityObjectIdentity::thisDatasetType)
                    .count();
            long clinicalStudies = viewVisPermissionObjectIdentities.stream()
                    .filter(AcuityObjectIdentity::thisClinicalStudyType)
                    .count();
            LOG.debug("Returning {} drug programmes, {} clinical studies and {} datasets", drugProgrammes, clinicalStudies, datasets);

            return viewVisPermissionObjectIdentities;
        } catch (UsernameNotFoundException nfe) {
            LOG.error("User " + userId + " not found", nfe);
            throw new WebApplicationException(userId + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Checks if the user has permission for an ObjectIdentity.
     * <p/>
     * /security/acl/DrugProgram/1/32/user001 has user001 got permission 32/View Dataset on DrugProgram 1 /security/acl/ClinicalStudy/2/64/user001 has user001
     * got permission 64 on ClinicalStudy 2
     *
     * @return Status 202 if has permission otherwise 406
     */
    @GET
    @Path("/acl/{objectIdentityClassname}/{objectIdentityId}/{permissionMask}/{userId}")
    public Response hasPermissionForUser(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            @PathParam("userId") String userId,
            @PathParam("permissionMask") Integer permissionMask) {

        AcuitySidDetails acuityUserDetails = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);

        LOG.debug("GET: has permission for user " + userId + ", for " + objectIdentityClassname + ": " + objectIdentityId);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        boolean hasPermission = securityAclService.hasPermission(acuityAclObjectIdentity, permissionMask, acuityUserDetails.toSids());
        if (hasPermission) {
            return Response.accepted().build(); //202
        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build(); // 406 so doesnt clash with 401 username and password error with basic auth
        }
    }

    /**
     * Checks if the user has permission for a AcuityDataset from an unique studyid.
     * <p/>
     * /security/acl/AcuityDataset/STUDY0002/32/user001 has user001 got permission 32/View Dataset on Acuity dataset STUDY0002
     *
     * @return Status 202 if has permission otherwise 406
     */
    @GET
    @PreAuthorize("hasRole('ROLE_REMOTE_TRISARC_USER')")
    @Path("/acl/AcuityDataset/{studyId}/{userId}")
    public AcuityObjectIdentityWithPermissionAndLockDown hasAcuityDatasetPermissionForUserFromStudyId(
            @PathParam("studyId") String studyId,
            @PathParam("userId") String userId) {

        AcuitySidDetails acuityUserDetails = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);

        LOG.debug("GET: has permission for user " + userId + ", for Acuity: " + studyId);

        try {
            Optional<AcuityObjectIdentityWithPermissionAndLockDown> roiFromStudyId = securityAclService.getRoiFromStudyId(acuityUserDetails, studyId);
            if (roiFromStudyId.isPresent()) {
                return roiFromStudyId.get();
            } else {
                // 406 so doesnt clash with 401 username and password error with basic auth
                throw new WebApplicationException("StudyId: " + studyId + " not found", Response.Status.NOT_ACCEPTABLE);
            }
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("StudyId: " + studyId + " not found", nfe);
            throw new WebApplicationException("StudyId: " + studyId + " not found", Response.Status.NOT_FOUND);
        } catch (UsernameNotFoundException unf) {
            LOG.error("User: " + userId + " not found", unf);
            throw new WebApplicationException("User: " + userId + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Checks if the user has permission for the datasets.
     * <p/>
     * /security/acl/32/user001 --> has user001 got permission 32/View Dataset on DrugProgram 1 /security/acl/64/user001 has user001 got permission 64 on
     * ClinicalStudy 2
     *
     * @return Status 202 if has permission otherwise 406
     */
    @POST
    @Path("/acl/haspermission/{permissionMask}/{userId}")
    public Response hasPermissionForUser(
            @PathParam("userId") String userId,
            @PathParam("permissionMask") Integer permissionMask,
            DatasetsRequest datasetsRequest) {

        AcuitySidDetails acuityUserDetails = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);

        LOG.debug("POST: has permission for user " + userId + ", for " + datasetsRequest.getDatasets());

        for (Dataset dataset : datasetsRequest.getDatasets()) {
            LOG.debug("Checking permission for " + dataset);
            boolean hasPermission = securityAclService.hasPermission(dataset, permissionMask, acuityUserDetails.toSids());
            if (!hasPermission) {
                LOG.debug("User " + userId + " doesn't have permisson for " + dataset);
                return Response.status(Response.Status.NOT_ACCEPTABLE).build(); // 406 so doesnt clash with 401 username and password error with basic auth
            }
        }
        LOG.debug("User " + userId + " has permission for " + datasetsRequest.getDatasets());
        return Response.accepted().build(); //202
    }

    /**
     * Gets the role permission mask for the user for an ObjectIdentity.
     * <p/>
     * /security/acl/DrugProgramme/1/user001 gets user001 permission on DrugProgram 1 /security/acl/ClinicalStudy/2/64/user001 has user001 got permission 64 on
     * ClinicalStudy 2
     *
     * @return Status 202 if has permission otherwise 401
     */
    @GET
    @Path("/acl/{objectIdentityClassname}/{objectIdentityId}/{userId}")
    public int getPermissionForUser(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            @PathParam("userId") String userId) {

        AcuitySidDetails acuityUserDetails = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);

        LOG.debug("POST: gets the role permission for user " + userId + ", for " + objectIdentityClassname + ": " + objectIdentityId);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        Permission permission = securityAclService.getRolePermissionForUser(acuityAclObjectIdentity, acuityUserDetails);

        if (permission != null) {
            return permission.getMask();
        } else {
            return 0;
        }
    }

    /**
     * Creates an Acl for a AcuityAclObjectIdentity.
     * <p>
     * POST: /security/acl/ownerId/{ownerId} Json object AcuityObjectIdentityWithParent. Owner optional
     *
     * @return 201 created with uri: /acl/DrugProgramme/13
     */
    @POST
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR')")
    @LogOperation(name = "CREATE_ACL", logOnlyOnSuccess = true, value = {
            @LogArg(arg = 2, name = ACL_CLASSNAME, expression = "getType()"),
            @LogArg(arg = 2, name = ACL_ID, expression = "getId()"),
            @LogArg(arg = 1, name = "ACL_OWNER")
    })
    public Response createAcl(@Context UriInfo uriInfo,
                              @PathParam("ownerId") String ownerId, AcuityObjectIdentityImpl acuityObjectIdentityWithParent) {

        return aclService.createAcl(uriInfo, ownerId, acuityObjectIdentityWithParent);
    }

    /**
     * Lists all the user permissions for a AcuityAclObjectIdentity.
     * <p>
     * GET: /security/acl/AcuityDataset/1
     *
     * @return list of users with uri: /acl/AcuityDataset/1
     */
    @GET
    @Path("/acl/{objectIdentityClassname}/{objectIdentityId}")
    public List<AcuitySidDetails> listPermissionForDataset(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        LOG.debug("GET: list user permissions for " + objectIdentityClassname + ": " + objectIdentityId);

        Dataset acuityDataset = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);

        List<AcuitySidDetails> allPermissionForDataset = securityAclService.getGrantedUsersForAcl(acuityDataset).stream().
                filter(roi -> !roi.getUser().isGroup()).map(AcuitySidDetailsWithPermissionMask::getUser).collect(toList());

        return allPermissionForDataset;
    }

    /**
     * Lists all the user permissions for all of the AcuityAclObjectIdentitoes.
     * <p>
     * GET: /security/acl/datasets/listpermissions
     */
    @POST
    @Path("/acl/datasets/listpermissions")
    public List<AcuitySidDetails> listPermissionForDataset(@RequestParam DatasetsRequest requestBody) {

        LOG.debug("GET: list user permissions for " + requestBody.getDatasetsObject());

        return requestBody.getDatasets().stream().flatMap(ds -> {
            return securityAclService.getGrantedUsersForAcl(ds).stream().
                    filter(roi -> !roi.getUser().isGroup()).map(roi -> roi.getUser());
        }).distinct().collect(toList());
    }

    @HEAD
    @Path("/sync")
    public Response runSynchronization() {
        syncAclsTask.run();
        return Response.ok().build();
    }
}
