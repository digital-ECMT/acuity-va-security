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

package com.acuity.va.auditlogger.aspect;

import com.acuity.va.auditlogger.annotation.LogArg;
import com.acuity.va.auditlogger.annotation.LogOperation;
import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.acuity.va.auditlogger.service.AuditLoggerService;
import com.acuity.va.security.acl.domain.auditlogger.DatasetsLoggingObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static org.joor.Reflect.on;
import org.springframework.core.annotation.Order;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Logs the parameters of the method call depending on what is configured in the LogOperation LogArg annotations on the method
 *
 * @author Glen
 */
@Aspect
@Component
@Order(1)
public class LogOperationAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LogOperationAspect.class);
    @Autowired
    private AuditLoggerService auditLoggerService;
    // Constants
    public static final String ERROR_THROWN = "ERROR_THROWN";
    public static final String ACL_CLASSNAME = "ACL_CLASSNAME";
    public static final String ACL_ID = "ACL_ID";

    public LogOperationAspect() {
        LOG.debug("LogOperationAspect created");
    }

    /*
     * Cut across all methods that have com.acuity.va.auditlogger.annotations.LogOperation annotation
     */
    @Around("execution(@com.acuity.va.auditlogger.annotation.LogOperation * *(..))")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {

        Object returnResult = null; //actual result to return to user
        Object loggedReturnResult = null; // logged result to log

        boolean exceptionThrown = false;
        try {
            returnResult = pjp.proceed();

            loggedReturnResult = returnResult;

            return returnResult;
        } catch (Throwable ex) {
            LOG.error("Unable to log method", ex);
            exceptionThrown = true;
            loggedReturnResult = ex;
            throw ex;
        } finally {
            doLog(pjp, loggedReturnResult, exceptionThrown); // log the method call
        }
    }

    /**
     * Logs all the arguments of the methods that are configured in the method annotation
     */
    private void doLog(ProceedingJoinPoint pjp, Object returnResult, boolean exceptionThrown) {
        String methodPackageAndName = pjp.getSourceLocation().getWithinType().getCanonicalName() + "." + pjp.getSignature().getName();

        try {
            LogOperation logOperation = getAnnotation(pjp);

            if (!logOperation.logOnlyOnSuccess() || !exceptionThrown) {

                LogArg[] logAnnotationArgs = logOperation.value();
                Object[] methodArgs = pjp.getArgs();

                LOG.debug("Logging " + methodPackageAndName + "()");

                String prid = null;
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String sessionId = "NONE";
                if (authentication == null) {
                    prid = "Unknown, security not enabled";
                } else {
                    prid = authentication.getName();

                    try {
                        sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
                    } catch (IllegalStateException ex) {
                        LOG.info("Got IllegalStateException, unable to retrieve session, so SessionID : " + sessionId);
                    }
                    LOG.info("LOA SessionID : " + sessionId);
                }

                //create instance to persist to db
                LogOperationEntity logRecord = new LogOperationEntity(logOperation.name(), methodPackageAndName, prid, sessionId);

                DatasetsLoggingObject datasetsLoggingObject = null;

                for (LogArg logAnnotationArg : logAnnotationArgs) {
                    LogArgEntity logRecordArg = null;
                    Object result = null;

                    if (logAnnotationArg.isDatasetsLoggingObject()) {
                        datasetsLoggingObject = (DatasetsLoggingObject) getResultFromArg(logAnnotationArg, methodArgs, returnResult);
                        if (null != datasetsLoggingObject) {
                            LOG.info("Found datasetsLoggingObject : {}", datasetsLoggingObject);
                        } else {
                            LOG.info("No datasetsLoggingObject found for {}", logAnnotationArg);
                        }
                    } else {

                        // if static then us this first
                        if (StringUtils.isNotEmpty(logAnnotationArg.constIntegerValue())) {
                            result = new Integer(logAnnotationArg.constIntegerValue());
                        } else if (StringUtils.isNotEmpty(logAnnotationArg.constStringValue())) {
                            result = logAnnotationArg.constStringValue();
                        } else {
                            result = getResultFromArg(logAnnotationArg, methodArgs, returnResult);
                        }

                        if (logAnnotationArg.persistNull()) {
                            logRecordArg = new LogArgEntity(logAnnotationArg.name(), result);
                        } else if (result != null) {
                            logRecordArg = new LogArgEntity(logAnnotationArg.name(), result);
                        }

                        if (logRecordArg != null) {
                            logRecord.addLogArg(logRecordArg);
                            LOG.debug("Adding " + logRecordArg);
                        }
                        
                        // 2 ways of logging the datasets, the old way, and the new way using isDatasetsLoggingObject (see above)
                        // check for old
                        LogArgEntity aclClassname = logRecord.getLogArgs().stream().filter(arg -> arg.getName().equals(ACL_CLASSNAME)).findFirst().orElse(null);
                        LogArgEntity aclId = logRecord.getLogArgs().stream().filter(arg -> arg.getName().equals(ACL_ID)).findFirst().orElse(null);

                        // old way
                        if (aclClassname != null && aclId != null) {

                            logRecord.setObjectIdentityClassname(aclClassname.getStringValue());
                            logRecord.setObjectIdentityId(aclId.getLongValue());

                            logRecord.removeLogArg(aclClassname);
                            logRecord.removeLogArg(aclId);
                        }
                    }
                }

                if (null != datasetsLoggingObject) {

                    // set new info about merged dataset
                    LogArgEntity logRecordArg1 = new LogArgEntity("IS_MERGED", 1);
                    String ids = Joiner.on(":").join(datasetsLoggingObject.getIds());
                    LogArgEntity logRecordArg2 = new LogArgEntity("MERGED_IDS", ids);
                    logRecord.addLogArg(logRecordArg1);
                    logRecord.addLogArg(logRecordArg2);

                    for (Long id : datasetsLoggingObject.getIds()) {
                        logRecord.setObjectIdentityClassname(datasetsLoggingObject.getType());
                        logRecord.setObjectIdentityId(id);

                        log(logRecord);
                    }
                } else {
                    log(logRecord);
                }
            }
        } catch (Throwable ex) {
            LOG.error("Unable to log the method call " + methodPackageAndName + "() " + pjp, ex);
        }
    }

    private void log(LogOperationEntity logRecord) {
        LOG.debug("Persisting " + logRecord + "...");
        auditLoggerService.save(logRecord);
        LOG.debug("Persisted " + logRecord);
    }

    /**
     * Gets the result from executing LogArg..value() to the argument object using JOOR
     */
    private Object getResultFromArg(LogArg logArg, Object[] methodArgs, Object returnResult) {
        LOG.debug("Index " + logArg.arg() + " Expression: " + logArg.expression());
        String javaExpression = logArg.expression();

        Object argObject = null;

        switch (logArg.arg()) {
            case -1:  //-1 means we want the returned value as the object
                argObject = returnResult;
                break;
            case -2: // -2 not interested in arg, statis method call with arguments
                argObject = null;
                break;
            default:
                argObject = methodArgs[logArg.arg()];
                break;
        }
        /**
         * if throwable then its a error, if empty expression on annotation then just return the object
         */
        if (argObject instanceof Throwable || StringUtils.isEmpty(javaExpression)) {
            //then return the error
            return argObject;
        }

        // need to call the expression on the object now
        javaExpression = StringUtils.replace(javaExpression, "(", "");
        javaExpression = StringUtils.replace(javaExpression, ")", "");

        Iterable<String> expressions = Splitter.on(".").split(javaExpression);

        Reflect reflect = null;
        Object result = null;

        if (StringUtils.isNotEmpty(logArg.staticClass())) { // static invocation
            reflect = on(logArg.staticClass());

            String firstExpression = expressions.iterator().next();

            if (logArg.arg() != -2) { // use arg
                LOG.debug("Calling '" + firstExpression + "' with argument '" + argObject + "' on a " + reflect.type());
                reflect = reflect.call(firstExpression, argObject);

                result = reflect.get();
                LOG.debug("Result of '" + logArg.expression() + "' on '" + logArg.staticClass() + "' with argument '" + argObject + "' is: " + result);
            } else if (logArg.arg() == -2) { // dont use arg
                // if no method, then try a method without arg
                LOG.debug("Calling '" + firstExpression + "' without argument on a " + reflect.type());
                reflect = reflect.call(firstExpression);

                result = reflect.get();
                LOG.debug("Result of '" + logArg.expression() + "' on '" + logArg.staticClass() + "' without argument is: " + result);
            }
        } else {
            reflect = on(argObject);

            for (String expression : expressions) {
                try {
                    LOG.debug("Calling '" + expression + "' on a " + reflect.type());
                    reflect = reflect.call(expression);
                } catch (ReflectException nsme) {
                    // if no method, then try a field
                    LOG.debug("Exception, trying calling field '" + expression + "' on a " + reflect.type());
                    reflect = reflect.field(expression);
                }
            }

            result = reflect.get();
            LOG.debug("Result of '" + logArg.expression() + "' on '" + argObject + "' is: " + result);
        }

        return result;
    }

    /**
     * Gets the LogOperation annotation from the method signature
     */
    private LogOperation getAnnotation(ProceedingJoinPoint thisJoinPoint) throws NoSuchMethodException {
        final String methodName = thisJoinPoint.getSignature().getName();
        final MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        method = thisJoinPoint.getTarget().getClass().getDeclaredMethod(methodName, method.getParameterTypes());
        LogOperation logOperation = method.getAnnotation(LogOperation.class);

        if (logOperation == null) {
            throw new IllegalStateException("Method must be have Annotation LogOperation, " + method);
        } else {
            return logOperation;
        }
    }
}
