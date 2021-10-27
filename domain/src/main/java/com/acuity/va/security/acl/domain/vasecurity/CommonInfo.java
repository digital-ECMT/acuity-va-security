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

package com.acuity.va.security.acl.domain.vasecurity;

import lombok.Data;

import java.io.Serializable;

/**
 * Represents common info from the MAP_PROJECT_RULE, MAP_STUDY_RULE and MAP_ACUITY_INSTANCE acuity tables.
 *
 * @author Glen
 */
@Data
public class CommonInfo implements Serializable {

    /**
     * The primary key of the drug project/clinical study/dataset
     */
    protected long id;

    /**
     * Drug programme name
     */
    protected String drugProgramme;
}
