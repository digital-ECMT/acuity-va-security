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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by knml167 on 19/02/2016.
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(callSuper = true)
public class DrugStudyDatasetWithPermission extends DrugStudyDataset {

    private boolean hasPermission = false;
    private int rolePermission = 0;
    private int viewPermission = 0;
    
    public DrugStudyDatasetWithPermission(Dataset dataset, AcuityObjectIdentityWithPermission datasetWithPermission) {
        setDrugProgramme(dataset.drugProgramme);
        setClinicalStudyName(dataset.clinicalStudyName);
        setClinicalStudyCode(dataset.clinicalStudyCode);
        setDatasetName(dataset.name);
        
        if (datasetWithPermission != null) {
            hasPermission = datasetWithPermission.getCanView();
            rolePermission = datasetWithPermission.getRolePermissionMask();
            viewPermission = datasetWithPermission.getViewPermissionMask();
        }
    }
}
