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
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_CLASSNAME;
import static com.acuity.va.auditlogger.aspect.LogOperationAspect.ACL_ID;
import org.springframework.stereotype.Component;

/**
 * Examples of how to use this framework for acl operations
 *
 * @author Glen
 */
@Component
public class AclResource {

    @LogOperation(name = "sayHello-ROI", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID)
    })
    public String sayHelloLogRoi(String classname, Long id) throws Exception {
        return "done";
    }

    @LogOperation(name = "PERMISSIONS_SET_LOCKDOWN", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID)
    })
    public String setLockdown(String classname, Long id) throws Exception {
        return "done";
    }

    @LogOperation(name = "PERMISSIONS_UNSET_LOCKDOWN", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID)
    })
    public String unsetLockdown(String classname, Long id) throws Exception {
        return "done";
    }

    @LogOperation(name = "PERMISSIONS_ADD_ACE", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(arg = 4, name = "ACE_PERMISSION", constIntegerValue = "3"),
        @LogArg(arg = 4, name = "ACE_GRANTING", constIntegerValue = "1"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", constIntegerValue = "0"),
        @LogArg(arg = 3, name = "OVERWRITE_ALL_FOR_USER", constIntegerValue = "1")
    })
    public String addAce(String classname, Long id, String user) throws Exception {
        return "done";
    }
    
    @LogOperation(name = "PERMISSIONS_ADD_ACE", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(arg = 4, name = "ACE_PERMISSION", constIntegerValue = "3"),
        @LogArg(arg = 4, name = "ACE_GRANTING", constIntegerValue = "1"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", constIntegerValue = "1"),
        @LogArg(arg = 3, name = "OVERWRITE_ALL_FOR_USER", constIntegerValue = "1")
    })
    public String addAceGroup(String classname, Long id, String group) throws Exception {
        return "done";
    }
    
    @LogOperation(name = "PERMISSIONS_REMOVE_ACE", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(arg = 4, name = "ACE_PERMISSION", constIntegerValue = "0"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", constIntegerValue = "0"),
    })
    public String removeAce(String classname, Long id, String user) throws Exception {
        return "done";
    }
    
    @LogOperation(name = "PERMISSIONS_REMOVE_ACE", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(arg = 4, name = "ACE_PERMISSION", constIntegerValue = "0"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", constIntegerValue = "1"),
    })
    public String removeAceGroup(String classname, Long id, String group) throws Exception {
        return "done";
    }

    @LogOperation(name = "PERMISSIONS_REPLACE_DATAOWNER", logOnlyOnSuccess = true, value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(name = "ACE_ISGROUP", constIntegerValue = "0"),
        @LogArg(name = "ACE_PERMISSION", constIntegerValue= "12345")
    })
    public String replaceDataOwnerPermission(
            String classname,
            Long id,
            String newDataOwner) {
        return "done";
    }
    
    @LogOperation(name = "PERMISSIONS_REMOVE_ACE", logOnlyOnSuccess = true, value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(name = "ACE_ISGROUP", constIntegerValue = "0"),
        @LogArg(name = "ACE_PERMISSION", constIntegerValue="0")
    })
    public String removeAuthorisor(
            String classname,
            Long id,
            String newAdmin) {
        return "done";
    }
    
    @LogOperation(name = "PERMISSIONS_ADD_ACE", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID),
        @LogArg(arg = 2, name = "ACE_USER"),
        @LogArg(name = "ACE_PERMISSION", constIntegerValue="442383"),
        @LogArg(name = "ACE_GRANTING", constIntegerValue="1"),
        @LogArg(name = "ACE_ISGROUP", constIntegerValue="0"),
        @LogArg(name = "OVERWRITE_ALL_FOR_USER", constIntegerValue="0")
    })
    public String addAuthorisor(
            String classname,
            Long id,
            String newAdmin) {
        return "done";
    }
    
    @LogOperation(name = "STUDY_METADATA", value = {
        @LogArg(arg = 0, name = ACL_CLASSNAME),
        @LogArg(arg = 1, name = ACL_ID)
    })
    public String getStudyMetadata(String classname, Long id) {
        return "done";
    }
    
    
    @LogOperation(name = "PERMISSIONS_REMOVE_USER_FROM_GROUP", value = {
        @LogArg(arg = 0, name = "ACE_USER"),
        @LogArg(arg = 1, name = "ACE_GROUP")
    })
     public String removeUserFromGroup(String user, String group) {
        return "done";
    }
     
    @LogOperation(name = "PERMISSIONS_ADD_USER_TO_GROUP", value = {
        @LogArg(arg = 0, name = "ACE_USER"),
        @LogArg(arg = 1, name = "ACE_GROUP")
    })
     public String addUserToGroup(String user, String group) {
        return "done";
    }
}
