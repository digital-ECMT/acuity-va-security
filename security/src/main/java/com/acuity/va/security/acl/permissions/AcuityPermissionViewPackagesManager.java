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

package com.acuity.va.security.acl.permissions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_BASE_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_MACHINE_INSIGHTS_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_ONCOLOGY_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_PROACT_PACKAGE;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;

@Component
public class AcuityPermissionViewPackagesManager {

    @Value("${azureml.enable:false}")
    private boolean amlEnable;

    public List<AcuityPermissions> getAllViewPackages() {
        List<AcuityPermissions> all = new ArrayList<>(getExtraAndBaseViewPackages());
        all.add(VIEW_VISUALISATIONS);
        return all;
    }

    public List<AcuityPermissions> getExtraViewPackages() {
        List<AcuityPermissions> extra = new ArrayList<>();
        extra.add(VIEW_ONCOLOGY_PACKAGE);
        extra.add(VIEW_PROACT_PACKAGE);
        if (amlEnable) {
            extra.add(VIEW_MACHINE_INSIGHTS_PACKAGE);
        }
        return extra;
    }

    private List<AcuityPermissions> getExtraAndBaseViewPackages() {
        List<AcuityPermissions> allView = new ArrayList<>(getExtraViewPackages());
        allView.add(VIEW_BASE_PACKAGE);
        return allView;
    }
}
