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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;


public class SerializingActiveDirectoryRecordTest {
    
    private static ObjectMapper mapper;

    @BeforeClass
    public static void beforeClass() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Test
    public void testSerializeActiveDirectoryRecord() throws IOException {

        ActiveDirectoryRecord ad = ActiveDirectoryRecord.builder().prid("first_user_email.com#EXT#somethingelse")
                .givenName("name").surname("surname").displayName("name surname").mail("first_user@email.com").build();

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, ad);
        String json = writer.toString();

        final String prid = mapper.readTree(json).at("/cn").asText();
        assertThat(prid).isEqualTo("first_user@email.com");
    }
}
