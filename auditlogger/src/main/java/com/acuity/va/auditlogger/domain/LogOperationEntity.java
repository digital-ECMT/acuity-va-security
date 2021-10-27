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
import org.apache.commons.lang3.time.DateUtils;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Log Operation, this has many arguments, and the operation is defined by a name, it also logs the package and method from where it came from and the owner of
 * the method call
 *
 * @author Glen
 */
public class LogOperationEntity implements Serializable {

    /**
     * primary key
     */
    private Long logOperationId;
    /**
     * args
     */
    private List<LogArgEntity> logArgs = new ArrayList<LogArgEntity>();
    /**
     * Name of the operation
     */
    private String name;
    /**
     * Package and name of the method
     */
    private String packageAndMethodName;
    /**
     * network owner
     */
    private String owner;
    /**
     * Date/Time record was created
     */
    private Date dateCreated;

    /**
     * Active session id for the user session
     */
    private String sessionId;
    /**
     * AcuityObjectIdentity classname
     */
    private String objectIdentityClassname;
    /**
     * AcuityObjectIdentity id
     */
    private Long objectIdentityId;

    protected LogOperationEntity() {
        super();
    }

    public LogOperationEntity(String name, String packageAndMethodName, String user, String sessionId) {
        this("N/A", -1L, name, packageAndMethodName, user, sessionId);
    }

    public LogOperationEntity(String objectIdentityClassname, Long objectIdentityId, String name, String packageAndMethodName, String user, String sessionId) {
        Validate.isTrue(name != null, "name is null");
        Validate.isTrue(packageAndMethodName != null, "packageAndMethodName is null");
        Validate.isTrue(user != null, "user is null");
        Validate.isTrue(objectIdentityClassname != null, "objectIdentityClassname is null");
        Validate.isTrue(objectIdentityId != null, "objectIdentityId is null");

        this.objectIdentityClassname = objectIdentityClassname;
        this.objectIdentityId = objectIdentityId;
        this.name = name;
        this.packageAndMethodName = packageAndMethodName;
        this.owner = user;
        this.sessionId = sessionId;
        setDateCreated(new Date());
    }

    @XmlTransient
    public Long getId() {
        return logOperationId;
    }

    public String getOwner() {
        return owner;
    }

    private void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    private void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPackageAndMethodName() {
        return packageAndMethodName;
    }

    private void setPackageAndMethodName(String packageAndMethodName) {
        this.packageAndMethodName = packageAndMethodName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateCreatedWithoutTime() {
        return dateCreated == null ? null : DateUtils.truncate(dateCreated, Calendar.DATE);
    }

    public void setDateCreatedWithoutTime() {
        // nop
    }

    private void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void addLogArg(LogArgEntity logArg) {
        this.logArgs.add(logArg);
    }

    public void setLogArgs(List<LogArgEntity> logArgs) {
        this.logArgs = logArgs;
    }

    public void removeLogArg(LogArgEntity logArg) {
        this.logArgs.remove(logArg);
    }

    public List<LogArgEntity> getLogArgs() {
        return logArgs;
    }

    public String getObjectIdentityClassname() {
        return objectIdentityClassname;
    }

    public void setObjectIdentityClassname(String objectIdentityClassname) {
        this.objectIdentityClassname = objectIdentityClassname;
    }

    public Long getObjectIdentityId() {
        return objectIdentityId;
    }

    public void setObjectIdentityId(Long objectIdentityId) {
        this.objectIdentityId = objectIdentityId;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.logOperationId).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LogOperationEntity)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        LogOperationEntity that = (LogOperationEntity) o;
        // New instances have null for id as it is generated by a sequence in the database.
        // However EqualsBuilder returns true when comparing two nulls.
        // Below logic ensures that new instances are always not equal
        if (logOperationId == null && that.logOperationId == null) {
            return false;
        }
        return new EqualsBuilder().append(logOperationId, that.logOperationId).isEquals();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("id", getId());

        builder.append("objectIdentityClassname", objectIdentityClassname);
        builder.append("objectIdentityId", objectIdentityId);
        builder.append("name", name);
        builder.append("packageAndMethodName", packageAndMethodName);
        builder.append("owner", owner);
        builder.append("sessionId", sessionId);
        builder.append("dateCreated", dateCreated);

        return builder.toString();
    }
}
