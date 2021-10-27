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

package com.acuity.va.auditlogger.service;

import com.acuity.va.auditlogger.dao.AuditLoggerRepository;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Glen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/spring-auditlogger.xml",
        "classpath:/spring/mybatis/mybatis-auditlogger.xml",
        "classpath:spring/test/dataSource.xml"
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
@ActiveProfiles(profiles = {"h2-unit-tests"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class WhenLoggingWithAuditLoggerServiceWithDatasets {

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    @Autowired
    private TestSpringBean testSpringBean;

    @Test
    public void shouldLogSpringMethodWithDatasets() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("Glend", "Glen.d", Lists.newArrayList()));

        testSpringBean.datasetsTestRB(new DatasetsRequestTest(145L, 200L));

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(2);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").containsOnly("Glend");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityClassname").containsOnly("DetectDataset");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityId").contains(145L, 200L);

        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(4);
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("IS_MERGED", "MERGED_IDS");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly(1L, "145:200");
    }

    @Test
    public void shouldLogSpringMethodWith3Dataset() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("Glend", "Glen.d", Lists.newArrayList()));

        testSpringBean.datasetsTestRB(new DatasetsRequestTest(145L, 200L, 500L));

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(3);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(6);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("owner").containsOnly("Glend");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityClassname").containsOnly("DetectDataset");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityId").contains(145L, 200L, 500L);

        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(6);
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("IS_MERGED", "MERGED_IDS");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly(1L, "145:500:200");

        long distincttimes = auditLoggerRepository.getAllLogOperations().stream().map(logOp -> logOp.getDateCreated()).distinct().count();

        assertThat(distincttimes).isEqualTo(1);
    }
}
