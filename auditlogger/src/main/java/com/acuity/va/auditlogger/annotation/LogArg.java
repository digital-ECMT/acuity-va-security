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

package com.acuity.va.auditlogger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Glen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogArg {

    /**
     * @return  arg index in pointcut, -1 for the return value, -2 no position
     */
    int arg() default -2;    
    /**
     * Defines if this is the datasets log object to persist multiple time for each dataset
     * @return 
     */
    boolean isDatasetsLoggingObject() default false;
   
    /**
     * @return  name of the value
     */
    String name();
    /**
     * expression toString() on an object would call object.toString();
     * expression toString().getClass() on an object would call object.toString().getClass();
     * leave empty/"" to return the actual value
     *
     * @return string representation of method calls
     */
    String expression() default "";
    /**
     * If set to false, and the value is null, then its not inserted into the database
     */
    boolean persistNull() default true;
    /**
     * If this is set then the constant value is used instead of running the expression from value()
     */
    String constStringValue() default "";
    /**
     * If this is set then the constant value is used instead of running the expression from value()
     */
    String constIntegerValue() default "";    
    /**
     * The class to call the expression on if its not the actual arg
     */
    String staticClass() default "";       
}
