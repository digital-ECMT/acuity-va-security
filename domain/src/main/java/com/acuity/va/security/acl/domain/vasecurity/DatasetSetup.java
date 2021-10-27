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

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * @author Glen
 */
@Data
public class DatasetSetup implements Serializable {

    /**
     * has the data setup in the admin ui been completed
     */
    private Boolean completed;
    /**
     * is this study enabled for acuity analysis
     */
    private Boolean enabled;
    private List<String> fileLocations;

    /**
     * Gets the file directory locations from the fileLocations.
     * <p/>
     * Trims down to the parent directory. The format wasn't correct so getParent wasn't working so had to manually remove last / or \.
     */
    public Set<String> getTrimmedFileLocations() {
        return fileLocations.stream().map(file -> {
                    // hack at the moment
                    int index = file.lastIndexOf("\\") != -1 ? file.lastIndexOf("\\") : file.lastIndexOf("/");
                    if (index != -1) {
                        return file.substring(0, index);
                    } else {
                        return file;
                    }
                }
        ).collect(Collectors.toSet());
    }

    //for json
    public void setTrimmedFileLocations(Set<String> ignore) {
    }
}
