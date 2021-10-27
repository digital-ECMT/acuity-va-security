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

package com.acuity.va.security.rest.config;

import com.acuity.va.auditlogger.config.AuditLoggerConfiguration;
import com.acuity.va.security.rest.resources.AclResource;
import com.acuity.va.security.rest.resources.GroupResource;
import com.acuity.va.security.rest.resources.LoggingResource;
import com.acuity.va.security.rest.resources.MyAnalyticsResource;
import com.acuity.va.security.rest.resources.MyResource;
import com.acuity.va.security.rest.resources.SecurityResource;
import com.acuity.va.security.rest.resources.UserGroupResource;
import com.acuity.va.security.rest.resources.UserResource;
import com.acuity.va.validators.config.ValidatorsConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.ws.rs.ApplicationPath;

@Configuration
@Import({
        AuditLoggerConfiguration.class,
        ValidatorsConfiguration.class
})
@ApplicationPath("/resources")
public class RestConfiguration extends ResourceConfig {

    @Autowired
    public RestConfiguration(JacksonFeature jacksonFeature) {
        registerClasses(
                AclResource.class,
                SecurityResource.class,
                MyResource.class,
                UserResource.class,
                UserGroupResource.class,
                GroupResource.class,
                MyAnalyticsResource.class,
                LoggingResource.class,
                HttpMethodOverrideFilter.class
        );

        register(jacksonFeature);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }

}
