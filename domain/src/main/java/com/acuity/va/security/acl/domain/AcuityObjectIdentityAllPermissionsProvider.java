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

package com.acuity.va.security.acl.domain;

public final class AcuityObjectIdentityAllPermissionsProvider {

    private AcuityObjectIdentityAllPermissionsProvider() {
    }

    public static AcuityObjectIdentityWithPermission provide(AcuityObjectIdentity roi) {
        if (roi.thisClinicalStudyType()) {
            return new ClinicalStudyAllPermissions((ClinicalStudy) roi);
        } else if (roi.thisDrugProgrammeType()) {
            return new DrugProgrammeAllPermissions((DrugProgramme) roi);
        } else if (roi.thisAcuityType()) {
            return new AcuityDatasetAllPermissions((AcuityDataset) roi);
        } else if (roi.thisDetectType()) {
            return new DetectDatasetAllPermissions((DetectDataset) roi);
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot provide permissions to non AcuityObjectIdentity instance, but got %s",
                            roi.getClass().getName()));
        }
    }

}
