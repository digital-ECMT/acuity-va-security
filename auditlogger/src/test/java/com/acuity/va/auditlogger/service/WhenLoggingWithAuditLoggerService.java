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
import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

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
public class WhenLoggingWithAuditLoggerService {

    private static final Logger LOG = LoggerFactory.getLogger(WhenLoggingWithAuditLoggerService.class);

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    @Autowired
    private AuditLoggerService auditLoggerService;

    @Autowired
    private TestSpringBean testSpringBean;

    @Test
    public void shouldLogSimpleSpringMethod() {

        LogOperationEntity logOperationEntity = new LogOperationEntity("sf", "sd", "se", "si");
        logOperationEntity.addLogArg(new LogArgEntity("dd", new Date()));
        logOperationEntity.addLogArg(new LogArgEntity("a", 1L));

        auditLoggerService.save(logOperationEntity);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countLogArgs(logOperationEntity.getId())).isEqualTo(2);

        LogOperationEntity logOperation = auditLoggerRepository.getAllLogOperations().get(0);

        assertThat(logOperation.getName()).isEqualTo("sf");
        assertThat(logOperation.getPackageAndMethodName()).isEqualTo("sd");
        assertThat(logOperation.getOwner()).isEqualTo("se");
        assertThat(logOperation.getSessionId()).isEqualTo("si");

        LogArgEntity logArgEntityOut = auditLoggerRepository.getLogArgs(logOperationEntity.getId()).get(0);

        assertThat(logArgEntityOut.getName()).isEqualTo("dd");
    }

    @Test
    public void shouldLogSpringMethodWithExpression() {

        testSpringBean.sayHello("glen");

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(2);

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value");
    }

    @Test
    public void shouldLogSpringMethodWithOwner() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2("glen", 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value1", "Input-Value2");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerIgnoringNulls() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2IgnoreNull(null, 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(2);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value2");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerIgnoringNulls2() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2IgnoreNull("not null", 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value1", "Input-Value2");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("Hello not null", "not null", "10");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerNotIgnoringNulls() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2NotIgnoreNull(null, 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value1", "Input-Value2");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("Hello null", null, "10");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerNotIgnoringNulls2() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2NotIgnoreNull("not null", 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("Hello not null", "not null", "10");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Return-Value", "Input-Value1", "Input-Value2");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerConstantString() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2ConstantString("not null", 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("not null", "10", "GLEN");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1", "Input-Value2", "Input-Constant");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerConstantInteger() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHello2ConstantInteger("not null", 10L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(3);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("not null", "10", 1L);
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1", "Input-Value2", "Input-Constant");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerStaticMethodCall() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHelloWithStaticMethodWithArg("not null", true);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(2);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("not null", 1L);
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1", "Input-StaticMethod");
    }

    @Test
    public void shouldLogSpringMethodWithOwnerStaticMethodCallWithoutArg() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHelloWithStaticMethodWithOutArg("not null", false);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(2);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("not null", "name without arg");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1", "Input-StaticMethod");
    }

    @Test
    public void shouldLogSpringMethodThatThrowsExceptions() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        try {
            testSpringBean.sayHelloException("not null");
        } catch (Exception ex) {
        }

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(1);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("not null");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1");
    }

    @Test
    public void shouldLogSpringMethodThatThrowsExceptionsReturnValue() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        try {
            testSpringBean.sayHelloLogExceptionReturn("not null");
        } catch (Exception ex) {
        }

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(1);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("java.lang.Exception: Exception in method");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-ReturnValue1");

    }

    @Test
    public void shouldntLogSpringMethodThatThrowsExceptions() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        try {
            testSpringBean.sayHelloDontLogException("not null");
        } catch (Exception ex) {
        }

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(0);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(0);
    }

    @Test
    public void shouldntLogSpringMethodDoLog() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHelloDoLog("glen");

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(1);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityClassname()).isEqualTo("N/A");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityId()).isEqualTo(-1L);

        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").containsOnly("glen");
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").containsOnly("Input-Value1");
    }

    @Test
    public void shouldLogSpringMethodWithROI() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.sayHelloLogRoi("DrugProgramme", 12L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(0);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityClassname()).isEqualTo("DrugProgramme");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityId()).isEqualTo(12L);
    }
    
    @Test
    public void shouldLogSpringMethodWithUnsetLockdownROI() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("glend", "glen.d", Lists.newArrayList()));

        testSpringBean.unsetLockdown("DrugProgramme", 12L);

        assertThat(auditLoggerRepository.countAllLogOperations()).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs()).isEqualTo(0);
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getOwner()).isEqualTo("glend");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityClassname()).isEqualTo("DrugProgramme");
        assertThat(auditLoggerRepository.getAllLogOperations().get(0).getObjectIdentityId()).isEqualTo(12L);
    }
}
