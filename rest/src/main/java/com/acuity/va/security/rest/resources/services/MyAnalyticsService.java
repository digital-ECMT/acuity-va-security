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

package com.acuity.va.security.rest.resources.services;

import com.acuity.va.security.acl.service.SecurityAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Service that just combines queries from Acl and Acuity db to populate Acl Info (DatasetInfo, ClinicalStudyInfo etc)
 *
 * @author Glen
 */
@Service
public class MyAnalyticsService {

    @Autowired
    private SecurityAclService securityAclService;
    @Autowired
    @Qualifier("myExecutor")
    private TaskExecutor myExecutor;

    @FunctionalInterface
    public interface WorkerInterface {

        String execute();
    }
}
