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

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Glen
 */
@Data
@AllArgsConstructor
public class UserAccessItem {

    // user who accessed the study in lockdown
    private User user;
    // access time
    private Date accessDateTime;
    // admin who granted him access
    private User administrator;
    // authorisors at the time of access
    private List<User> authorisors;
    // data owner at the time of access
    private User dataOwner;
}
