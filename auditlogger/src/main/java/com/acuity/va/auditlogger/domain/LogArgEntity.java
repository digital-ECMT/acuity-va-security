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

package com.acuity.va.auditlogger.domain;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * Arguments for the LogOperation, defined by a name and a object value
 *
 * @author Glen
 */
public class LogArgEntity {

    /**
     * primary key
     */
    private Long logArgId;
    /**
     * log operation
     */
    private Long logOperationId;
    /**
     * Name of the record
     */
    private String name;
    /**
     * Is error
     */
    private Boolean error = false;
    /**
     * String value of the args
     */
    private String stringValue;
    /**
     * Float value of the args
     */
    private Float floatValue;
    /**
     * Long value of the args
     */
    private Long longValue;
    /**
     * Date value of the args
     */
    private Date dateValue;

    protected LogArgEntity() {
    }

    public LogArgEntity(Long logOperationId, String name) {
        this(logOperationId, name, null);
    }

    public LogArgEntity(String name, Object value) {
        this(null, name, value);
    }

    public LogArgEntity(Long logOperationId, String name, Object value) {
        Validate.isTrue(name != null, "name is null");

        this.name = name;

        //for logging a error been thrown when trying to log a return type
        if (value instanceof Throwable) {
            Throwable throwable = ((Throwable) value);
            this.error = true;
            init(logOperationId, name, throwable.getClass().getName() + ": " + throwable.getMessage());
        } else {
            init(logOperationId, name, value);
        }
    }

    private void init(Long logOperationId, String name, Object value) {
        this.logOperationId = logOperationId;

        if (value != null) {
            if (value instanceof Float || value instanceof Double) {
                this.floatValue = new Float(value.toString());
            } else if (value instanceof String) {
                this.stringValue = (String) value;
            } else if (value instanceof Long || value instanceof Integer) {
                this.longValue = new Long(value.toString());
            } else if (value instanceof Date) {
                this.dateValue = (Date) value;
            } else {
                this.stringValue = value.toString();
            }
        }
    }

    public Long getId() {
        return logArgId;
    }

    public void setLogOperationId(Long logOperationId) {
        this.logOperationId = logOperationId;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getStringValue() {
        return stringValue;
    }

    private void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    private void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    private void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    private void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Object getValue() {
        if (stringValue == null && longValue == null && floatValue == null && dateValue == null) {
            return null;
        } else if (stringValue != null) {
            return stringValue;
        } else if (longValue != null) {
            return longValue;
        } else if (dateValue != null) {
            return dateValue;
        } else {
            return floatValue;
        }
    }

    public Boolean isError() {
        return error;
    }

    private void setError(Boolean error) {
        this.error = error;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.logArgId).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LogArgEntity)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        LogArgEntity that = (LogArgEntity) o;
        // New instances have null for id as it is generated by a sequence in the database.
        // However EqualsBuilder returns true when comparing two nulls.
        // Below logic ensures that new instances are always not equal
        if (logArgId == null && that.logArgId == null) {
            return false;
        }
        return new EqualsBuilder().append(logArgId, that.logArgId).isEquals();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("id", getId());

        builder.append("name", name);
        builder.append("value", getValue());
        builder.append("stringValue", stringValue);
        builder.append("floatValue", floatValue);
        builder.append("longValue", longValue);
        builder.append("dateValue", dateValue);
        builder.append("isError", error);
        return builder.toString();
    }
}
