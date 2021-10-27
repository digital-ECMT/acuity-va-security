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

package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import org.junit.Test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuity.va.security.acl.domain.AcuitySidDetails;

import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.test.util.ReflectionTestUtils;


@Ignore
public class AcuitySecurityResourceClientITCase {

    private static final Logger LOG = LoggerFactory.getLogger(AcuitySecurityResourceClientITCase.class);

    private final AcuitySecurityResourceClient acuitySecurityResourceClient = new AcuitySecurityResourceClient();

    @Before
    public void before() {
        ReflectionTestUtils.setField(acuitySecurityResourceClient, "url", "");
        ReflectionTestUtils.setField(acuitySecurityResourceClient, "username", "");
        ReflectionTestUtils.setField(acuitySecurityResourceClient, "password", "");
        
        ReflectionTestUtils.invokeMethod(acuitySecurityResourceClient, "addBasicAuth");
    }
    
    @Test
    public void shouldLoadUserDetails() throws Exception {
        AcuitySidDetails userDetails = acuitySecurityResourceClient.loadUserByUsername("user002");

        System.out.println(userDetails);
    }

    @Test
    public void shouldCheckPermission() throws Exception {
        boolean hasPermission = acuitySecurityResourceClient.hasPermissionForUser("user001", new AcuityDataset(145L), VIEW_VISUALISATIONS.getMask());

        System.out.println(hasPermission);
    }
    
    @Test
    public void shouldGetAclsForUser() throws Exception {
        List<AcuityObjectIdentityWithPermission> aclsForUser = acuitySecurityResourceClient.getAclsForUser("user001");

        System.out.println(aclsForUser);
    }
}
