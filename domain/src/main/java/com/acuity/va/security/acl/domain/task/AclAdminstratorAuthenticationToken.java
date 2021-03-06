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

package com.acuity.va.security.acl.domain.task;

import com.google.common.collect.Lists;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Class extends UsernamePasswordAuthenticationToken to add a user with ACL_ADMINISTRATOR role as spring
 * requires this to add and remove stuff from the acl
 *
 * Created by Glen on 03/09/2014.
 */
public class AclAdminstratorAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public AclAdminstratorAuthenticationToken(Object principal) {
        super(principal, "pass", Lists.newArrayList(new SimpleGrantedAuthority("ACL_ADMINISTRATOR")));
    }
}
