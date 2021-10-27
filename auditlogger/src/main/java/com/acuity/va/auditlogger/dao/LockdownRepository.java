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

import com.acuity.va.auditlogger.domain.DataOwner;
import com.acuity.va.auditlogger.domain.Group;
import com.acuity.va.auditlogger.domain.LockdownPeriod;
import com.acuity.va.auditlogger.domain.AcuityObjectIdentityForAudit;
import com.acuity.va.auditlogger.domain.RoiGrantedAccess;
import com.acuity.va.auditlogger.domain.User;
import java.util.Date;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Repository for Lockdown Audit Log queries 
 *
 * @author glen
 */
@Repository
@Transactional(value = "security", readOnly = true)
public interface LockdownRepository {
   
    List<LockdownPeriod> getLockdowns(@Param("roi") AcuityObjectIdentityForAudit roi);
    
    DataOwner getDataowner(@Param("roi") AcuityObjectIdentityForAudit roi, @Param("date") Date date);
    
    boolean hasBeenRemovedAsDataOwner(
            @Param("roi") AcuityObjectIdentityForAudit roi,
            @Param("dateAddedAsDataowner") Date dateAddedAsDataowner, 
            @Param("dateGrantedUserPermission") Date dateGrantedUserPermission, 
            @Param("dataownerToCheck") String dataownerToCheck);
    
    List<String> getAuthorisors(@Param("roi") AcuityObjectIdentityForAudit roi, @Param("date") Date date);
    
    List<Group> getGroups(@Param("date") Date date, @Param("user") String user, @Param("group") String group);
    
    List<RoiGrantedAccess> getWhosGotAccess(@Param("roi") AcuityObjectIdentityForAudit roi, @Param("date") Date date);
    List<RoiGrantedAccess> getWhosGotAccess(@Param("roi") AcuityObjectIdentityForAudit roi, @Param("date") Date date, @Param("user") String user);
    
    User getWhoRemovedLockdown(@Param("roi") AcuityObjectIdentityForAudit roi, @Param("date") Date date);
}
