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

import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.GroupWithLockdown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.acuity.va.security.acl.domain.GroupAuthorityConverter.toAuthority;

/**
 * Rest resource for operations on groups on the GBAC spring schema
 *
 * @author Glen
 */
@Component
@Path("/groups")
@Produces({"application/json"})
@PreAuthorize("hasRole('TRAINED_USER')")
public class GroupResource extends BaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(GroupResource.class);

    /*
     * Methods:
     * GET:
     *      /groups/{group}  list all users in a group
     *      /groups/not/{group}  list all users not in a group
     */
    /**
     * Lists all the users of a particular group
     *
     * @param group group name
     * @return list of AcuityUserDetails who belong to a group
     */
    @GET
    @Path("/{group}")
    public List<AcuitySidDetails> listUsersForGroup(@PathParam("group") String group) {
        LOG.info("GET: list all acuity users for group " + group);
        groupExists(group);

        return userService.getAcuityUsersByGroup(group);
    }

    /**
     * Lists all the users of not in a particular group
     *
     * @param group group name
     * @return list of AcuityUserDetails who dont belong to a group
     */
    @GET
    @Path("/not/{group}")
    public List<AcuitySidDetails> listUsersNotInGroup(@PathParam("group") String group) {
        LOG.info("GET: list all acuity users not in group " + group);
        groupExists(group);

        return userService.getAcuityUsersNotInGroup(group);
    }

    /**
     * Lists all the groups
     *
     * @return list of groups
     */
    @GET
    @Path("/")
    public List<String> listGroups() {
        LOG.info("GET: list all groups");

        return userService.listGroups();
    }
    
    /**
     * Lists all the groups with lockdown permission status
     *
     * @return list of groups with lockdown permission status
     */
    @GET
    @Path("/withlockdown")
    public List<GroupWithLockdown> listGroupsAndIfInLockdown() {
        LOG.info("GET: list all groups with lockdown");

        return userService.listGroupsWithLockdown();
    }
    
    /**
     * Lists all the groups and dataset names with lockdown permission status
     *
     * @return list of groups dataset names with lockdown permission status
     */
    @GET
    @Path("/withdatasetsandlockdown")
    public List<GroupWithItsLockdownDatasets> listGroupsAndIfInLockdownWithDatasets() {
        LOG.info("GET: list all groups with lockdown");

        return userService.listGroupsWithLockdownAndDatasets();
    }

    /**
     * Adds a group
     */
    @POST
    @Path("/{group}")
    @Produces({"text/plain"})
    public Response addGroup(@Context UriInfo uriInfo, @PathParam("group") String group) {
        LOG.info("POST: adding group " + group);

        userService.createGroup(group);

        Response.ResponseBuilder resp = Response.created(uriInfo.getAbsolutePathBuilder().build());
        return resp.entity(group).build();
    }

    /**
     * removes a group
     */
    @DELETE
    @Path("/{group}")
    public Response deleteGroup(@PathParam("group") String group, @QueryParam("deleteAces") @DefaultValue("true") boolean deleteAces) {
        LOG.info("DELETE: deleting group " + group);
        boolean exists = userService.groupExists(group);
        if (!exists) {
            throw new WebApplicationException("Group: " + group + " not found", Response.Status.NOT_FOUND);
        } else {
            if (deleteAces) {
                securityAclService.removeAllAcesForSid(toAuthority(group));
            }
            userService.removeGroup(group);

            return Response.noContent().build();
        }
    }
}
