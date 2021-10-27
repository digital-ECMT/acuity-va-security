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

package com.acuity.va.security.rest.resources;

import com.google.common.collect.Sets;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingResourceTest extends AbstractSpringContextJerseyTest {

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(LoggingResource.class);
    }

    @Test
    public void shouldCallLoggingDataset() throws Exception {

        Response response = target("logging/dataset_log/webApp/false/moduleType/moduleType/analysisName/analysisName").
             queryParam("datasetId", "238").queryParam("tab", "aes").request().post(Entity.json(""), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldCallLoggingDatasetWithoutTab() throws Exception {

        Response response = target("logging/dataset_log/webApp/false/moduleType/moduleType/analysisName/analysisName").
           queryParam("datasetId", "238")
            .request().post(Entity.json(""), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    public void shouldCallLoggingDataset2() throws Exception {

        Response response = target("logging/dataset_log/webApp/true/moduleType/safety/analysisName/Aes").
            queryParam("datasetId", "238").queryParam("tab", "aes").request().post(Entity.json(""), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    public void shouldCallLoggingStudy() throws Exception {

        Response response = target("logging/study_log/webApp/false/moduleType/moduleType/analysisName/analysisName").
             queryParam("studyId", "238").request().post(Entity.json(""), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    public void shouldCallLoggingDrugProgramme() throws Exception {

        Response response = target("logging/drugprogramme_log/webApp/true/moduleType/moduleType1/analysisName/analysisName1").
            queryParam("drugprogrammeId", "238").request().post(Entity.json(""), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
