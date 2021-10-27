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
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Represents info from the MAP_ACUITY_INSTANCE acuity table.
 *
 * @author Glen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DatasetInfo extends CommonInfo {

    /**
     * Dataset name, ie ACUITY_Safety_STDY4321_Dummy_Instance
     */
    protected String name;
        
    /**
     * Drug programme name
     */
    protected String clinicalStudy;

    /**
     * The date the drug project/clinical study/dataset was added to ACUITY
     */
    protected Date addedDate;

    /**
     * The person that added the drug project/clinical study/dataset to ACUITY
     */
    protected String addedBy;
    ///////////////////////////////////////////////////////////////////////////////
    // variables manually added after the query has populated the above variables
    ///////////////////////////////////////////////////////////////////////////////

    private int usersAmount;
    /**
     * TODO javadoc??
     */
    protected List<String> subjectGroups;
    protected List<String> aeGroups;
    protected List<String> labGroups;
}
