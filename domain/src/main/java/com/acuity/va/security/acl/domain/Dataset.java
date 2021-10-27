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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.ClassUtils;

/**
 * Simple Object representing an object entity identity in the spring security acl tables.
 * <p>
 * This represents the MAP_ACUITY_INSTANCE acuity table.
 *
 * @author Glen
 */
public abstract class Dataset extends AcuityObjectIdentityImpl {

    /**
     * Drug Programme of the Dataset.
     * <p>
     * For filtering on client.
     * <p>
     */
    protected String drugProgramme;
    /**
     * Clinical Study of the Dataset.
     * <p>
     * For filtering on client.
     * <p>
     */
    protected String clinicalStudyName;
    protected String clinicalStudyCode;

    public Dataset() {
        Class<?> typeClass = ClassUtils.getUserClass(this.getClass());
        type = typeClass.getName();
        Class<?> supertypeClass = ClassUtils.getUserClass(this.getClass().getSuperclass());
        supertype = supertypeClass.getName();
    }

    public Dataset(Long identifier) {
        this();
        this.id = identifier;
    }

    public Dataset(Long identifier, String name) {
        this(identifier);
        this.name = name;
    }

    public Dataset(Long identifier, String name, Integer mask) {
        this(identifier, name);
        this.permissionMask = mask;
    }

    public String getDrugProgramme() {
        return drugProgramme;
    }

    public String getClinicalStudyName() {
        return clinicalStudyName;
    }

    public String getClinicalStudyCode() {
        return clinicalStudyCode;
    }

    /*
     * Gets the class of the parent drug programme
     */
    @JsonIgnore
    public abstract Class getDrugProgrammeClass();

    @JsonIgnore
    public abstract Class getClinicalStudyClass();

    /*
     * Gets the class name of the parent drug programme
     */
    @JsonIgnore
    public abstract String getDrugProgrammeClassString();

    /*
     * Gets the class name of the parent clinical study
     */
    @JsonIgnore
    public abstract String getClinicalStudyClassString();

    public void setDrugProgramme(String drugProgramme) {
        this.drugProgramme = drugProgramme;
    }

    public void setClinicalStudyName(String clinicalStudyName) {
        this.clinicalStudyName = clinicalStudyName;
    }

    public void setClinicalStudyCode(String clinicalStudyCode) {
        this.clinicalStudyCode = clinicalStudyCode;
    }

    @Override
    public String toString() {
        return "Dataset{id=" + id + ", name=" + name + ", type=" + type + ", drugProgramme=" + drugProgramme
                + ", clinicalStudyCode=" + clinicalStudyCode + " clinicalStudyName=" + clinicalStudyName + "}";
    }
}
