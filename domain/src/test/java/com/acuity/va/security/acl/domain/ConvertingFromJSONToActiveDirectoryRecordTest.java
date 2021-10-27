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

package com.acuity.va.security.acl.domain;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ConvertingFromJSONToActiveDirectoryRecordTest {

    @Test
    public void shouldCreateCorrectADRFromJSON() throws IOException {

        File file = ResourceUtils.getFile(this.getClass().getResource("/activeDirectoryRecord.json"));

        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

        final JsonNode arrNode = objectMapper.readTree(file).get("value");
        ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ActiveDirectoryRecord>>() {
        });
        List<ActiveDirectoryRecord> adr = reader.readValue(arrNode);

        assertThat(adr).hasSize(2);

        assertThat(adr.get(0)).extracting(ActiveDirectoryRecord::getDisplayName,
                ActiveDirectoryRecord::getGivenName, ActiveDirectoryRecord::getSurname,
                ActiveDirectoryRecord::getMail, ActiveDirectoryRecord::getPrid)
                .containsExactly("User 1 display name", "User1 name", "User1 surname", null, "user_1@email.com");
        assertThat(adr.get(1)).extracting(ActiveDirectoryRecord::getDisplayName,
                ActiveDirectoryRecord::getGivenName, ActiveDirectoryRecord::getSurname,
                ActiveDirectoryRecord::getMail, ActiveDirectoryRecord::getPrid)
                .containsExactly("User 2 display name", "User2 name", "User2 surname", "user_2@email.com", "user_2@email.com");
    }
}
