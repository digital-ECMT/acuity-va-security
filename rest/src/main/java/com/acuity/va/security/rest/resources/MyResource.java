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
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.task.RefreshCachesTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.PROACT_ONLY_USER;
import static java.util.stream.Collectors.toList;

/**
 * Rest resource for operations for the current logged on user
 *
 * @author Glen
 */
@Component
@Path("/my")
@Produces({"application/json"})
public class MyResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(MyResource.class);

    private final ReentrantLock cacheRefreshLock = new ReentrantLock();

    @Autowired
    private RefreshCachesTask refreshCachesTask;

    /*
     * Methods:
     * GET:
     *      /my/user/whoami                           uses security to get current logged on user
     *      /my/acl/                                  list current user object identities
     *      /my/acl/DrugProgram/1?permission=32       has current user permission for object identity
     */
    @LogOperation(name = "LOGON", value = {
            @LogArg(arg = -1, name = "PRID", expression = "getUserId()"),
            @LogArg(arg = -1, name = "NAME", expression = "getFullName()"),
            @LogArg(arg = -1, name = "AUTHORITIES", expression = "getAuthoritiesAsStringToString()")})
    @GET
    @Path("/user/whoami")
    public AcuitySidDetails whoami() {
        AcuitySidDetails acuityUserDetails = getUser();

        LOG.info("GET: Show security info. Principal = " + acuityUserDetails.getUsername());

        return acuityUserDetails;
    }

    /**
     * Gets the list of Object Identities and their permissions the current user has access to.
     */
    @GET
    @Path("/acl")
    public List<AcuityObjectIdentityWithPermissionAndLockDown> getAclForCurrentUser() {
        AcuitySidDetails acuityUserDetails = getUser();

        LOG.debug("GET: get acls for User " + acuityUserDetails);

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(acuityUserDetails, false);

        // log the results by numbers
        long drugProgrammes = userObjectIdentities.stream().filter(obid -> obid.thisDrugProgrammeType()).count();
        long datasets = userObjectIdentities.stream().filter(obid -> obid.thisDatasetType()).count();
        long clinicalStudies = userObjectIdentities.stream().filter(obid -> obid.thisClinicalStudyType()).count();
        LOG.debug("Returning {} drug programmes, {} clinical studies and {} datasets", drugProgrammes, clinicalStudies, datasets);
        return userObjectIdentities;
    }

    /**
     * Gets the list of Object Identities and their permissions the current user has access to that is higher than
     * a AUTHORISED USER
     */
    @GET
    @Path("/aclswithoutauthorisedusers")
    public List<AcuityObjectIdentityWithPermissionAndLockDown> getAclsWithoutAuthorisedUsersForCurrentUser() {
        AcuitySidDetails acuityUserDetails = getUser();

        LOG.debug("GET: get acls for User without authorised users access " + acuityUserDetails);

        List<AcuityObjectIdentityWithPermissionAndLockDown> userObjectIdentities = securityAclService.getUserObjectIdentities(acuityUserDetails, false).
                stream().filter(roi ->
                roi.getRolePermissionMask() != AUTHORISED_USER.getMask()
                        && roi.getRolePermissionMask() != PROACT_ONLY_USER.getMask())
                .collect(toList());

        // log the results by numbers
        long drugProgrammes = userObjectIdentities.stream().filter(obid -> obid.thisDrugProgrammeType()).count();
        long datasets = userObjectIdentities.stream().filter(obid -> obid.thisDatasetType()).count();
        long clinicalStudies = userObjectIdentities.stream().filter(obid -> obid.thisClinicalStudyType()).count();
        LOG.debug("Returning {} drug programmes, {} clinical studies and {} datasets", drugProgrammes, clinicalStudies, datasets);
        return userObjectIdentities;
    }

    /**
     * Checks if current user has permission for an ObjectIdentity.
     * <p/>
     * /my/acl/DrugProgram/1?permission=32 has current user got permission 32/View Dataset on DrugProgram 1 /my/acl/ClinicalStudy/2?permission=64 has
     * current user got permission 64 on ClinicalStudy 2
     *
     * @return Status 202 if has permission otherwise 401
     */
    @GET
    @Path("/acl/{objectIdentityClassname}/{objectIdentityId}")
    public Response hasPermissionForCurrentUser(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            @QueryParam("permissionMask") Integer permissionMask) {

        AcuitySidDetails acuityUserDetails = getUser();

        LOG.debug("POST: has permission for current user "
                + acuityUserDetails.getUserId() + ", for " + objectIdentityClassname + ": " + objectIdentityId);

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        boolean hasPermission = securityAclService.hasPermission(acuityAclObjectIdentity, permissionMask, acuityUserDetails.toSids());
        if (hasPermission) {
            return Response.accepted().build(); //202
        } else {
            return Response.status(Status.UNAUTHORIZED).build(); // 401
        }
    }

    /**
     * Clears a cache with a specific name.
     * <p>
     * Only used by dev team members.
     */
    @GET
    @Path("/clearcache/{name}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response clearCache(@PathParam("name") String name) {

        LOG.debug("GET: clearing cache " + name);

        refreshCachesTask.getCacheManager().getCache(name).removeAll();
        return Response.ok().build();
    }

    /**
     * Clears all the caches, filter key tables and refreshed the mat views
     * <p>
     * Only used by dev team members.
     */
    @POST
    @Path("/refresh")
    @Produces({"text/plain"})
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response refreshAll() {

        LOG.debug("POST: clearing caches, filters and refresh mat views");

        if (cacheRefreshLock.tryLock()) {
            try {
                refreshCachesTask.runHourly();
                refreshCachesTask.runNightly();
            } finally {
                cacheRefreshLock.unlock();
            }
            return Response.ok().build();
        } else {
            return Response.ok("Refresh already started").build();
        }
    }

    @GET
    @Path("/cleanup")
    @Produces({"text/plain"})
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response cleanup() {
        refreshCachesTask.runHourly();
        refreshCachesTask.runNightly();

        return Response.ok().build();
    }
}
