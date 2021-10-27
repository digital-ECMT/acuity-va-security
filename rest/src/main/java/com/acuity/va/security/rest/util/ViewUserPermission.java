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

package com.acuity.va.security.rest.util;

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Glen
 */
@Data
public class ViewUserPermission {

    private AcuitySidDetails acuitySidDetails;
    private List<Integer> viewPermissionMasks;

    @JsonIgnore
    public List<String> getViewPermissionMasksAsStrings() {
        if (viewPermissionMasks != null) {
            return viewPermissionMasks.stream().map(AcuityPermissions::getStringMask).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @JsonIgnore
    public Integer getMaxPermission() {
        if (viewPermissionMasks != null) {
            return viewPermissionMasks.stream().max(Comparator.naturalOrder()).get();
        } else {
            return 0;
        }
    }

}
