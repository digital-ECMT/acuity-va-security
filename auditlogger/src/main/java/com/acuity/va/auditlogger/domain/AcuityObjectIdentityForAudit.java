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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 * @author Glen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AcuityObjectIdentityForAudit {

    /**
     * AcuityObjectIdentity classname
     */
    private String objectIdentityClassname;
    /**
     * AcuityObjectIdentity id
     */
    private Long objectIdentityId;
    private String name;

    private List<LockdownPeriod> lockdownPeriods = newArrayList();

    public AcuityObjectIdentityForAudit(String objectIdentityClassname, Long objectIdentityId, String name) {
        this.objectIdentityClassname = objectIdentityClassname;
        this.objectIdentityId = objectIdentityId;
        this.name = name;
    }

    public AcuityObjectIdentityForAudit(String objectIdentityClassname, Long objectIdentityId) {
        this.objectIdentityClassname = objectIdentityClassname;
        this.objectIdentityId = objectIdentityId;
    }
}
