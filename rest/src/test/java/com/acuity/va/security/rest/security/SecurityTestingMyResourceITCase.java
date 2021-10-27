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

package com.acuity.va.security.rest.security;

import com.acuity.va.security.rest.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.acuity.va.security.rest.resources.MyResource;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.permissions.AcuityPermissions;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class SecurityTestingMyResourceITCase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestingMyResourceITCase.class);

    @Autowired
    private MyResource myResource;

    @Test
    public void shouldAllowAnyOneToCall_getAclForCurrentUser() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("user001"));

        myResource.getAclForCurrentUser();

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myResource.getAclForCurrentUser();
    }

    @Test
    public void shouldAllowAnyOneToCall_hasPermissionForCurrentUserr() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("user001"));

        myResource.hasPermissionForCurrentUser(DrugProgramme.class.getSimpleName(), 2L, AcuityPermissions.EDIT_ADMINISTRATORS.getMask());

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

    }

    @Test
    public void shouldAllowAnyOneToCall_whoami() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("user001"));

        myResource.whoami();

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        myResource.whoami();
    }
}
