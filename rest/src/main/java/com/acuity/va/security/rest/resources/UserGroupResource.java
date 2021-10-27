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
import com.acuity.va.security.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Rest resource for operations on users groups on the GBAC spring schema
 *
 * @author Glen
 */
@Component
@Path("/users/{userId}/groups")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class UserGroupResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserGroupResource.class);
    public static final String AUDIT_LOGGING_PREFIX = "PERMISSIONS_";

    /*
     * Methods:
     * GET:
     *      /user/{userId}/groups
     * DELETE:
     *      /user/{userId}/groups/{group}
     * PUT:
     *      /user/{userId}/groups
     *      /user/{userId}/groups/{group}/fullName/{fullName}
     */
    @GET
    public List<String> getGroups(@PathParam("userId") String userId) {
        LOG.info("GET: user's groups " + userId);
        userExists(userId);

        return userService.getUser(userId).getAuthoritiesAsString();
    }

    @DELETE
    @Path("/{group}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "REMOVE_USER_FROM_GROUP", value = {
        @LogArg(arg = 0, name = "ACE_USER"),
        @LogArg(arg = 1, name = "ACE_GROUP")
    })
    public Response deleteUserFromGroup(@PathParam("userId") String userId, @PathParam("group") String group) {
        LOG.info("DELETE: user " + userId + " from group " + group);
        userExists(userId);
        groupExists(group);

        userService.removeUserFromGroup(userId, group);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/trainedUserGroup")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    @Transactional
    public Response deleteUserFromTrainingUserGroup(@PathParam("userId") String userId, @QueryParam("deleteAces") @DefaultValue("true") boolean deleteAces) {
        LOG.info("DELETE: user " + userId + " from trained user group and all aces: " + deleteAces);
        userExists(userId);

        userService.removeUserFromGroup(userId, Constants.TRAINED_USER_GROUP);
        if (deleteAces) {
            securityAclService.removeAllAcesAndGroupsForSid(userId);
        }
        return Response.noContent().build();
    }

    @PUT
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    @Produces("text/plain")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "ADD_USER_TO_GROUP", value = {
        @LogArg(arg = 1, name = "ACE_USER"),
        @LogArg(arg = 2, name = "ACE_GROUP")
    })
    public Response addUserToGroup(@Context UriInfo uriInfo, @PathParam("userId") String userId, String group) {
        userId = userId.toLowerCase();
        LOG.info("PUT: add user " + userId + " to group " + group);
        userExists(userId);
        groupExists(group);

        userService.addUserToGroup(userId, group);

        Response.ResponseBuilder resp = Response.created(uriInfo.getAbsolutePathBuilder().path(group).build());
        return resp.entity(userId).build();
    }

    @PUT
    @Path("/{group}/fullName/{fullName}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    @Produces("text/plain")
    @LogOperation(name = AUDIT_LOGGING_PREFIX + "ADD_USER_TO_GROUP", value = {
        @LogArg(arg = 1, name = "ACE_USER"),
        @LogArg(arg = 2, name = "ACE_GROUP")
    })
    public Response createUserAndAddToGroup(@Context UriInfo uriInfo,
            @PathParam("userId") String userId,
            @PathParam("group") String group,
            @PathParam("fullName") String fullName) {
        userId = userId.toLowerCase();
        LOG.info("PUT: create user " + userId + " with name " + fullName + " and add user to group " + group);
        groupExists(group);

        Response.ResponseBuilder resp;

        if (!userService.userExists(userId)) {

            userService.createUser(userId, fullName);
            LOG.info("User created with UserId : " + userId);

            userService.addUserToGroup(userId, group);
            LOG.info("User added to group : " + group);

            resp = Response.created(uriInfo.getAbsolutePathBuilder().path(group).build());
            return resp.entity(userId).build();

        } else if (!userService.isUserInGroup(userId, group)) {

            userService.addUserToGroup(userId, group);
            LOG.info("User added to group : " + group);

            resp = Response.ok();
            return resp.entity(userId).build();

        } else {

            LOG.info("User : " + userId + " already in group : " + group);
            throw new WebApplicationException("User : " + userId + " already in group : " + group, Response.Status.CONFLICT);
        }
    }
}
