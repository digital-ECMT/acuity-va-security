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

package com.acuity.va.validators.periodtype.aspect;

import com.acuity.va.validators.periodtype.annotation.ValidPeriodTypes;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


/**
 * Validates periodType.toString() for a given argument falls within required range of acceptable values
 * as detailed in the validTypes annotation argument.
 *
 * @author andrew
 */
@Aspect
@Component
public class ValidPeriodTypesAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ValidPeriodTypesAspect.class);
    
    /**
     * Cut across all methods that have com.acuity.va.validators.periodtype.annotation.ValidPeriodTypes annotation
     */
    @Before("execution(@com.acuity.va.validators.periodtype.annotation.ValidPeriodTypes * *(..))")
    public void validate(JoinPoint jp) {
       
        doValidate(jp);
        
    }
    /**
     * Perform the validation check
     * @param jp the method joinpoint
     */
    private void doValidate(JoinPoint jp) {

        try {

            String methodPackageAndName = jp.getSourceLocation().getWithinType().getCanonicalName() + "." + jp.getSignature().getName();

            ValidPeriodTypes periodTypes = getAnnotation(jp);
            String[] validTypes = periodTypes.validTypes();
            int argIndex = periodTypes.args();
            Object[] methodArgs = jp.getArgs();

            checkAnnotationArgs(argIndex, methodArgs.length); // confirm annotation args are sensible

            List<String> validList = Arrays.asList(validTypes);

            String targetArg = methodArgs[argIndex].toString();

            // the actual validation check
            if (!validList.contains(targetArg)) {
                LOG.error("Invalid period type, received [" + targetArg + "] expected " + validList.toString());
                throw new IllegalArgumentException("Invalid period type, received [" + targetArg + "] expected " + validList.toString());
            }
        } catch (NoSuchMethodException ex) {
            LOG.error("Internal error in aspect, no such method : " + ex.getMessage());
        }
    }

    /**
     * Gets the ValidPeriodTypes annotation from the method signature
     */
    private ValidPeriodTypes getAnnotation(JoinPoint thisJoinPoint) throws NoSuchMethodException {
        final String methodName = thisJoinPoint.getSignature().getName();
        final MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        method = thisJoinPoint.getTarget().getClass().getDeclaredMethod(methodName, method.getParameterTypes());
        ValidPeriodTypes validPeriodTypes =  method.getAnnotation(ValidPeriodTypes.class);

        if (validPeriodTypes == null) {
            LOG.error("Method must be have Annotation ValidPeriodTypes, " + method);
            throw new IllegalStateException("Method must be have Annotation ValidPeriodTypes, " + method);
        } else {
            return validPeriodTypes;
        }
    }

    /**
     * Carry out checks on the annotation arguments
     * @param index
     * @param methodArgCount 
     */
    private void checkAnnotationArgs(int index, int methodArgCount) {
    
        if (index >= methodArgCount) {
            LOG.error("args index value (in annotation) exceeds number of parameters in the method");
            throw new IndexOutOfBoundsException("args index value (in annotation) exceeds number of parameters in the method");
        }
            
        if (index < 0) {
            LOG.error("args index (in annotation) is negative");
            throw new IndexOutOfBoundsException("args index (in annotation) is negative");
        }
    }    
}

