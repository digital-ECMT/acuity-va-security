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

package com.acuity.va.security.rest.resources.services;

import com.acuity.va.security.acl.domain.vasecurity.ClinicalStudyInfo;
import com.acuity.va.security.acl.domain.vasecurity.DrugProgrammeInfo;
import com.acuity.va.security.acl.domain.vasecurity.DatasetInfo;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DrugProgramme;

/**
 * Interface describing
 *
 * @author Glen
 */
public interface MyAnalyticsInfo {

    /**
     * Creates clinicalStudyInfo object and then uses the ClinicalStudyRepository rest of the information
     */
    ClinicalStudyInfo getClinicalStudyInfo(ClinicalStudy clinicalStudy);

    /**
     * Creates drugProgrammeInfo object and then uses the DrugProgrammeRepository rest of the information
     */
    DrugProgrammeInfo getDrugProgrammeInfo(DrugProgramme drugProgramme);

    /**
     * Creates datasetInfo object and then uses the DatasetRepository rest of the information
     */
    DatasetInfo getDatasetInfo(Dataset dataset);
}
