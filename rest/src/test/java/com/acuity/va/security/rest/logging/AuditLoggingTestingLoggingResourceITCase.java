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

package com.acuity.va.security.rest.logging;

import com.acuity.va.auditlogger.dao.AuditLoggerRepository;
import com.acuity.va.security.config.annotation.FlatXmlNullColumnSensingDataSetLoader;
import com.acuity.va.security.rest.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.rest.resources.LoggingResource;
import com.acuity.va.security.rest.security.TestingAuthenication;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullColumnSensingDataSetLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuditLoggingTestingLoggingResourceITCase {

    private static Logger LOG = LoggerFactory.getLogger(AuditLoggingTestingLoggingResourceITCase.class);

    @Autowired
    private LoggingResource loggingResource;

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    @Test
    public void shouldLog_viewChangeForDrugProgramme() {

        // Given
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        // When
        loggingResource.viewChangeForDrugProgramme(true, "moduleType", "analysisName", 2L);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(4);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("name").contains("VIEWCHANGE");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").contains("User1");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").
            containsOnly("ISWEBAPP", "MODULETYPE", "ANALYSISNAME", "DRUGPROGRAMME_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").
            containsOnly("true", "moduleType", "analysisName", 2L);       
    }
    
    @Test
    public void shouldLog_viewChangeForStudy() {

        // Given
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        // When
        loggingResource.viewChangeForStudy(true, "moduleType", "analysisName", 2L);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(4);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("name").contains("VIEWCHANGE");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").contains("User1");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").
            containsOnly("ISWEBAPP", "MODULETYPE", "ANALYSISNAME", "STUDY_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").
            containsOnly("true", "moduleType", "analysisName", 2L);       
    }
    
    @Test
    public void shouldLog_viewChangeForDataset() {

        // Given
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        // When
        loggingResource.viewChangeForDataset(true, "moduleType", "analysisName", "tab", 2L);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(5);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("name").contains("VIEWCHANGE");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").contains("User1");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").
            containsOnly("ISWEBAPP", "MODULETYPE", "ANALYSISNAME", "TAB", "DATASET_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").
            containsOnly("true", "moduleType", "analysisName", "tab", 2L);       
    }
    
    @Test
    public void shouldLog_viewChangeForDatasetnoTab() {

        // Given
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        // When
        loggingResource.viewChangeForDataset(true, "moduleType", "analysisName", null, 2L);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(4);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("name").contains("VIEWCHANGE");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").contains("User1");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").
            containsOnly("ISWEBAPP", "MODULETYPE", "ANALYSISNAME", "DATASET_ID");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").
            containsOnly("true", "moduleType", "analysisName", 2L);       
    }
}
