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

package com.acuity.va.security.acl.assertion;

import java.util.List;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.BooleanAssert;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assert class for asserting the Acl objects
 *
 * @author Glen
 */
public class AclAssert extends AbstractAssert<AclAssert, Acl> {

    public AclAssert(Acl actual) {
        super(actual, AclAssert.class);
    }

    public BooleanAssert isGranted(List<Sid> sids, Permission permission) {
        List<Permission> permissions = newArrayList(permission);

        return isGranted(sids, permissions);
    }
    
    public BooleanAssert isGranted(List<Sid> sids, List<Permission> permissions) {
        try {
            boolean granted = actual.isGranted(permissions, sids, false);
            return (BooleanAssert) assertThat(granted);
        } catch (Exception ex) {
            System.out.println(ex);
            return (BooleanAssert) assertThat(false);
        }
    }
}
