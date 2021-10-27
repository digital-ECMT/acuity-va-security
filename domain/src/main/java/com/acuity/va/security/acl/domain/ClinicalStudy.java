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

/**
 * Simple Object representing an object entity identity in the spring security acl tables.
 *
 * This represents the MAP_STUDY_RULE acuity table.
 *
 * @author Glen
 */
public class ClinicalStudy extends AbstractClinicalStudy {

    public ClinicalStudy() {
        super();
    }

    public ClinicalStudy(Long id, String code, String name) {
        super(id, code, name);
    }
    
    public ClinicalStudy(String code, String name) {
        super(code, name);
    }
    
    public ClinicalStudy(Long id, String name) {
        super(id, name, name);
    }

    public ClinicalStudy(Long id) {
        super(id);
    }

    public ClinicalStudy(String name) {
        super(name, name);
    }

    public ClinicalStudy(String name, Integer mask) {
        super(name, name, mask);
    }

    @Override
    public String toString() {
        return "ClinicalStudy{id=" + id + ", code=" + code + ", name=" + name + ", type=" + type + ", drugProgramme=" + drugProgramme + "}";
    }

    @Override
    public Class getDrugProgrammeClass() {
        return DrugProgramme.class;
    }

    @Override
    public String getDrugProgrammeClassString() {
        return DrugProgramme.class.getName();
    }
}
