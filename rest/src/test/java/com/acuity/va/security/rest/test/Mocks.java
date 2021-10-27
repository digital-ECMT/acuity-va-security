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

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Glen
 */
public class Mocks {

    public static UsernamePasswordAuthenticationToken createAuthentication(String principal, List<String> groups) {
        return new UsernamePasswordAuthenticationToken(principal, "pass", groups.stream().map(g -> new SimpleGrantedAuthority(g)).collect(toList()));
    }

    public static UserDetails createUserDetails(String principal, List<String> groups) {
        return new User(principal, "pass", true, true, true, true, groups.stream().map(g -> new SimpleGrantedAuthority(g)).collect(toList()));
    }
}
