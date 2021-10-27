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

package com.acuity.va.auditlogger.service;

import com.acuity.va.auditlogger.annotation.LogArg;
import com.acuity.va.auditlogger.annotation.LogOperation;
import org.springframework.stereotype.Component;

/**
 * Examples of how to use this framework for acl operations
 *
 * @author Glen
 */
@Component
public class UserGroupResource {

    @LogOperation(name = "PERMISSIONS_REMOVE_USER_FROM_GROUP", value = {
        @LogArg(arg = 0, name = "ACE_USER"),
        @LogArg(arg = 1, name = "ACE_GROUP")
    })
    public String removeUserFromGroup(String userId, String group) {
        return "done";
    }

    @LogOperation(name = "PERMISSIONS_ADD_USER_TO_GROUP", value = {
        @LogArg(arg = 0, name = "ACE_USER"),
        @LogArg(arg = 1, name = "ACE_GROUP")
    })
    public String addUserToGroup(String userId, String group) {
        return "done";
    }
}
