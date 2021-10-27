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

import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.rest.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.acuity.va.security.rest.resources.AclResource;
import com.acuity.va.security.acl.domain.AcuityDataset;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.Lists;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class SecurityTestingAclResourceITCase {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestingAclResourceITCase.class);

    @Autowired
    private AclResource aclResource;

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_DrugProgramme() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User1"));

        aclResource.getAllPermissionForAcl(DrugProgramme.class.getSimpleName(), 2L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_DrugProgramme() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        aclResource.getAllPermissionForAcl(DrugProgramme.class.getSimpleName(), 2L);
    }

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_ClinicalStudy() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User2"));

        aclResource.getAllPermissionForAcl(ClinicalStudy.class.getSimpleName(), 4L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_ClinicalStudy() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        aclResource.getAllPermissionForAcl(ClinicalStudy.class.getSimpleName(), 2L);
    }

    @Test
    public void shouldOnlyAllow_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_Dataset() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User3"));

        aclResource.getAllPermissionForAcl(AcuityDataset.class.getSimpleName(), 10L);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_VIEW_VISUALATIONS_permissionFor_getAllPermissionForAcl_Dataset() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("bob the builder"));

        aclResource.getAllPermissionForAcl(AcuityDataset.class.getSimpleName(), 10L);
    }

    @Test
    public void shouldOnlyAllow_ACL_ADMINISTRATOR_roleFor_createAcl() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User3", Lists.newArrayList("ACL_ADMINISTRATOR")));
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        aclResource.createAcl(mockUriInfo, "Glen", new DrugProgramme(100L));
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyWithout_ACL_ADMINISTRATOR_roleFor_createAcl() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User3", Lists.newArrayList("NO_ACL_ADMINISTRATOR")));

        aclResource.createAcl(null, "Glen", new DrugProgramme(100L));
    }

}
