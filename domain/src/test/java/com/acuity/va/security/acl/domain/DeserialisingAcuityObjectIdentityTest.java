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
import static com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl.Origin.NONE;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;
import java.util.Arrays;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author glen
 */
public class DeserialisingAcuityObjectIdentityTest {

    private static ObjectMapper mapper;

    @BeforeClass
    public static void beforeClass() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void shouldCreateDrugProgramme() throws IOException {
        String json = "{"
            + "\"typeForJackson\":\"com.acuity.va.security.acl.domain.ClinicalStudy\","
            + "\"type\":\"com.acuity.va.security.acl.domain.ClinicalStudy\", "
            + "\"id\":224,"
            + "\"name\":\"ClinicalStudy BA\","
            + "\"parent\":{\"typeForJackson\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"type\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"id\":30,\"name\":\"Drug A\"}}";

        AcuityObjectIdentityWithParent readValue = (AcuityObjectIdentityWithParent) mapper.readValue(json, AcuityObjectIdentity.class);

        assertThat(readValue).isInstanceOf(ClinicalStudy.class);
        assertThat(readValue.getName()).isEqualTo("ClinicalStudy BA");
        assertThat(readValue.getId()).isEqualTo(224);
        assertThat(readValue.getOrigin()).isEqualTo(NONE);
        assertThat(readValue.getParent()).isInstanceOf(DrugProgramme.class);
        assertThat(readValue.getSupertype()).isEqualTo(ClinicalStudy.class.getName());
        assertThat(readValue.getParent().getId()).isEqualTo(30);
        assertThat(readValue.getParent().getName()).isEqualTo("Drug A");
    }
    
    @Test
    public void shouldCreateDrugProgrammeWithOrigin() throws IOException {
        String json = "{"
            + "\"typeForJackson\":\"com.acuity.va.security.acl.domain.ClinicalStudy\","
            + "\"type\":\"com.acuity.va.security.acl.domain.ClinicalStudy\", "
            + "\"id\":224,"
            + "\"origin\":\"DETECT\","
            + "\"name\":\"ClinicalStudy BA\","
            + "\"parent\":{\"typeForJackson\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"type\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"id\":30,\"name\":\"Drug A\"}}";

        AcuityObjectIdentityWithParent readValue = (AcuityObjectIdentityWithParent) mapper.readValue(json, AcuityObjectIdentity.class);

        assertThat(readValue).isInstanceOf(ClinicalStudy.class);
        assertThat(readValue.getName()).isEqualTo("ClinicalStudy BA");
        assertThat(readValue.getId()).isEqualTo(224);
        assertThat(readValue.getOrigin()).isEqualTo(DETECT);
        assertThat(readValue.getParent()).isInstanceOf(DrugProgramme.class);
        assertThat(readValue.getSupertype()).isEqualTo(ClinicalStudy.class.getName());
        assertThat(readValue.getParent().getId()).isEqualTo(30);
        assertThat(readValue.getParent().getName()).isEqualTo("Drug A");
    }
    
    @Test
    public void shouldCreateDrugProgramme3() throws IOException {
        String json = "{"
            + "\"typeForJackson\":\"com.acuity.va.security.acl.domain.DetectDataset\","
            + "\"type\":\"com.acuity.va.security.acl.domain.DetectDataset\", "
            + "\"id\":224,"
            + "\"name\":\"ClinicalStudy BA\""
            + "}";

        AcuityObjectIdentityWithParent readValue = (AcuityObjectIdentityWithParent) mapper.readValue(json, AcuityObjectIdentity.class);

        assertThat(readValue).isInstanceOf(DetectDataset.class);
        assertThat(readValue.getName()).isEqualTo("ClinicalStudy BA");
        assertThat(readValue.getId()).isEqualTo(224);
        assertThat(readValue.getSupertype()).isEqualTo(Dataset.class.getName());
    }

    @Test
    public void shouldCreateDetectDataset() throws IOException {
        String json = " {"
            + "  \"typeForJackson\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"type\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"id\" : 401232,"
            + "  \"name\" : \"CazAviPooled\""
            + "} ";

        AcuityObjectIdentityWithParent readValue = (AcuityObjectIdentityWithParent) mapper.readValue(json, AcuityObjectIdentity.class);

        assertThat(readValue).isInstanceOf(DetectDataset.class);
        assertThat(readValue.getName()).isEqualTo("CazAviPooled");
        assertThat(readValue.getId()).isEqualTo(401232L);
        assertThat(readValue.getSupertype()).isEqualTo(Dataset.class.getName());
    }
    
