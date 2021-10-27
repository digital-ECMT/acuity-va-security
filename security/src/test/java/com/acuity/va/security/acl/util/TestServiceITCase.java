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

package com.acuity.va.security.acl.util;

import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-all-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class TestServiceITCase {

    private static Logger LOG = LoggerFactory.getLogger(TestServiceITCase.class);

    @Autowired
    private TestService testService;

    @Test
    public void shouldListAllUsersNotInTrainedUserGroup() {
        testService.sayHelloSelf();
    }

}
