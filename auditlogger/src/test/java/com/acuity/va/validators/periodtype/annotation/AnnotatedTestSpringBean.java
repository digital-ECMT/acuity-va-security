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

import org.springframework.stereotype.Component;

/**
 * Examples of how to use this annotation
 *
 * @author andrew
 */
@Component
public class AnnotatedTestSpringBean {

    @ValidPeriodTypes(args=0, validTypes={"foo","fighters"})
    public String sayHello(String name) {
        return "Hello " + name;
    }
    
    @ValidPeriodTypes(args=1, validTypes={"foo","fighters"})
    public String sayHello2(String name) {
        return "Hello " + name;
    }
    
    @ValidPeriodTypes(args=0, validTypes={})
    public String sayHello3(String name) {
        return "Hello " + name;
    }
    
    @ValidPeriodTypes(args=-1, validTypes={"foo","fighters"})
    public String sayHello4(String name) {
        return "Hello " + name;
    }
    
    @ValidPeriodTypes(args=0, validTypes={"weekly1", "weekly2"})
    public String sayHello5(PeriodType name) {
        return "Hello " + name;
    }
}
