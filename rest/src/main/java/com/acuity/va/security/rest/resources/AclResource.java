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
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugStudyDatasetWithPermission;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMask;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.permissions.AcuityPermissionViewPackagesManager;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.rest.resources.services.AclRestService;
import com.acuity.va.security.rest.util.UserPermission;
import com.acuity.va.security.rest.util.ViewUserPermission;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_CLASSNAME;
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_ID;
import static com.acuity.va.security.common.Constants.TRAINED_USER_GROUP;
import static java.util.stream.Collectors.toList;

/**
 * Rest resource for operations on acls from the ACL spring schema.
 *
 * @author Glen
 */
@Component(value = "aclResource")
@Path("/acl")
@Produces({"application/json"})
@Consumes({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
@Slf4j
public class AclResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(AclResource.class);
    public static final String AUDIT_LOGGING_PREFIX = "PERMISSIONS_";

    @Autowired
    private AclRestService aclRestService;
    @Autowired
    private AcuityPermissionViewPackagesManager permissionViewPackagesManager;
    /*
     * Methods:
     * GET:
     *      NOT IMPL /acl/DrugProgramme/1                             get DrugProgramme  - not needed
     *      - /acl/DrugProgramme/1/ace                                gets all aces for DrugProgramme 1
     *      - /acl/DrugProgramme/1/acewithscheduled                   gets all aces (with scheduled) for DrugProgramme 1
     *      - /acl/DrugProgramme/1/userace                            gets all user ace from inherited and groups for DrugProgramme 1
     *
     * POST:
     *      -/acl/DrugProgramme/1/deleteace                          json object -> permission=32 userId=glen    delete permission 32 for glen
     * POST:
     *      -/acl/DrugProgramme/1/ace?overwrite=true                 json object -> userId=glen add permission for glen 32, returns uri
     *      -/acl/DrugProgramme/1/ace/dataOwner/glen                 replaces the data owner for DrugProgramme 1 with glen
     *      -/acl/DrugProgramme/1/schedule/ace                       schedule adding of an Ace
     * POST:
     *      NOT IMPL /acl/objectidentity                             DrugProgramme/1?name='Drug A'  add new drug program - not needed
     * POST:
     *      /acl/DetectDataset/1/setlockdown                   json -> true Sets the lockdown of DetectDataset id 1 to true
     */

    /**
     * Gets all the Ace permissions for a AcuityAclObjectIdentity.
     * <p>
     * GET: /acl/DrugProgramme/1/ace
     */
    @GET
    @Path("/{objectIdentityClassname}/{objectIdentityId}/ace")
    @PreAuthorize("hasPermission(#objectIdentityId, #objectIdentityClassname, 'VIEW_VISUALISATIONS') or hasRole('DEVELOPMENT_TEAM')")
    public List<AcuitySidDetailsWithPermissionMaskAndGranted> getAllPermissionForAcl(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("GET: get permissions for " + acuityAclObjectIdentity);

        try {
            return securityAclService.getAllPermissionForAcl(acuityAclObjectIdentity);
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable get all permissions to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException(acuityAclObjectIdentity + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets all the user ace permissions for a AcuityAclObjectIdentity, including inherited and grouped user
     * <p>
     * GET: /acl/DrugProgramme/1/useraces
     */
    @GET
    @Path("/{objectIdentityClassname}/{objectIdentityId}/useraces")
    @PreAuthorize("hasPermission(#objectIdentityId, #objectIdentityClassname, 'VIEW_VISUALISATIONS') or hasRole('DEVELOPMENT_TEAM')")
    public List<AcuitySidDetailsWithPermissionMask> getGrantedUsersForForAcl(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("GET: get all users permissions for " + acuityAclObjectIdentity);

        try {
            return securityAclService.getGrantedUsersForAcl(acuityAclObjectIdentity);
        } catch (NotFoundException nfe) {
            LOG.error("Unable get all users all permissions to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException(acuityAclObjectIdentity + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets all the user ace permissions, including inherited and grouped user
     * <p>
     * GET: /acl/userpermissions/ksnqi199
     */
    @GET
    @Path("/userpermissions/{username}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public List<AcuityObjectIdentityWithPermissionAndLockDown> getUserAcls(@PathParam("username") String username) {

        AcuitySidDetails acuityUserDetails = userService.getUser(username);

        LOG.debug("GET: get acls for User " + acuityUserDetails);

        return securityAclService.getUserObjectIdentities(acuityUserDetails);
    }

    @GET
    @Path("/userdatasetpermissions/{username}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public List<DrugStudyDatasetWithPermission> getUserDatasetPermissions(@PathParam("username") String username) {

        AcuitySidDetails acuityUserDetails = userService.getUser(username);

        LOG.debug("GET: get acls for User " + acuityUserDetails);

        return securityAclService.getUserDatasetPermissions(acuityUserDetails);
    }

    /**
     * Cant set lockdown
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/setlockdown")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and hasPermission(#objectIdentityId, #objectIdentityClassname, 'EDIT_AUTHORISED_USERS')")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "SET_LOCKDOWN", value = {
            @LogArg(arg = 0, name = ACL_CLASSNAME),
            @LogArg(arg = 1, name = ACL_ID)
    })
    public Response setLockdown(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        Dataset detectDataset = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);

        LOG.debug("POST: setting lockdown status {} for {}", true, detectDataset);

        return setLockdown(detectDataset, true);
    }

    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/unsetlockdown")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and hasPermission(#objectIdentityId, #objectIdentityClassname, 'EDIT_AUTHORISED_USERS')")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "UNSET_LOCKDOWN", value = {
            @LogArg(arg = 0, name = ACL_CLASSNAME),
            @LogArg(arg = 1, name = ACL_ID)
    })
    public Response unsetLockdown(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        Dataset detectDataset = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);

        return setLockdown(detectDataset, false);
    }

    private Response setLockdown(Dataset detectDataset, boolean lockdownStatus) {
        LOG.debug("POST: setting lockdown status {} for {}", lockdownStatus, detectDataset);

        try {
            securityAclService.setLockdownStatus((DetectDataset) detectDataset, lockdownStatus);

            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            LOG.error("Unable to find {}", detectDataset, iae);
            throw new WebApplicationException(detectDataset + " not found", Response.Status.NOT_FOUND);
        } catch (Exception nfe) {
            LOG.error("Unable to set lockdown status {} for {}", lockdownStatus, detectDataset, nfe);
            throw new WebApplicationException("Unable to set lockdown status " + lockdownStatus + " for " + detectDataset,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/setInheritPermission")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and hasPermission(#objectIdentityId, #objectIdentityClassname, 'EDIT_AUTHORISED_USERS')")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "ENABLE_INHERITED_PERMISSION", value = {
            @LogArg(arg = 0, name = ACL_CLASSNAME),
            @LogArg(arg = 1, name = ACL_ID)
    })
    public Response setInheritPermission(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        Dataset detectDataset = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);

        return setInheritPermission(detectDataset, true);
    }

    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/unsetInheritPermission")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and hasPermission(#objectIdentityId, #objectIdentityClassname, 'EDIT_AUTHORISED_USERS')")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "DISABLE_INHERITED_PERMISSION", value = {
            @LogArg(arg = 0, name = ACL_CLASSNAME),
            @LogArg(arg = 1, name = ACL_ID)
    })
    public Response unsetInheritPermission(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId) {

        Dataset detectDataset = AcuityObjectIdentityImpl.createDataset(objectIdentityClassname, objectIdentityId);

        return setInheritPermission(detectDataset, false);
    }

    private Response setInheritPermission(Dataset detectDataset, boolean inheritedStatus) {
        LOG.debug("POST: setting Inherit Permissions status {} for {}", inheritedStatus, detectDataset);

        try {
            securityAclService.setInheritPermissions(detectDataset, inheritedStatus);

            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            LOG.error("Unable to find {}", detectDataset, iae);
            throw new WebApplicationException(detectDataset + " not found", Response.Status.NOT_FOUND);
        } catch (Exception nfe) {
            LOG.error("Unable to set inherit permissions status {} for {}", true, detectDataset, nfe);
            throw new WebApplicationException("Unable to set inherit permissions status status " + true + " for " + detectDataset,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds an Ace permission for a AcuityAclObjectIdentity for the user with permission mask.
     * <p>
     * If overwrite=true all other permissions for this object for this user are removed, default is true.
     * <p>
     * POST: /acl/DrugProgramme/1/ace?overwrite=true with Json object permissionMask=32 userId=glen
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/ace")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and @pe.hasAddPermission(authentication, #objectIdentityId, #objectIdentityClassname, #up.permissionMask)")
    public Response addPermissionForASid(
            @Context UriInfo uriInfo,
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            @QueryParam("overwrite") @DefaultValue("true") boolean overwrite,
            UserPermission up) {

        return aclRestService.addPermissionForASid(uriInfo, objectIdentityClassname, objectIdentityId, overwrite, up);
    }

    /**
     * Adds new permissions for batch upload, but is also used to swap user permissons.
     * <p>
     * Because of this the check param is used, if its true, as for batch upload it checks if the user already has permissions, if so does nothing.
     * <p>
     * For swap users, it doesnt check and swaps the permissions.
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/aces")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and @pe.hasAddPermission(authentication, #objectIdentityId, #objectIdentityClassname, #ups[0].permissionMask)")
    @Transactional
    public List<AcuitySidDetailsWithPermissionMaskAndGranted> addPermissionsForSids(
            @Context UriInfo uriInfo,
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            @QueryParam("check") @DefaultValue("true") boolean check,
            List<UserPermission> ups) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("POST: adding permissions for users " + ups);

        try {
            int permissionMask = ups.get(0).getPermissionMask();
            // the security on the method is done by the fisrt permission mask, so make sure more than one isnt been passed to bypass security
            if (ups.stream().map(up -> up.getPermissionMask()).distinct().count() != 1) {
                LOG.error("More than 1 permission mask in the array of UserPermissions");
                throw new WebApplicationException("Permission masks must all be the same", Response.Status.PRECONDITION_FAILED);
            }

            // checks that all prids are valid before an interaction with db
            List<String> invalidPrids = new ArrayList<>();
            for (UserPermission up : ups) {
                boolean isValid = true;
                if (up.getAcuitySidDetails().isGroup()) {
                    isValid = userService.groupExists(up.getAcuitySidDetails().getSidAsString());
                } else {
                    isValid = userService.isValidUser(up.getAcuitySidDetails().getSidAsString());
                }
                if (!isValid) {
                    invalidPrids.add(up.getAcuitySidDetails().getSidAsString());
                }
            }
            if (invalidPrids.size() > 0) {
                throw new IllegalArgumentException("Unknown " + (invalidPrids.size() == 1 ? "prid" : "prids") + ": "
                        + String.join(",", invalidPrids.toArray(new String[invalidPrids.size()])));
            }
            for (UserPermission up : ups) {
                if (!up.getAcuitySidDetails().isGroup()) {
                    // always need user in the user db before adding them to the security ACL db, also default them to training group
                    userService.createUserIfNotExistAndAddToGroup(up.getAcuitySidDetails().getSidAsString(), TRAINED_USER_GROUP);
                }

                if (check) { // for batch upload to not override current permissions
                    AccessControlEntry userAce = securityAclService.getSidsAceForAcl(acuityAclObjectIdentity, up.getAcuitySidDetails().toSid());

                    // if the user has already been added then dont add
                    if (userAce == null) {
                        //this replaces the old granting permission to the new one
                        aclRestService.addPermissionForASid(uriInfo, objectIdentityClassname, objectIdentityId, true, up);
                    }
                } else { //swap permissions
                    //this replaces the old granting permission to the new one
                    aclRestService.addPermissionForASid(uriInfo, objectIdentityClassname, objectIdentityId, true, up);
                }
            }
            return securityAclService.getAllPermissionForAcl(acuityAclObjectIdentity).
                    stream().filter(p -> p.getPermissionMask().equals(permissionMask)).collect(toList());

        } catch (IllegalArgumentException iae) {
            LOG.error("Invalid prid", iae);
            Response response = Response.status(Status.BAD_REQUEST).entity(iae.getMessage()).type(MediaType.TEXT_PLAIN).build();
            throw new WebApplicationException(iae.getMessage(), response);
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable to add permissions " + ups + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException(acuityAclObjectIdentity + " not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Removes an Ace permission for a AcuityAclObjectIdentity for the user with (optional) permission mask.
     * <p>
     * <code>
     * POST: /acl/DrugProgramme/1/deleteace  with UserPermission with permissionMask    removes single permission
     * POST: /acl/DrugProgramme/1/deleteace with UserPermission with no permissionMask  removes all users permissions
     * </code>
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/deleteace")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and @pe.hasRemovePermission(authentication, #objectIdentityId, #objectIdentityClassname,"
            + " #up.acuitySidDetails.getSidAsString(), #up.permissionMask)")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "REMOVE_ACE", logOnlyOnSuccess = true, value = {
            @LogArg(arg = 0, name = ACL_CLASSNAME),
            @LogArg(arg = 1, name = ACL_ID),
            @LogArg(arg = 2, name = "ACE_USER", expression = "acuitySidDetails.getSidAsString()"),
            @LogArg(arg = 2, name = "ACE_ISGROUP", expression = "acuitySidDetails.isGroup()"),
            @LogArg(arg = 2, name = "ACE_PERMISSION", expression = "permissionMask")
    })
    public Response removePermissionForASid(
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            UserPermission up) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        Integer permissionMask = up.getPermissionMask();
        String sid = up.getAcuitySidDetails().getSidAsString();
        try {
            if (null == permissionMask || permissionMask == 0) {
                LOG.debug("POST: remove all permissions for User " + sid + " to " + acuityAclObjectIdentity);
                securityAclService.removeAces(acuityAclObjectIdentity, up.getAcuitySidDetails().toSid());
            } else {
                LOG.debug("POST: remove permission " + permissionMask + " for User " + sid + " to " + acuityAclObjectIdentity);
                securityAclService.removeAce(acuityAclObjectIdentity, permissionMask, up.getAcuitySidDetails().toSid());
            }

            return Response.noContent().build();
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable to remove permission " + permissionMask + " for Sid " + sid + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException(acuityAclObjectIdentity + " not found", Response.Status.NOT_FOUND);
        } catch (Exception nfe) {
            LOG.error("Unable to remove scheduled permission for Sid " + sid + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException(acuityAclObjectIdentity + " not found", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates an Acl for a AcuityAclObjectIdentity.
     * <p>
     * POST: /acl?ownerId=dsdsd Json object AcuityObjectIdentityWithParent. Owner optional
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
                              @QueryParam("ownerId") String ownerId, AcuityObjectIdentityImpl acuityObjectIdentityWithParent) {

        return aclRestService.createAcl(uriInfo, ownerId, acuityObjectIdentityWithParent);
    }

    /**
     * Set a list of view packages permission for a AcuityAclObjectIdentity for the user.
     * <p>
     * <p>
     * POST: /acl/DrugProgramme/1/viewpackagesaces with Json object permissionMasks=[32, 2] userId=glen
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/viewpackagesaces")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and @pe.hasAddPermission(authentication, #objectIdentityId, #objectIdentityClassname, "
            + " T(com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles).AUTHORISED_USER.getMask())")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "SET_VIEW_PACKAGES", value = {
            @LogArg(arg = 1, name = ACL_CLASSNAME),
            @LogArg(arg = 2, name = ACL_ID),
            @LogArg(arg = 3, name = "ACE_USER", expression = "acuitySidDetails.getSidAsString()"),
            @LogArg(arg = 3, name = "ACE_PERMISSIONS", expression = "viewPermissionMasks"),
            @LogArg(arg = 3, name = "ACE_PERMISSIONS_AS_STRINGS", expression = "getViewPermissionMasksAsStrings()")
    })
    public Response setViewPackagePermissionForASid(
            @Context UriInfo uriInfo,
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            ViewUserPermission vup) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("POST: setting view packages permissions " + vup.getViewPermissionMasks()
                + " for User " + vup.getAcuitySidDetails().getSidAsString()
                + " to " + acuityAclObjectIdentity);

        try {
            securityAclService.setViewPackagesAces(acuityAclObjectIdentity, vup.getViewPermissionMasks(), vup.getAcuitySidDetails());

            return Response.ok().build();
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable to set view packages permissions " + vup.getViewPermissionMasks()
                    + " for User " + vup.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException("Unable to set view packages permissions " + vup.getViewPermissionMasks()
                    + " for User " + vup.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Set a list of view packages permission for a AcuityAclObjectIdentity for the user.
     * <p>
     * <p>
     * POST: /acl/DrugProgramme/1/viewpackagesaces with Json object permissionMasks=[32, 2] userId=glen
     */
    @POST
    @Path("/{objectIdentityClassname}/{objectIdentityId}/viewextrapackagesaces")
    @PreAuthorize("hasRole('ACL_ADMINISTRATOR') "
            + "and @pe.hasAddPermission(authentication, #objectIdentityId, #objectIdentityClassname, "
            + " T(com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles).AUTHORISED_USER.getMask())")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "SET_EXTRA_VIEW_PACKAGES", value = {
            @LogArg(arg = 1, name = ACL_CLASSNAME),
            @LogArg(arg = 2, name = ACL_ID),
            @LogArg(arg = 3, name = "ACE_USER", expression = "acuitySidDetails.getSidAsString()"),
            @LogArg(arg = 3, name = "ACE_PERMISSIONS", expression = "viewPermissionMasks"),
            @LogArg(arg = 3, name = "ACE_PERMISSIONS_AS_STRINGS", expression = "getViewPermissionMasksAsStrings()")
    })
    public Response setExtraViewPackagePermissionForASid(
            @Context UriInfo uriInfo,
            @PathParam("objectIdentityClassname") String objectIdentityClassname,
            @PathParam("objectIdentityId") Long objectIdentityId,
            ViewUserPermission vup) {

        AcuityObjectIdentity acuityAclObjectIdentity = AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId);

        LOG.debug("POST: setting extra view packages permissions " + vup.getViewPermissionMasks() + " for "
                + "User " + vup.getAcuitySidDetails().getSidAsString() + " to " + acuityAclObjectIdentity);

        try {
            securityAclService.setExtraViewPackagesAces(acuityAclObjectIdentity, vup.getViewPermissionMasks(), vup.getAcuitySidDetails());

            return Response.ok().build();
        } catch (org.springframework.security.acls.model.NotFoundException nfe) {
            LOG.error("Unable to set extra view packages permissions " + vup.getViewPermissionMasks()
                    + " for User " + vup.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, nfe);
            throw new WebApplicationException("Unable to set extra view packages permissions " + vup.getViewPermissionMasks()
                    + " for User " + vup.getAcuitySidDetails().getSidAsString()
                    + " to " + acuityAclObjectIdentity, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Set a list of view packages permission for a AcuityAclObjectIdentity for the user.
     * <p>
     * <p>
     * POST: /acl/DrugProgramme/1/viewpackagesaces with Json object permissionMasks=[32, 2] userId=glen
     */
    @GET
    @Path("/viewextrapackages")
    public Map<String, List<AcuityPermissions>> getListOfExtraPackages() {
        try {
            Map<String, List<AcuityPermissions>> types = new HashMap<>();
            types.put("view", permissionViewPackagesManager.getAllViewPackages());
            types.put("extra", permissionViewPackagesManager.getExtraViewPackages());
            types.put("all", AcuityPermissions.getAllWithoutVisualisations());
            return types;
        } catch (Throwable t) {
            log.error("ERROR occurred when trying to get extra packages list!", t);
            throw new RuntimeException(t);
        }
    }
}
