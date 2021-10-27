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

package com.acuity.va.security.acl.domain;

import org.apache.commons.lang3.StringUtils;

import static com.acuity.va.security.common.Constants.ACL_ADMINISTRATOR_AUTHORITY;
import static com.acuity.va.security.common.Constants.ACL_ADMINISTRATOR_GROUP;
import static com.acuity.va.security.common.Constants.DEVELOPMENT_GROUP;
import static com.acuity.va.security.common.Constants.DEVELOPMENT_TEAM_AUTHORITY;
import static com.acuity.va.security.common.Constants.TRAINED_USER_AUTHORITY;
import static com.acuity.va.security.common.Constants.TRAINED_USER_GROUP;

/**
 * Converts between the group name and the authority added for that group.
 * <p>
 * Generally now since group were added, the group will be added with a name, and authority will be appended with _AUTHORITY.
 * <p>
 * This wont apply for the current 'static' groups found in com.acuity.visualisations.common.Constants, ie DEVELOPMENT_GROUP,
 * ACL_ADMINISTRATOR_GROUP and TRAINED_USER_GROUP.
 *
 */
public final class GroupAuthorityConverter {

    public static final String AUTHORITY = "_AUTHORITY";

    private GroupAuthorityConverter() {
    }

    public static String toGroup(String authority) {
        if (StringUtils.equals(TRAINED_USER_AUTHORITY, authority)) {
            return TRAINED_USER_GROUP;
        } else if (StringUtils.equals(ACL_ADMINISTRATOR_AUTHORITY, authority)) {
            return ACL_ADMINISTRATOR_GROUP;
        } else if (StringUtils.equals(DEVELOPMENT_TEAM_AUTHORITY, authority)) {
            return DEVELOPMENT_GROUP;
        } else {
            return StringUtils.removeEnd(authority, AUTHORITY);
        }
    }

    public static String toAuthority(String group) {
        if (StringUtils.equals(TRAINED_USER_GROUP, group)) {
            return TRAINED_USER_AUTHORITY;
        } else if (StringUtils.equals(ACL_ADMINISTRATOR_GROUP, group)) {
            return ACL_ADMINISTRATOR_AUTHORITY;
        } else if (StringUtils.equals(DEVELOPMENT_GROUP, group)) {
            return DEVELOPMENT_TEAM_AUTHORITY;
        } else {
            return group + AUTHORITY;
        }
    }
}
