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

package com.acuity.va.auditlogger.dao;

import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

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
@ActiveProfiles(profiles = {"h2-unit-tests"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhenRunningAuditLoggerRespositoryQueries {

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    @Test
    public void shouldInsertLogOperation() {

        LogOperationEntity logOperationEntity = new LogOperationEntity("sf", "sd", "se", "D6EE5025DB0FEDCAA51171BA1822F9D9");

        auditLoggerRepository.insertLogOperation(logOperationEntity);

        assertThat(logOperationEntity.getId()).isNotNull();
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);

        LogOperationEntity logOperation = auditLoggerRepository.getAllLogOperations().get(0);

        assertThat(logOperation.getName()).isEqualTo("sf");
        assertThat(logOperation.getObjectIdentityClassname()).isEqualTo("N/A");
        assertThat(logOperation.getObjectIdentityId()).isEqualTo(-1);
        assertThat(logOperation.getPackageAndMethodName()).isEqualTo("sd");
        assertThat(logOperation.getOwner()).isEqualTo("se");
        assertThat(logOperation.getSessionId()).isEqualTo("D6EE5025DB0FEDCAA51171BA1822F9D9");
    }

    @Test
    public void shouldInsertLogOperationWithRoi() throws Exception {

        LogOperationEntity logOperationEntity = new LogOperationEntity("DrugProgramme", 13L, "sf", "sd", "se", "D6EE5025DB0FEDCAA51171BA1822F9D9");

        auditLoggerRepository.insertLogOperation(logOperationEntity);

        assertThat(logOperationEntity.getId()).isNotNull();
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);

        LogOperationEntity logOperation = auditLoggerRepository.getAllLogOperations().get(0);

        assertThat(logOperation.getName()).isEqualTo("sf");
        assertThat(logOperation.getObjectIdentityClassname()).isEqualTo("DrugProgramme");
        assertThat(logOperation.getObjectIdentityId()).isEqualTo(13);
        assertThat(logOperation.getPackageAndMethodName()).isEqualTo("sd");
        assertThat(logOperation.getOwner()).isEqualTo("se");
        assertThat(logOperation.getSessionId()).isEqualTo("D6EE5025DB0FEDCAA51171BA1822F9D9");
    }

    @Test
    public void shouldInsertLogArgs() {

        // given
        LogOperationEntity logOperationEntity = new LogOperationEntity("sf", "sd", "sd", "si");
        auditLoggerRepository.insertLogOperation(logOperationEntity);

        LogArgEntity logArgEntity = new LogArgEntity(logOperationEntity.getId(), "sb", 100);

        // when
        auditLoggerRepository.insertLogArg(logArgEntity);

        // then
        assertThat(logArgEntity.getId()).isNotNull();
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countLogArgs(logOperationEntity.getId())).isEqualTo(1);

        LogArgEntity logArgEntityOut = auditLoggerRepository.getLogArgs(logOperationEntity.getId()).get(0);

        assertThat(logArgEntityOut.getName()).isEqualTo("sb");
        assertThat(logArgEntityOut.getLongValue()).isEqualTo(100L);
        assertThat(logArgEntityOut.getValue()).isEqualTo(100L);
        assertThat(logArgEntityOut.getFloatValue()).isEqualTo(null);
    }

    @Test
    public void shouldInsertDateLogArgs() {

        // given
        LogOperationEntity logOperationEntity = new LogOperationEntity("sf", "sd", "sd", "si");
        auditLoggerRepository.insertLogOperation(logOperationEntity);

        final Date date = new Date();
        LogArgEntity logArgEntity = new LogArgEntity(logOperationEntity.getId(), "sb", date);

        // when
        auditLoggerRepository.insertLogArg(logArgEntity);

        // then
        assertThat(logArgEntity.getId()).isNotNull();
        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countLogArgs(logOperationEntity.getId())).isEqualTo(1);

        LogArgEntity logArgEntityOut = auditLoggerRepository.getLogArgs(logOperationEntity.getId()).get(0);

        assertThat(logArgEntityOut.getName()).isEqualTo("sb");
        assertThat(logArgEntityOut.getDateValue()).isEqualTo(date);
    }
}
