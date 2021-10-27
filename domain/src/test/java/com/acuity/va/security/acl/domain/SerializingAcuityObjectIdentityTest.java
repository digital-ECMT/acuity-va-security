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
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import static com.google.common.collect.Lists.newArrayList;
import java.io.StringWriter;
import org.junit.*;
import static com.google.common.collect.Lists.newArrayList;


/**
 *
 * @author glen
 */
public class SerializingAcuityObjectIdentityTest {
    
    private static ObjectMapper mapper;
    
    @BeforeClass
    public static void beforeClass() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Test
    public void shouldSerialiseDetectDrugProgramme() throws IOException {
        DetectDataset detectDrugProgramme = new DetectDataset(1L, "sd");
                
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, detectDrugProgramme);
        
        String json = writer.toString();
        
        System.out.println(json);
    }
}
