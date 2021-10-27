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

package com.acuity.va.security.common;

/**
 * Constants file for all application wide information.
 *
 * @author Glen
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Prefix of all cache names that can be refreshed on a schedule.
     */
    public static final String REFRESHABLE_CACHE = "refreshable-";
    public static final String HOURLY_REFRESHABLE_CACHE = "hourly-refreshable-";

    ////  TRAINED USER GROUPS AND AUTHORITIES  /////
    ////////////////////////////////////////////////////

    // Trained user group in spring security
    public static final String TRAINED_USER_GROUP = "TRAINED_USER_GROUP";
    // Trained user authority/role in spring security
    public static final String TRAINED_USER_AUTHORITY = "TRAINED_USER";

    ////  ACL ADMINISTRATOR GROUPS AND AUTHORITIES  /////
    ////////////////////////////////////////////////////

    // Group that everyone is added to allow people to modify Acls
    public static final String ACL_ADMINISTRATOR_GROUP = "ACL_ADMINISTRATOR_GROUP";
    // role that everyone is added to allow people to modify Acls
    public static final String ACL_ADMINISTRATOR_AUTHORITY = "ACL_ADMINISTRATOR";

    ////  DEVELOPMENT TEAM GROUPS AND AUTHORITIES  /////
    ////////////////////////////////////////////////////

    // Group for developers
    public static final String DEVELOPMENT_GROUP = "DEVELOPMENT_GROUP";
    // Group for support
    public static final String ACUITY_SUPPORT_GROUP = "ACUITY_SUPPORT";
    // role for devs
    public static final String DEVELOPMENT_TEAM_AUTHORITY = "DEVELOPMENT_TEAM";

    // Development team AD group
    public static final String ACUITY_DEVELOPMENT_AD_GROUP = "ACUITY_DEVELOPMENT";
    // Trained user AD group
    public static final String ACUITY_TRAINED_USERS_AD_GROUP = "UKAPDU-ACUITY-APPLICATION-USERS";

    public static final String ACTUATOR_ENV_KEY = "env";
}
