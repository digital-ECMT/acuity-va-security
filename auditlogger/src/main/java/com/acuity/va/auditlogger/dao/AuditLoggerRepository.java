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
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for Audit Log queries and updates
 *
 * @author glen
 */
@Repository
@Transactional(value = "audit")
public interface AuditLoggerRepository {

    @Insert({"INSERT INTO log_operation ("
            + "LOG_OPERATION_ID, OBJECT_IDENTITY_CLASSNAME, OBJECT_IDENTITY_ID, DATECREATED, LOG_NAME, OWNER, PACKAGE_AND_METHOD_NAME, SESSION_ID)"
            + " VALUES (#{logOperationId}, #{objectIdentityClassname}, #{objectIdentityId},"
            + " #{dateCreated}, #{name}, #{owner}, #{packageAndMethodName}, #{sessionId})"})
    @SelectKey(
            statement = {" SELECT nextval('Log_Operation_Seq') "},
            keyProperty = "logOperationId",
            before = true,
            resultType = Long.class
    )
    void insertLogOperation(LogOperationEntity logOperationEntity);

    @Insert({"INSERT INTO log_arg  VALUES (#{logArgId}, #{name,jdbcType=VARCHAR}, #{stringValue,jdbcType=VARCHAR}, #{dateValue,jdbcType=TIMESTAMP },"
            + " #{longValue,jdbcType=BIGINT}, #{floatValue,jdbcType=FLOAT},"
            + " (CASE WHEN #{error} THEN 1 ELSE 0 END), #{logOperationId})"})
    @SelectKey(
            statement = {" SELECT nextval('Log_Arg_Seq') "},
            keyProperty = "logArgId",
            before = true,
            resultType = Long.class
    )
    void insertLogArg(LogArgEntity logArgEntity);

    @Select("SELECT count(*) "
        + "  FROM log_operation")
    @Options(useCache = false, flushCache = true)
    int countAllLogOperations();

    @Select("SELECT count(*) "
        + "  FROM log_arg "
        + "  WHERE log_operation_id = #{logOperationId}")
    @Options(useCache = false, flushCache = true)
    int countLogArgs(long logOperationId);

    @Select("SELECT count(*) "
        + "  FROM log_arg")
    @Options(useCache = false, flushCache = true)
    int countAllLogArgs();

    @Select("SELECT * "
        + "  FROM log_operation")
    @Results(value = {
        @Result(property = "logOperationId", column = "LOG_OPERATION_ID"),
        @Result(property = "objectIdentityClassname", column = "OBJECT_IDENTITY_CLASSNAME"),
        @Result(property = "objectIdentityId", column = "OBJECT_IDENTITY_ID"),
        @Result(property = "name", column = "LOG_NAME"),
        @Result(property = "packageAndMethodName", column = "PACKAGE_AND_METHOD_NAME"),
        @Result(property = "sessionId", column = "SESSION_ID")})
    @Options(useCache = false, flushCache = true)
    List<LogOperationEntity> getAllLogOperations();

    @Select("SELECT * "
        + "  FROM log_operation"
        + " WHERE log_operation_id = #{logOperationId}")
    @Results(value = {
        @Result(property = "logOperationId", column = "LOG_OPERATION_ID"),
        @Result(property = "objectIdentityClassname", column = "OBJECT_IDENTITY_CLASSNAME"),
        @Result(property = "objectIdentityId", column = "OBJECT_IDENTITY_ID"),
        @Result(property = "name", column = "LOG_NAME"),
        @Result(property = "packageAndMethodName", column = "PACKAGE_AND_METHOD_NAME"),
        @Result(property = "sessionId", column = "SESSION_ID")})
    @Options(useCache = false, flushCache = true)
    LogOperationEntity getLogOperations(long logOperationId);

    @Select("SELECT * "
        + "  FROM log_arg")
    @Results(value = {
        @Result(property = "logArgId", column = "LOG_ARG_ID"),
        @Result(property = "name", column = "LOG_ARG_NAME"),
        @Result(property = "stringValue", column = "LOG_ARG_STRING_VALUE"),
        @Result(property = "dateValue", column = "LOG_ARG_DATE_VALUE"),
        @Result(property = "longValue", column = "LOG_ARG_LONG_VALUE"),
        @Result(property = "floatValue", column = "LOG_ARG_FLOAT_VALUE")
    })
    @Options(useCache = false, flushCache = true)
    List<LogArgEntity> getAllLogArgs();

    @Select("SELECT * "
        + "  FROM log_arg "
        + "  WHERE log_operation_id = #{logOperationId}")
    @Results(value = {
        @Result(property = "logArgId", column = "LOG_ARG_ID"),
        @Result(property = "name", column = "LOG_ARG_NAME"),
        @Result(property = "stringValue", column = "LOG_ARG_STRING_VALUE"),
        @Result(property = "dateValue", column = "LOG_ARG_DATE_VALUE"),
        @Result(property = "longValue", column = "LOG_ARG_LONG_VALUE"),
        @Result(property = "floatValue", column = "LOG_ARG_FLOAT_VALUE")
    })
    @Options(useCache = false, flushCache = true)
    List<LogArgEntity> getLogArgs(long logOperationId);
        
    //@Options(useCache = false, flushCache = true)
    List<LogOperationEntity> getLockdowns();
}
