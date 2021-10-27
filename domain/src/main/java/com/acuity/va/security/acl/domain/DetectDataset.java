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

import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.DETECT;

/**
 * Simple Object representing an object entity identity in the spring security acl tables.
 *
 * This represents the MAP_ACUITY_INSTANCE acuity table.
 *
 * @author Glen
 */
public class DetectDataset extends Dataset {

    public DetectDataset() {
        super();
        this.origin = DETECT;
    }

    public DetectDataset(Long identifier) {
        super(identifier);
        this.origin = DETECT;
    }

    public DetectDataset(Long identifier, String name) {
        super(identifier, name);        
        this.origin = DETECT;
    }

    public DetectDataset(Long identifier, String name, Integer mask) {
        super(identifier, name, mask);        
        this.origin = DETECT;
    }

    @Override
    public String toString() {
        return "DetectDataset{id=" + id + ", name=" + name + ", type=" + type + ", drugProgramme=" + drugProgramme
                + ", clinicalStudyCode=" + clinicalStudyCode + ", clinicalStudyName=" + clinicalStudyName + "}";
    }

    @Override
    public Class getDrugProgrammeClass() {
        return DrugProgramme.class;
    }

    @Override
    public Class getClinicalStudyClass() {
        return ClinicalStudy.class;
    }

    @Override
    public String getDrugProgrammeClassString() {
        return DrugProgramme.class.getName();
    }

    @Override
    public String getClinicalStudyClassString() {
        return ClinicalStudy.class.getName();
    }
}
