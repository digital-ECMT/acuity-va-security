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

package com.acuity.va.security.acl.dao;

import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gets the name of the AcuityObjectIdentity
 *
 */
@Service
public class AcuityObjectService {

    @Autowired
    private AclRepository aclRepository;

    /**
     * Gets the name of the AcuityObjectIdentity from the id
     */
    public String getName(AcuityObjectIdentity acuityObjectIdentity) {

        return aclRepository.getAclName(acuityObjectIdentity.getClass().getName(), acuityObjectIdentity.getId());
    }

    /**
     * Gets the name of the AcuityObjectIdentity from the id
     *
     * @param objectIdentityClassname ie DrugProgramme
     * @param objectIdentityId primary key
     */
    public String getName(String objectIdentityClassname, Long objectIdentityId) {
        return getName(AcuityObjectIdentityImpl.create(objectIdentityClassname, objectIdentityId));
    }

    /**
     * Gets the list of clinical studies for the DrugProgramme
     *
     * @param drugProgramme ie DrugProgramme
     * @return list of studies
     */
    public List<ClinicalStudy> listClinicalStudiesForDrugProgramme(DrugProgramme drugProgramme) {
        List<AcuityObjectIdentityWithPermissionAndLockDown> listObjectIdentities = aclRepository.listObjectIdentities();

        return listObjectIdentities.stream().
                filter(roi -> roi.thisClinicalStudyType() && ((ClinicalStudy) roi).getDrugProgramme().equals(drugProgramme.getName())).
                map(roi -> ((ClinicalStudy) roi)).
                collect(toList());
    }

    /**
     * Gets the list of datasets for the study
     *
     * @param cs ie ClinicalStudy
     * @return list of Datasets
     */
    public List<Dataset> listDatasetsForClinicalStudy(ClinicalStudy cs) {
        List<AcuityObjectIdentityWithPermissionAndLockDown> listObjectIdentities = aclRepository.listObjectIdentities();

        return listObjectIdentities.stream().
                filter(roi -> roi.thisDatasetType() && ((Dataset) roi).getClinicalStudyCode().equals(cs.getCode())).
                map(roi -> ((Dataset) roi)).
                collect(toList());
    }
}
