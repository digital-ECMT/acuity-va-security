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

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author glen
 */
public class CloningAcuityObjectIdentityTest {

    @Test
    public void shouldCloneDrugProgramme() throws CloneNotSupportedException {

        DrugProgramme drugProgramme = new DrugProgramme(10L);
        AcuityObjectIdentityWithPermission clonedDrugProgramme = (AcuityObjectIdentityWithPermission) drugProgramme.cloneAndReset();

        assertThat(clonedDrugProgramme.getId()).isEqualTo(drugProgramme.id);
        assertThat(clonedDrugProgramme).isInstanceOf(DrugProgramme.class);
    }

    @Test
    public void shouldCloneClinicalStudy() throws CloneNotSupportedException {

        ClinicalStudy clinicalStudy = new ClinicalStudy(10L);
        AcuityObjectIdentityWithPermission clonedClinicalStudy = (AcuityObjectIdentityWithPermission) clinicalStudy.cloneAndReset();

        assertThat(clonedClinicalStudy.getId()).isEqualTo(clinicalStudy.id);
        assertThat(clonedClinicalStudy).isInstanceOf(ClinicalStudy.class);
    }

    @Test
    public void shouldCloneDataset() throws CloneNotSupportedException {

        Dataset dataset = new AcuityDataset(10L);
        dataset.setDrugProgramme("Drug Programme A");
        AcuityObjectIdentityWithPermission clonedDataset = (AcuityObjectIdentityWithPermission) dataset.cloneAndReset();

        assertThat(clonedDataset.getId()).isEqualTo(dataset.id);
        assertThat(((Dataset) clonedDataset).getDrugProgramme()).isEqualTo(dataset.drugProgramme);
        assertThat(clonedDataset).isInstanceOf(AcuityDataset.class);
    }

    @Test
    public void shouldCloneDrugProgrammeWithName() throws CloneNotSupportedException {

        DrugProgramme drugProgramme = new DrugProgramme(10L, "name");
        AcuityObjectIdentityWithPermission clonedDrugProgramme = (AcuityObjectIdentityWithPermission) drugProgramme.cloneAndReset();

        assertThat(clonedDrugProgramme.getId()).isEqualTo(drugProgramme.getId());
        assertThat(clonedDrugProgramme.getName()).isEqualTo(drugProgramme.getName());
    }

    @Test
    public void shouldCloneDrugProgrammeAndNotChangeOriginalData() throws CloneNotSupportedException {

        DrugProgramme drugProgramme = new DrugProgramme(10L);
        AcuityObjectIdentityWithPermission clonedDrugProgramme = (AcuityObjectIdentityWithPermission) drugProgramme.cloneAndReset();

        clonedDrugProgramme.setRolePermissionMask(20);
        clonedDrugProgramme.setViewPermissionMask(20);

        assertThat(drugProgramme.getRolePermissionMask()).isNull();
        assertThat(drugProgramme.getViewPermissionMask()).isNull();
    }

    @Test
    public void shouldCloneDrugProgrammeAndNotChangeOriginalData2() throws CloneNotSupportedException {

        DrugProgramme drugProgramme = new DrugProgramme(10L, "name");
        AcuityObjectIdentityWithPermission clonedDrugProgramme = (AcuityObjectIdentityWithPermission) drugProgramme.cloneAndReset();

        drugProgramme.setId(20L);
        drugProgramme.setName("sdsd");

        assertThat(clonedDrugProgramme.getId()).isEqualTo(10);
        assertThat(clonedDrugProgramme.getName()).isEqualTo("name");
    }

    @Test
    public void shouldCloneDrugProgrammeAndRemoveHasPermission() throws CloneNotSupportedException {

        DrugProgramme drugProgramme = new DrugProgramme(10L, "name");
        drugProgramme.setRolePermissionMask(10);
        drugProgramme.setViewPermissionMask(10);

        assertThat(drugProgramme.hasRolePermission()).isEqualTo(true);

        AcuityObjectIdentityWithPermission clonedDrugProgramme = (AcuityObjectIdentityWithPermission) drugProgramme.cloneAndReset();

        assertThat(clonedDrugProgramme.hasRolePermission()).isEqualTo(false);
        assertThat(clonedDrugProgramme.getName()).isEqualTo("name");
        assertThat(clonedDrugProgramme.getId()).isEqualTo(10L);
    }
}
