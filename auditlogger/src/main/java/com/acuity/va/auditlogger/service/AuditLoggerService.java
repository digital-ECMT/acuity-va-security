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

package com.acuity.va.auditlogger.service;

import com.acuity.va.auditlogger.dao.AuditLoggerRepository;
import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "audit", propagation = Propagation.REQUIRES_NEW)
public class AuditLoggerService {

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    public void save(LogOperationEntity logOperationEntity) {

        auditLoggerRepository.insertLogOperation(logOperationEntity);

        for (LogArgEntity logArg : logOperationEntity.getLogArgs()) {
            logArg.setLogOperationId(logOperationEntity.getId());
            auditLoggerRepository.insertLogArg(logArg);
        }
    }
}
