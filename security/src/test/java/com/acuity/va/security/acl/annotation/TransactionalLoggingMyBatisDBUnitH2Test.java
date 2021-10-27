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

package com.acuity.va.security.acl.annotation;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Glen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(locations = {
    "classpath:spring/spring-transactions.xml",
    "classpath:spring/test/dataSource.xml",
    "classpath:spring/mybatis/mybatis-security.xml",
    "classpath:spring/test/spring-acl.xml",
    "classpath:spring/spring-authentication-manager.xml",
    "classpath:spring/spring-cache.xml",
    "classpath:spring/spring-auditlogger.xml",
    "classpath:spring/mybatis/mybatis-auditlogger.xml",
    "classpath:spring/spring-scheduler.xml"
})
@ActiveProfiles(profiles = {"h2-unit-tests"})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionDbUnitTestExecutionListener.class
})
@Transactional
public @interface TransactionalLoggingMyBatisDBUnitH2Test {
}
