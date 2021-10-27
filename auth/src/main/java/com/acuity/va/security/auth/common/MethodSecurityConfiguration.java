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

package com.acuity.va.security.auth.common;

import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.acuity.va.security.auth.local.nosecurity.LocalNoSecurityAclPermissionEvaluator;
import com.acuity.va.security.auth.remote.AcuityRemoteAclPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Enables method security and sets up a custom permissionEvaluator (remote back to va security)
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, proxyTargetClass = true)
public class MethodSecurityConfiguration {
        
    @Autowired
    private ISecurityResourceClient securityResourceClient;
    
    @Bean
    public PermissionFactory permissionFactory() {
        return new DefaultPermissionFactory(AcuityPermissions.class);
    }

    @Bean
    @Profile("!local-no-security")
    public PermissionEvaluator permissionEvaluator() {
        AcuityRemoteAclPermissionEvaluator acuityRemoteAclPermissionEvaluator = new AcuityRemoteAclPermissionEvaluator();
        acuityRemoteAclPermissionEvaluator.setPermissionFactory(permissionFactory());
        acuityRemoteAclPermissionEvaluator.setSecurityResourceClient(securityResourceClient);
        return acuityRemoteAclPermissionEvaluator;
    }

    @Bean(name = "permissionEvaluator")
    @Profile("local-no-security")
    public PermissionEvaluator permissionEvaluatorLocal() {
        AcuityRemoteAclPermissionEvaluator acuityRemoteAclPermissionEvaluator = new LocalNoSecurityAclPermissionEvaluator();
        acuityRemoteAclPermissionEvaluator.setPermissionFactory(permissionFactory());
        acuityRemoteAclPermissionEvaluator.setSecurityResourceClient(securityResourceClient);
        return acuityRemoteAclPermissionEvaluator;
    }
}
