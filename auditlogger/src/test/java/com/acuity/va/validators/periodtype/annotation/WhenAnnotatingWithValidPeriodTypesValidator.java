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

package com.acuity.va.validators.periodtype.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * @author andrew
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/spring-validators.xml"
})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class WhenAnnotatingWithValidPeriodTypesValidator {

    @Autowired
    private AnnotatedTestSpringBean testSpringBean;

    @Test
    public void shouldAllowNamedMethodCall() throws Exception {
        String result = testSpringBean.sayHello("foo");
        assertThat(result).isEqualTo("Hello foo");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldPreventNonNamedMethod() throws Exception {
        testSpringBean.sayHello("killers");
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public void shouldThrowWobblerOnWrongIndex() throws Exception {
        testSpringBean.sayHello2("fighters");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldPreventAllWithNoArgs() throws Exception {
        testSpringBean.sayHello3("foo");
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void shouldThrowIllegalStateOnNegativeIndex() throws Exception {
        testSpringBean.sayHello4("fighters");
    }

    @Test
    public void shouldAllowNamedMethodCallPeriodType() throws Exception {
        String result = testSpringBean.sayHello5(PeriodType.weekly1);
        assertThat(result).isEqualTo("Hello weekly1");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldPreventNonNamedMethodPeriodType() throws Exception {
        testSpringBean.sayHello5(PeriodType.weekly3);
    }

}
