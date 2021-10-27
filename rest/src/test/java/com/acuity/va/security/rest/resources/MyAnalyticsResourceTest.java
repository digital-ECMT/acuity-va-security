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

package com.acuity.va.security.rest.resources;

import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.vasecurity.ClinicalStudyInfo;
import com.acuity.va.security.acl.domain.vasecurity.DatasetDataUpload;
import com.acuity.va.security.acl.domain.vasecurity.DatasetInfo;
import com.acuity.va.security.acl.domain.vasecurity.DatasetSetup;
import com.acuity.va.security.acl.service.VASecurityResourceClient;
import com.google.common.collect.Sets;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Glen
 */
public class MyAnalyticsResourceTest extends AbstractSpringContextJerseyTest {

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(MyAnalyticsResource.class);
    }

    @Test
    public void shouldNotReturnDrugProgrammeInfoWithStatus404_ForInvalidId() {

        VASecurityResourceClient mockVASecurityResourceClient = mock(VASecurityResourceClient.class);
        when(mockAclRepository.getAclName(any(String.class), any(Long.class))).thenReturn("name1");
        when(mockVASecurityResourceClient.getDrugProgrammeInfo(any(DrugProgramme.class))).thenReturn(null);
        when(mockVASecurityResourceFactory.get()).thenReturn(mockVASecurityResourceClient);

        Response response = target("/myanalytics/DrugProgramme/DrugProgramme/1").request().get(Response.class);

        verifyNoMoreInteractions(mockSecurity);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * Clinical Study
     */
    @Test
    public void shouldReturnClinicalStudyInfoWithStatus200_ForCorrectId() {

        Long ID = 10L;
        Date now = new Date();
        final String studyCode = "StudyCodeA";

        ClinicalStudyInfo clinicalStudyInfo = new ClinicalStudyInfo();
        clinicalStudyInfo.setId(ID);
        clinicalStudyInfo.setDrugProgramme("DrugA");
        clinicalStudyInfo.setCode(studyCode);
        DatasetDataUpload clinicalStudyDataUpload = new DatasetDataUpload();
        clinicalStudyDataUpload.setLastDate(now);
        clinicalStudyDataUpload.setLastStatus("AMBER");
        DatasetSetup clinicalStudySetup = new DatasetSetup();
        clinicalStudySetup.setCompleted(false);
        clinicalStudySetup.setEnabled(true);
        clinicalStudySetup.setFileLocations(newArrayList());

        VASecurityResourceClient mockVASecurityResourceClient = mock(VASecurityResourceClient.class);
        when(mockVASecurityResourceClient.getClinicalStudyInfo(any(ClinicalStudy.class))).thenReturn(clinicalStudyInfo);
        when(mockAclRepository.getAclName(any(String.class), any(Long.class))).thenReturn("name1");
        when(mockVASecurityResourceFactory.get()).thenReturn(mockVASecurityResourceClient);

        Response response = target("/myanalytics/ClinicalStudy/ClinicalStudy/1").request().get(Response.class);

        verify(mockVASecurityResourceFactory, times(1)).get();
        verifyNoMoreInteractions(mockSecurity, mockVASecurityResourceFactory, mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);

        ClinicalStudyInfo clinicalStudyInfoOut = response.readEntity(ClinicalStudyInfo.class);
        assertThat(clinicalStudyInfoOut.getDrugProgramme()).isEqualTo("DrugA");
        assertThat(clinicalStudyInfoOut.getCode()).isEqualTo(studyCode);

    }

    @Test
    public void shouldNotReturnClinicalStudyInfoWithStatus404_ForInvalidId() {

        VASecurityResourceClient mockVASecurityResourceClient = mock(VASecurityResourceClient.class);
        when(mockAclRepository.getAclName(any(String.class), any(Long.class))).thenReturn("name1");
        when(mockVASecurityResourceClient.getClinicalStudyInfo(any(ClinicalStudy.class))).thenReturn(null);
        when(mockVASecurityResourceFactory.get()).thenReturn(mockVASecurityResourceClient);

        Response response = target("/myanalytics/ClinicalStudy/ClinicalStudy/1").request().get(Response.class);

        verifyNoMoreInteractions(mockSecurity);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * Dataset
     */
    @Test
    public void shouldReturnDatasetWithStatus200_ForCorrectId() {

        Long ID = 10L;
        Date now = new Date();
        String owner = "user001";

        DatasetInfo datasetInfo = new DatasetInfo();
        datasetInfo.setId(ID);
        datasetInfo.setName("ACUITY_Safety_STDY");
        datasetInfo.setAddedDate(now);
        datasetInfo.setAddedBy(owner);
        datasetInfo.setDrugProgramme("DrugA");

        int usersAmount = 4;

        List<String> parentStudies = newArrayList("ClinicalStudy1", "ClinicalStudy2");
        List<String> AEGroups = newArrayList("AEGroup1");
        List<String> subjectGroups = newArrayList("SubjectGroup1");
        List<String> labGroups = newArrayList("LabGroup1");
        List<String> drugCompounds = newArrayList("STDY4321", "STDY5321");

        datasetInfo.setAeGroups(AEGroups);
        datasetInfo.setSubjectGroups(subjectGroups);
        datasetInfo.setLabGroups(labGroups);

        VASecurityResourceClient mockVASecurityResourceClient = mock(VASecurityResourceClient.class);
        when(mockVASecurityResourceClient.getDatasetInfo(any(AcuityDataset.class))).thenReturn(datasetInfo);
        when(mockAclRepository.getAclName(any(String.class), any(Long.class))).thenReturn("name1");
        when(mockVASecurityResourceFactory.get()).thenReturn(mockVASecurityResourceClient);
        when(mockSecurityAclService.getGrantedUsersAmountForDataset(any(AcuityDataset.class))).thenReturn(usersAmount);

        Response response = target("/myanalytics/Dataset/AcuityDataset/1").request().get(Response.class);

        verify(mockVASecurityResourceFactory, times(1)).get();
        verify(mockSecurityAclService, times(1)).getGrantedUsersAmountForDataset(any(AcuityDataset.class));
        verifyNoMoreInteractions(mockSecurity, mockVASecurityResourceFactory, mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);

        DatasetInfo datasetInfoOut = response.readEntity(DatasetInfo.class);
        assertThat(datasetInfoOut.getName()).isEqualTo("ACUITY_Safety_STDY");
        assertThat(datasetInfoOut.getDrugProgramme()).isEqualTo("DrugA");
        assertThat(datasetInfoOut.getAddedDate()).isEqualTo(now);
        assertThat(datasetInfoOut.getAddedBy()).isEqualTo(owner);
        assertThat(datasetInfoOut.getUsersAmount()).isEqualTo(4);
        assertThat(datasetInfoOut.getAeGroups()).isEqualTo(AEGroups);
        assertThat(datasetInfoOut.getLabGroups()).isEqualTo(labGroups);
        assertThat(datasetInfoOut.getSubjectGroups()).isEqualTo(subjectGroups);
    }

    @Test
    public void shouldNotReturnDatasetInfoWithStatus404_ForInvalidId() {

        VASecurityResourceClient mockVASecurityResourceClient = mock(VASecurityResourceClient.class);
        when(mockVASecurityResourceClient.getDatasetInfo(any(DetectDataset.class))).thenReturn(null);
        when(mockAclRepository.getAclName(any(String.class), any(Long.class))).thenReturn("name1");
        when(mockVASecurityResourceFactory.get()).thenReturn(mockVASecurityResourceClient);

        Response response = target("/myanalytics/Dataset/DetectDataset/1").request().get(Response.class);

        verifyNoMoreInteractions(mockSecurity);
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
