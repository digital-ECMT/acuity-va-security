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

package com.acuity.va.security.rest.security;

import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.rest.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.acuity.va.security.rest.resources.MyAnalyticsResource;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

@Ignore("Cant run with mocking")
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class SecurityTestingMyAnalyticsResourceITCase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestingMyAnalyticsResourceITCase.class);

     /**
     * Common notes:
     * - User1 has access to the DrugProgramme#2
     * - User2 has access to the ClinicalStudy#4
     * - User3 has access to the Dataset#10
     * - User "bob the builder" doesn't exist in our database
     * - Everyone has access to the training data for Dataset
     */
    
    @Autowired
    private MyAnalyticsResource myAnalyticsResource;

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_for_getDrugProgrammeInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        myAnalyticsResource.getDrugProgrammeInfo(DrugProgramme.class.getSimpleName(), 2L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getDrugProgrammeInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myAnalyticsResource.getDrugProgrammeInfo(DrugProgramme.class.getSimpleName(), 2L);
    }

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_permissionFor_getClinicalStudyInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User2"));

        myAnalyticsResource.getClinicalStudyInfo(ClinicalStudy.class.getSimpleName(), 4L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getClinicalStudyInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myAnalyticsResource.getClinicalStudyInfo(ClinicalStudy.class.getSimpleName(), 4L);
    }

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_permissionFor_getDatasetInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User3"));

        myAnalyticsResource.getDatasetInfo(AcuityDataset.class.getSimpleName(), 10L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getDatasetInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myAnalyticsResource.getDatasetInfo(AcuityDataset.class.getSimpleName(), 10L);
    }
    
    @Test
    public void shouldAllow_VIEW_VISUALATIONS_ToTrainingDataForEveryone_getDatasetInfo() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myAnalyticsResource.getDatasetInfo(AcuityDataset.class.getSimpleName(), 22L);
    }
}
