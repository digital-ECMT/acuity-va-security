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

package com.acuity.va.security.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Glen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(locations = {
    // Test data source
    "classpath:spring/test/dataSource.xml",    
    // Config
    "classpath:spring/spring-cache.xml",
    "classpath:spring/spring-transactions.xml",
    // Audit Logger
    "classpath:/spring/spring-auditlogger.xml",
    "classpath:/spring/mybatis/mybatis-auditlogger.xml",    
    //Security
    "classpath:spring/mybatis/mybatis-security.xml",
    "classpath:spring/test/spring-acl.xml",
    "classpath:spring/spring-authentication-manager.xml",
    // Rest
    "classpath:spring/test/spring-executor.xml"})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionDbUnitTestExecutionListener.class
})
@ActiveProfiles("h2-unit-tests")
@Transactional
public @interface TransactionalMyBatisDBUnitH2Test {
}
