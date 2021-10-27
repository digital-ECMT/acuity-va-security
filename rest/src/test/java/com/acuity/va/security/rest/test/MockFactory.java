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

package com.acuity.va.security.rest.test;

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.rest.resources.services.AclRestService;
import com.acuity.va.security.rest.resources.services.MyAnalyticsService;
import com.acuity.va.security.rest.security.Security;
import com.acuity.va.security.acl.dao.AcuityObjectService;
import com.acuity.va.security.acl.dao.UserRepository;
 import com.acuity.va.security.acl.service.CustomUserDetailsManager;
import com.acuity.va.security.common.service.PeopleResourceClient;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.service.UserService;
import com.acuity.va.security.acl.service.VASecurityResourceFactory;
import com.acuity.va.security.acl.task.RefreshCachesTask;
import org.mockito.Mockito;

/**
 * MockFactory is used to create mocked items with Mockito
 */
public class MockFactory {

    public SecurityAclService createSecurityAclService() {
        return Mockito.mock(SecurityAclService.class);
    }

    public UserService createUserService() {
        return Mockito.mock(UserService.class);
    }

    public UserRepository createUserRespository() {
        return Mockito.mock(UserRepository.class);
    }

    public Security createSecurity() {
        return Mockito.mock(Security.class);
    }

    public AclRepository createAclRepository() {
        return Mockito.mock(AclRepository.class);
    }

    public CustomUserDetailsManager createCustomUserDetailsManager() {
        return Mockito.mock(CustomUserDetailsManager.class);
    }

    public MyAnalyticsService createMyAnalyticsService() {
        return Mockito.mock(MyAnalyticsService.class);
    }

    public AclRestService createAclRestService() {
        return Mockito.mock(AclRestService.class);
    }

    public VASecurityResourceFactory createVASecurityResourceFactory() {
        return Mockito.mock(VASecurityResourceFactory.class);
    }

    public RefreshCachesTask createRefreshCachesTask() {
        return Mockito.mock(RefreshCachesTask.class);
    }

    public PeopleResourceClient createPeopleResourceClient() {
        return Mockito.mock(PeopleResourceClient.class);
    }

    public AcuityObjectService createAcuityObjectService() {
        return Mockito.mock(AcuityObjectService.class);
    }
}
