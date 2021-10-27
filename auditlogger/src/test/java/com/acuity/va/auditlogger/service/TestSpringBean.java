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
 * Examples of how to use this framework
 *
 * @author Glen
 */
@Component
public class TestSpringBean {

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = -1, name = "Return-Value"),
        @LogArg(arg = 0, name = "Input-Value"),})
    public String sayHello(String name) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = -1, name = "Return-Value"),
        @LogArg(arg = 0, name = "Input-Value1"),
        @LogArg(arg = 1, name = "Input-Value2", expression = "toString()"),})
    public String sayHello2(String name, Long id) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = -1, name = "Return-Value"),
        @LogArg(arg = 0, name = "Input-Value1", persistNull = false),
        @LogArg(arg = 1, name = "Input-Value2", expression = "toString()"),})
    public String sayHello2IgnoreNull(String name, Long id) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = -1, name = "Return-Value"),
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true),
        @LogArg(arg = 1, name = "Input-Value2", expression = "toString()"),})
    public String sayHello2NotIgnoreNull(String name, Long id) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true),
        @LogArg(arg = 1, name = "Input-Value2", expression = "toString()"),
        @LogArg(name = "Input-Constant", constStringValue = "GLEN"),})
    public String sayHello2ConstantString(String name, Long id) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true),
        @LogArg(arg = 1, name = "Input-Value2", expression = "toString()"),
        @LogArg(name = "Input-Constant", constIntegerValue = "1")})
    public String sayHello2ConstantInteger(String name, Long id) {
        return "Hello " + name;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true),
        @LogArg(arg = 1, name = "Input-StaticMethod", staticClass = "org.apache.commons.lang3.BooleanUtils", expression = "toInteger()")})
    public String sayHelloWithStaticMethodWithArg(String name, boolean value) {
        return "Hello " + value;
    }

    @LogOperation(name = "sayHello-Variable", value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true),
        @LogArg(name = "Input-StaticMethod", staticClass = "com.acuity.va.auditlogger.service.TestSpringBean", expression = "getName()"),})
    public String sayHelloWithStaticMethodWithOutArg(String name, boolean value) {
        return "Hello " + value;
    }

    @LogOperation(name = "sayHello-LogException", value = {
        @LogArg(arg = -1, name = "Input-ReturnValue1", persistNull = true)
    })
    public String sayHelloLogExceptionReturn(String name) throws Exception {
        throw new Exception("Exception in method");
    }

    @LogOperation(name = "sayHello-Exception", value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true)
    })
    public String sayHelloException(String name) throws Exception {
        throw new Exception("Exception in method");
    }

    @LogOperation(name = "sayHello-Exception", logOnlyOnSuccess = true, value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true)
    })
    public String sayHelloDontLogException(String name) throws Exception {
        throw new Exception("Exception in method");
    }

    @LogOperation(name = "sayHello", logOnlyOnSuccess = true, value = {
        @LogArg(arg = 0, name = "Input-Value1", persistNull = true)
    })
    public String sayHelloDoLog(String name) {
        return "Hello " + name;
    }

    public static String getName() {
        return "name without arg";
    }

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
        @LogArg(arg = 4, name = "ACE_PERMISSION", constIntegerValue = "1"),
        @LogArg(arg = 4, name = "ACE_GRANTING", constIntegerValue = "1"),
        @LogArg(arg = 4, name = "ACE_ISGROUP", constIntegerValue = "0"),
        @LogArg(arg = 3, name = "OVERWRITE_ALL_FOR_USER", constIntegerValue = "1")
    })
    public String addAce(String classname, Long id, String user) throws Exception {
        return "done";
    }
    
    @LogOperation(name = "DATASETS_TEST_RB", value = {
        @LogArg(arg = 0, name = "DATASETS", expression = "getDatasetsLoggingObject()", isDatasetsLoggingObject = true)
    })
    public String datasetsTestRB(DatasetsRequestTest requestBody) throws Exception {
        return "done";
    }
}
