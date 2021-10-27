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

package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithInitialLockDown;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.vasecurity.DatasetInfo;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.junit.Ignore;


@Ignore
public class VASecurityResourceClientITCase {

    private static final Logger LOG = LoggerFactory.getLogger(VASecurityResourceClientITCase.class);

    private final VASecurityResourceClient vASecurityResourceClient = new VASecurityResourceClient("acuity", "", "");
    private final VASecurityResourceClient vASecurityResourceClientAcuity3 = new VASecurityResourceClient("acuity", "", "");

    @Test
    public void shouldloadRois() throws Exception {
        
        List<AcuityObjectIdentityWithInitialLockDown> loadRois = vASecurityResourceClient.loadRois();

        System.out.println(loadRois);
    }
    
    @Test
    public void shouldloadRoisForAcuity3() throws Exception {
        
        List<AcuityObjectIdentityWithInitialLockDown> loadRois = vASecurityResourceClientAcuity3.loadRois();

        System.out.println(loadRois);
    }
    
    @Test
    public void shouldGetDatasetInfo() throws Exception {
        
        DatasetInfo datasetInfo = vASecurityResourceClient.getDatasetInfo(new DetectDataset(401234535L));

        System.out.println(datasetInfo);
    }
}
