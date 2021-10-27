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

import com.acuity.va.security.acl.dao.UserRepository;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.service.CustomUserDetailsManager;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.service.UserService;
import com.acuity.va.security.rest.security.Security;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Base resource with all helper methods and autowire common spring beans.
 *
 * All resources should follow the best practices described here:
 *
 * @see
 * http://my.safaribooksonline.com/book/web-development/web-services/9781449383312/crud-web-services/building_crud_services
 * @see https://s3.amazonaws.com/tfpearsonecollege/bestpractices/RESTful+Best+Practices.pdf
 *
 * @author Glen
 */
public class BaseResource {

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected SecurityAclService securityAclService;

    @Autowired
    protected Security security;

    @Autowired
    protected CustomUserDetailsManager customUserDetailsManager;

    /**
     * Checks if the user exists in the database. if not throws an runtime exception that jersey handles and wraps as a
     * status 404
     */
    protected boolean userExists(String username) {
        boolean exists = userService.userExists(username);
        if (!exists) {
            throw new WebApplicationException("User: " + username + " not found", Response.Status.NOT_FOUND);
        } else {
            return true;
        }
    }

    /**
     * Checks if the group exists in the database. if not throws an runtime exception that jersey handles and wraps as a
     * status 404
     */
    protected boolean groupExists(String group) {
        boolean exists = userService.groupExists(group);
        if (!exists) {
            throw new WebApplicationException("Group: " + group + " not found", Response.Status.NOT_FOUND);
        } else {
            return true;
        }
    }

    /**
     * Checks if the user exists is authorised within the application. if not throws an runtime exception that jersey
     * handles and wraps as a status 401
     */
    protected AcuitySidDetails getUser() {
        try {
            return security.getAcuityUserDetails();
        } catch (IllegalStateException ex) {
            throw new WebApplicationException("Unable to establish current user", Response.Status.UNAUTHORIZED);
        }
    }
}
