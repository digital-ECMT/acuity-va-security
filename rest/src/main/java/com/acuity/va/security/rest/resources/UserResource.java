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

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.ActiveDirectoryRecord;
import com.acuity.va.security.common.service.PeopleResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Rest resource for operations on users from the GBAC spring schema
 *
 * @author Glen
 */
@Component
@Path("/user")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class UserResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private PeopleResourceClient peopleResourceClient;

    /*
     * Methods:
     * GET:
     *      /user                          lists all trained user
     *      /user/{userId}                 get user {userId}
     *      /user/search/{surname}         search user by {surname}
     * DELETE
     *      /user/{userId}                 delete user {userId}
     * PUT
     *      /user/{userId}                 creates user {userId}
     * POST
     *      /user/{userId}/link/{toUserId} links to userId to toUserId
     *      /user/{userId}/unlink          unlinks userId
     *
     */
    @GET
    @Path("/{userId}")
    public AcuitySidDetails getUser(@PathParam("userId") String userId) {
        LOG.info("GET: user " + userId);
        boolean exists = userService.userExists(userId);
        if (!exists) {
            throw new WebApplicationException("User: " + userId + " not found", Response.Status.NOT_FOUND);
        } else {
            return userService.getUser(userId);
        }
    }

    /**
     * Deletes a user
     *
     * @param userId
     * @return
     */
    @DELETE
    @Path("/{userId}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response deleteUser(@PathParam("userId") String userId) {
        LOG.info("DELETE: user " + userId);
        boolean exists = userService.userExists(userId);
        if (!exists) {
            throw new WebApplicationException("User: " + userId + " not found", Response.Status.NOT_FOUND);
        } else {
            userService.deleteUser(userId);
            return Response.noContent().build();
        }
    }

    /**
     * Adds a user to the users security db, adds them with the default role (that means nothing)
     *
     * @param uriInfo used to build response, autowired
     * @param userId prid
     * @param fullName Firstname plus surname
     * @return Rest endpoint for that user
     */
    @PUT
    @Path("/{userId}/{fullName}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    @Produces("text/plain")
    public Response createUser(@Context UriInfo uriInfo, @PathParam("userId") String userId, @PathParam("fullName") String fullName) {
        LOG.info("PUT: user " + userId + ", fullName " + fullName);
        boolean exists = userService.userExists(userId);
        if (exists) {
            throw new WebApplicationException("User: " + userId + " already exists", Response.Status.CONFLICT);
        } else {
            userService.createUser(userId, fullName);
            Response.ResponseBuilder resp = Response.created(uriInfo.getAbsolutePathBuilder().build());
            return resp.entity(userId).build();
        }
    }

    /**
     * Searches all domains in AD for user with surname
     *
     * @param surname so search for
     * @return List of users
     */
    @GET
    @Path("/search/{surname}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public List<ActiveDirectoryRecord> searchUsers(@PathParam("surname") String surname) {
        LOG.info("GET: search surname " + surname);

        return peopleResourceClient.searchUsersByName(surname);
    }

    /**
     * Links the userId account to toLinkToUserId
     *
     * @param userId userId to add a link to
     * @param toLinkToUserId userId to link to
     */
    @POST
    @Path("/{userId}/link/{toLinkToUserId}")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response linkUser(@PathParam("userId") String userId, @PathParam("toLinkToUserId") String toLinkToUserId) {
        LOG.info("POST: link " + userId + " to " + toLinkToUserId);

        try {
            userService.linkUser(userId, toLinkToUserId);
        } catch (Exception ex) {
            LOG.error("User: " + toLinkToUserId + " has already been linked", ex);
            throw new WebApplicationException("User: " + toLinkToUserId + " has already been linked", Response.Status.CONFLICT);
        }
        return Response.noContent().build();
    }

    /**
     * Unlinks the userId account
     *
     * @param userId userId to unlink
     */
    @POST
    @Path("/{userId}/unlink")
    @PreAuthorize("hasRole('DEVELOPMENT_TEAM')")
    public Response unlinkUser(@PathParam("userId") String userId) {
        LOG.info("POST: unlink " + userId);

        userService.unlinkUser(userId);
        return Response.noContent().build();
    }
}
