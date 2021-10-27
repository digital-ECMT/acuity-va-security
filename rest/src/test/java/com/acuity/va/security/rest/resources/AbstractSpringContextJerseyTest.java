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

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.service.UserService;
import com.acuity.va.security.common.service.PeopleResourceClient;
import com.acuity.va.security.acl.service.VASecurityResourceFactory;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.acl.service.CustomUserDetailsManager;
import com.acuity.va.security.rest.resources.services.AclRestService;
import com.acuity.va.security.rest.resources.services.MyAnalyticsService;
import com.acuity.va.security.rest.security.Security;
import com.acuity.va.security.rest.test.SpringContextJerseyTest;
import com.acuity.va.security.acl.dao.AcuityObjectService;
import com.acuity.va.security.acl.dao.UserRepository;
import com.acuity.va.security.acl.task.RefreshCachesTask;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Sets;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.util.SocketUtils;

import javax.ws.rs.core.Application;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Glen
 */
public abstract class AbstractSpringContextJerseyTest extends SpringContextJerseyTest {

    public static final String MOCK_SPRING_APPLICATIONCONTEXT = "classpath:/spring/test/mockApplicationContext.xml";

    /**
     * Mock spring services used in SecurityResource
     */
    // Common
    protected SecurityAclService mockSecurityAclService;
    protected UserService mockUserService;
    protected UserRepository mockUserRepository;
    protected Security mockSecurity;
    protected CustomUserDetailsManager mockCustomUserDetailsManager;
    protected MyAnalyticsService mockMyAnalyticsService;
    protected RefreshCachesTask mockRefreshCachesTask;
    protected AclRestService mockAclRestService;
    protected AcuityObjectService mockAcuityObjectService;
    protected AclRepository mockAclRepository;
    protected VASecurityResourceFactory mockVASecurityResourceFactory;

    protected PeopleResourceClient mockPeopleResourceClient;

    /**
     * abstract class to indicate which resources to load as part of the test
     */
    protected abstract Set<Class<?>> getResourcesToLoad();

    @Override
    protected Application configure() {
        Set<Class<?>> resources = Sets.union(getResourcesToLoad(),
            Sets.newHashSet(SerializationFeature.class, DeserializationFeature.class));

        ResourceConfig resourceConfig = new ResourceConfig(resources);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        // Set which application context to use
        resourceConfig.property("contextConfigLocation", MOCK_SPRING_APPLICATIONCONTEXT);
        resourceConfig.property(ServerProperties.PROVIDER_PACKAGES, "com.acuity.acuity.rest.util");
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        // config.register(SerializationFeature.class);
        // config.register(DeserializationFeature.class);
    }

    /**
     * Tests failing on jenkins, so need to check free ports when starting the grizly server
     */
    @Override
    protected int getPort() {
        return SocketUtils.findAvailableTcpPort();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockSecurityAclService = (SecurityAclService) getSpringApplicationContext().getBean("securityAclService");
        assertThat(mockSecurityAclService).isNotNull();
        mockUserService = (UserService) getSpringApplicationContext().getBean("userService");
        assertThat(mockUserService).isNotNull();
        mockUserRepository = (UserRepository) getSpringApplicationContext().getBean("userRepository");
        assertThat(mockUserRepository).isNotNull();
        mockSecurity = (Security) getSpringApplicationContext().getBean("security");
        assertThat(mockSecurity).isNotNull();
        mockCustomUserDetailsManager = (CustomUserDetailsManager) getSpringApplicationContext().getBean("customUserDetailsManager");
        assertThat(mockCustomUserDetailsManager).isNotNull();
        mockPeopleResourceClient = (PeopleResourceClient) getSpringApplicationContext().getBean("peopleResourceClient");
        assertThat(mockPeopleResourceClient).isNotNull();

        mockMyAnalyticsService = (MyAnalyticsService) getSpringApplicationContext().getBean("myAnalyticsService");
        assertThat(mockMyAnalyticsService).isNotNull();

        mockAclRestService = (AclRestService) getSpringApplicationContext().getBean("aclRestService");
        assertThat(mockAclRestService).isNotNull();
        mockAclRepository = (AclRepository) getSpringApplicationContext().getBean("aclRepository");
        assertThat(mockAclRepository).isNotNull();
        mockAcuityObjectService = (AcuityObjectService) getSpringApplicationContext().getBean("acuityObjectService");
        assertThat(mockAcuityObjectService).isNotNull();

        mockVASecurityResourceFactory = (VASecurityResourceFactory) getSpringApplicationContext().getBean("vASecurityResourceFactory");
        assertThat(mockVASecurityResourceFactory).isNotNull();

        mockRefreshCachesTask = (RefreshCachesTask) getSpringApplicationContext().getBean("refreshCachesTask");
        assertThat(mockRefreshCachesTask).isNotNull();
    }

    @After
    public void after() {
        Mockito.reset(mockSecurity, mockUserService, mockUserRepository, mockCustomUserDetailsManager, mockSecurityAclService, 
            mockMyAnalyticsService,  mockAclRestService,
                mockAcuityObjectService,  mockVASecurityResourceFactory,
                      
            mockRefreshCachesTask);
    }
}
