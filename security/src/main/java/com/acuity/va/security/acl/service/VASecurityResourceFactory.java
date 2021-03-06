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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Factory to allow testing of code
 *
 * @author Glen
 */
@Service
public class VASecurityResourceFactory {

    @Value("${acuity.vahub.url}")
    private String url;
    @Value("${acuity.vahub.username}")
    private String username;
    @Value("${acuity.vahub.password}")
    private String password;

    public List<VASecurityResourceClient> getAll() {
        return Collections.singletonList(get());
    }
    
    public List<VASecurityResourceClient> getDetect() {
        return getAll();
    }

    public VASecurityResourceClient get() {
        return new VASecurityResourceClient(username, password, url);
    }
}