    @Test
    public void shouldCreateDetectDatasetWithLockdown() throws IOException {
        String json = " {"
            + "  \"typeForJackson\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"type\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"id\" : 401232,"
            + "  \"lockdown\" : true,"
            + "  \"name\" : \"CazAviPooled\""
            + "} ";

        AcuityObjectIdentityWithPermissionAndLockDown
                readValue = mapper.readValue(json, AcuityObjectIdentityWithPermissionAndLockDown.class);

        assertThat(readValue).isInstanceOf(DetectDataset.class);
        assertThat(readValue.getName()).isEqualTo("CazAviPooled");
        assertThat(readValue.getId()).isEqualTo(401232L);
        assertThat(readValue.isLockdown()).isTrue();
        assertThat(readValue.getSupertype()).isEqualTo(Dataset.class.getName());
    }
    
    @Test
    public void shouldCreateDetectDatasetWithLockdownAndInitialUsers() throws IOException {
        String json = " {"
            + "  \"typeForJackson\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"type\" : \"com.acuity.va.security.acl.domain.DetectDataset\","
            + "  \"id\" : 401232,"
            + "  \"lockdown\" : true,"
            + "  \"bobby\" : true,"
            + "  \"name\" : \"CazAviPooled\""
            + "} ";

        AcuityObjectIdentityWithInitialLockDown
                readValue = mapper.readValue(json, AcuityObjectIdentityWithInitialLockDown.class);

        assertThat(readValue).isInstanceOf(DetectDataset.class);
        assertThat(readValue.getName()).isEqualTo("CazAviPooled");
        assertThat(readValue.getId()).isEqualTo(401232L);
        assertThat(readValue.isLockdown()).isTrue();
        assertThat(readValue.getSupertype()).isEqualTo(Dataset.class.getName());
    }

    @Test
    public void shouldCreateDrugProgramme2() throws IOException {
        String json = "{\"typeForJackson\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"type\":\"com.acuity.va.security.acl.domain.DrugProgramme\","
            + "\"id\":10,\"name\":\"Drug A\",\"parent\":{\"typeForJackson\":\"com.acuity.va.security.acl.domain.ClinicalStudy\",\"type\":\"com.acuity.va.security.acl.domain.ClinicalStudy\""
            + ",\"id\":101,\"name\":\"ClinicalStudy BA\",\"drugProgrammeClass\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"autoGeneratedId\":false},\"autoGeneratedId\":false}";

        AcuityObjectIdentityWithParent readValue = (AcuityObjectIdentityWithParent) mapper.readValue(json, AcuityObjectIdentity.class);

        assertThat(readValue).isInstanceOf(DrugProgramme.class);
        assertThat(readValue.getName()).isEqualTo("Drug A");
        assertThat(readValue.getId()).isEqualTo(10);
        assertThat(readValue.getParent()).isInstanceOf(ClinicalStudy.class);
        assertThat(readValue.getSupertype()).isEqualTo(DrugProgramme.class.getName());
        assertThat(readValue.getParent().getId()).isEqualTo(101);
        assertThat(readValue.getParent().getName()).isEqualTo("ClinicalStudy BA");
    }

    @Test
    public void shouldSerialisaeAcuityDrugProgramme() throws IOException {
        DrugProgramme acuityDrugProgramme = new DrugProgramme(1L, "sd");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, acuityDrugProgramme);

        String json = writer.toString();

        System.out.println(json);
    }

    @Test
    public void shouldSerialiseDetectDrugProgramme() throws IOException {
        DrugProgramme detectDrugProgramme = new DrugProgramme(1L, "sd");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, detectDrugProgramme);

        String json = writer.toString();

        System.out.println(json);
    }
    
    @Test
    public void shouldCreateDrugProgramme21() throws IOException {
        String json = "[{\"typeForJackson\":\"com.acuity.va.security.acl.domain.DrugProgramme\","
            + "\"type\":\"com.acuity.va.security.acl.domain.DrugProgramme\","
            + "\"id\":10,\"name\":\"Drug A\","
            + ""
            + "\"parent\":{\"typeForJackson\":\"com.acuity.va.security.acl.domain.ClinicalStudy\",\"type\":\"com.acuity.va.security.acl.domain.ClinicalStudy\""
            + ",\"id\":101,\"name\":\"ClinicalStudy BA\",\"drugProgrammeClass\":\"com.acuity.va.security.acl.domain.DrugProgramme\",\"autoGeneratedId\":false},\"autoGeneratedId\":false}]";

        System.out.println(Arrays.asList(mapper.readValue(json, AcuityObjectIdentity[].class)));
    }
}
