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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This a db table that provides the 'cached' information for the security to the acl_object_identity table
 *
 * @author Glen
 */
@Data
@NoArgsConstructor
//@AllArgsConstructor
@ToString
public final class AclObject {

    private Long id;
    // can be updated ds and cs
    private String name;
    // cant be updated for cs
    private String code;
    /**
     * The primary key for aclObjectIdentity table
     */
    private long aclObjectIdentityPkId;
    // can be updated
    private String moduleType;
    // name of parent drug programme (only for datasets and studies)
    private String parentDrugProgramme;
    // study name of parent clinical study (only for datasets)
    private String parentClinicalStudyCode;
    // study code of parent clinical study (only for datasets)
    private String parentClinicalStudyName;

    // is this in lockdown
    private boolean lockdown = false;
    // inherited permissions of this object
    private boolean inherited = false;
    
    // type of acl.  DP, CS or DS
    private String aclType;

    private AclObject(Long id, String code, String name, long aclObjectIdentityPkId, String parentDrugProgramme, 
            String parentClinicalStudyCode, String parentClinicalStudyName,
            boolean lockdown, boolean inherited, String aclType) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.aclObjectIdentityPkId = aclObjectIdentityPkId;
        this.parentDrugProgramme = parentDrugProgramme;
        this.parentClinicalStudyCode = parentClinicalStudyCode;
        this.parentClinicalStudyName = parentClinicalStudyName;
        this.lockdown = lockdown;
        this.inherited = inherited;
        this.aclType = aclType;
    }

    /**
     * Drug Programme
     */
    private AclObject(Long id, String name, long aclObjectIdentityPkId) {
        this(id, null, name, aclObjectIdentityPkId, null, null, null, false, false, "DP");
    }
    
    /**
     * Clinical Study or Dataset
     */
    private AclObject(Long id, String code, String name, long aclObjectIdentityPkId, String parentDrugProgramme, 
            String parentClinicalStudyCode, String parentClinicalStudyName, String aclType) {
        this(id, code, name, aclObjectIdentityPkId, parentDrugProgramme, parentClinicalStudyCode, parentClinicalStudyName, false, false, aclType);
    }     

    public static AclObject createDP(Long id, String name, long aclObjectIdentityPkId) {
        return new AclObject(id, name, aclObjectIdentityPkId);
    }      

    public static AclObject createCS(Long id, String code, String name, long aclObjectIdentityPkId, String parentDrugProgramme) {
        return new AclObject(id, code, name, aclObjectIdentityPkId, parentDrugProgramme, null, null, "CS");
    }
    
    public static AclObject createDataset(Long id, String name, long aclObjectIdentityPkId, String parentDrugProgramme,
            String parentClinicalStudyCode, String parentClinicalStudyName) {
        return new AclObject(id, null, name, aclObjectIdentityPkId, parentDrugProgramme, parentClinicalStudyCode, parentClinicalStudyName, "DS");
    }
}
