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

import com.acuity.va.security.acl.domain.auditlogger.DatasetsLoggingObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Glen
 */
public class DatasetsRequestTest  {
    private final Set<Long> _ids;
    private final String type = "DetectDataset";
    
    DatasetsRequestTest(Long... ids) {
        _ids = new HashSet(Arrays.asList(ids));
    }
    
    public DatasetsLoggingObject getDatasetsLoggingObject() {
        return new DatasetsLoggingObject() {
            @Override
            public Set<Long> getIds() {
                return _ids;
            }

            @Override
            public String getType() {
                return type;
            }            
        };
    }
}
