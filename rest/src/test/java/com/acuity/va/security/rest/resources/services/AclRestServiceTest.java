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

package com.acuity.va.security.rest.resources.services;

import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.rest.security.Security;
import com.acuity.va.security.acl.service.SecurityAclService;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.springframework.security.acls.model.AlreadyExistsException;

/**
 *
 * @author Glen
 */
@RunWith(MockitoJUnitRunner.class)
public class AclRestServiceTest {

    @InjectMocks
    AclRestService aclRestService = new AclRestService();

    @Mock
    SecurityAclService securityAclService;
    @Mock
    Security security;

    @Test
    public void shouldCreateAcl() {

        DrugProgramme drugProgramme = new DrugProgramme(10l);
        String owner = "Glen";

        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        aclRestService.createAcl(mockUriInfo, owner, drugProgramme);

        verify(securityAclService, times(1)).createAcl(eq(drugProgramme), eq(owner));
        verifyNoMoreInteractions(securityAclService, securityAclService);
    }

    @Test(expected = WebApplicationException.class)
    public void shouldFailToCreateAclWithInvalidDrugProgramme() {

        DrugProgramme drugProgramme = new DrugProgramme(10l);
        String owner = "Glen";
        doThrow(new AlreadyExistsException("not found")).when(securityAclService).createAcl(any(DrugProgramme.class), eq(owner));

        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        try {
            aclRestService.createAcl(mockUriInfo, owner, drugProgramme);
        } catch (WebApplicationException ex) {
            verify(securityAclService, times(1)).createAcl(eq(drugProgramme), eq(owner));
            verifyNoMoreInteractions(securityAclService, securityAclService);
            
            assertThat(ex.getResponse().getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

            throw ex;
        }

    }
}
