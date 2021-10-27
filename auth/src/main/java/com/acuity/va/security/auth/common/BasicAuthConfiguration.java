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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(2)
public class BasicAuthConfiguration extends WebSecurityConfigurerAdapter {

    public static final String REMOTE_USER = "REMOTE_USER";

    private BasicAuthProperties properties;

    @Autowired
    public BasicAuthConfiguration(BasicAuthProperties properties) {
        this.properties = properties;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> userDetailsManagerConfigurer = auth.inMemoryAuthentication();

        for (BasicAuthUser user : properties.getUsers()) {
            userDetailsManagerConfigurer
                    .withUser(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getAuthorities())
                    .roles(user.getRoles());
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/resources/security/**")
                .authorizeRequests()
                .anyRequest().hasRole(REMOTE_USER)
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .anonymous().disable();
    }
}
