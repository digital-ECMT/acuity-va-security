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

package com.acuity.va.security.auth.local.nosecurity;

import com.acuity.va.security.auth.remote.AcuityRemoteAclPermissionEvaluator;
import org.springframework.security.core.Authentication;

public class LocalNoSecurityAclPermissionEvaluator extends AcuityRemoteAclPermissionEvaluator {

    @Override
    public boolean hasViewDatasetWithExtraPermission(Authentication authentication, Object targetDomainObject, Object extraPermission) {
        return true;
    }

    @Override
    public boolean hasViewDatasetPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return true;
    }
}
