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

import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DatasetsRequest;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;

import java.util.Set;

import javax.ws.rs.core.Response;

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

/**
 * @author Glen
 */
public class SecurityResourceTest extends AbstractSpringContextJerseyTest {

    private static ObjectMapper mapper;

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(SecurityResource.class);
    }

    /**
     * Load by username
     */
    @Test
    public void shouldReturnUserWithStatus200For_loadUserByUsername() {

        String NAME = "user001";
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(NAME, "fullName");
        acuityUserDetails.setAuthoritiesAsString(Lists.newArrayList("TEST"));

        when(mockCustomUserDetailsManager.loadUserByUsername(NAME)).thenReturn(acuityUserDetails);

        Response response = target("security/loadUserByUsername/" + NAME).request().get(Response.class);

        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        //verifyNoMoreInteractions(mockCustomUserDetailsManager);
        assertThat(response.getStatus()).isEqualTo(200);

        AcuitySidDetails returnedObject = response.readEntity(AcuitySidDetails.class);

        assertThat(returnedObject.getUsername()).isEqualTo(NAME);
        assertThat(returnedObject.getFullName()).isEqualTo("fullName");
        assertThat(returnedObject.getAuthoritiesAsString()).containsOnly("TEST");
        assertThat(returnedObject.getAuthorities()).hasSize(1);
    }

    @Test
    public void shouldReturnUserWithStatus200ForEmailUser_loadUserByUsername() {

        String NAME = "user001@gmail.com";
        AcuitySidDetails acuityUserDetails = new AcuitySidDetails(NAME, "fullName");
        acuityUserDetails.setAuthoritiesAsString(Lists.newArrayList("TEST"));

        when(mockCustomUserDetailsManager.loadUserByUsername(NAME)).thenReturn(acuityUserDetails);

        Response response = target("security/loadUserByUsername/" + NAME).request().get(Response.class);

        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        //verifyNoMoreInteractions(mockCustomUserDetailsManager);
        assertThat(response.getStatus()).isEqualTo(200);

        AcuitySidDetails returnedObject = response.readEntity(AcuitySidDetails.class);

        assertThat(returnedObject.getUsername()).isEqualTo(NAME);
        assertThat(returnedObject.getFullName()).isEqualTo("fullName");
        assertThat(returnedObject.getAuthoritiesAsString()).containsOnly("TEST");
        assertThat(returnedObject.getAuthorities()).hasSize(1);
    }

    /**
     * List permissions
     */
    @Test
    public void shouldReturnTrueForUserWithStatus202For_getAclsForUser() {

        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);
        List<AcuityObjectIdentityWithPermissionAndLockDown> list = newArrayList(new DrugProgramme(10L), new AcuityDataset(1L));

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.getUserObjectIdentities(eq(user))).thenReturn(list);

        Response response = target("security/acls/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getUserObjectIdentities(eq(user));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
     * Has Permission
     */
    @Test
    public void shouldReturnTrueForUserWithStatus202For_hasPermission() {

        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.hasPermission(any(AcuityObjectIdentity.class), anyInt(), eq(user.toSids()))).thenReturn(true);

        Response response = target("security/acl/DrugProgramme/1/32/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).hasPermission(any(AcuityObjectIdentity.class), eq(32), eq(user.toSids()));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void shouldReturnTrueForUserWithStatus202ForEmailUser_hasPermission() {

        String NAME = "user001@gmail.com";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.hasPermission(any(AcuityObjectIdentity.class), anyInt(), eq(user.toSids()))).thenReturn(true);

        Response response = target("security/acl/DrugProgramme/1/32/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).hasPermission(any(AcuityObjectIdentity.class), eq(32), eq(user.toSids()));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void shouldReturnFalseForUserWithStatus406For_hasPermission() {

        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.hasPermission(any(AcuityObjectIdentity.class), anyInt(), eq(user.toSids()))).thenReturn(false);

        Response response = target("security/acl/DrugProgramme/1/32/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).hasPermission(any(AcuityObjectIdentity.class), eq(32), eq(user.toSids()));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(406);
    }

    @Test
    public void shouldReturnPermissions200For_getPermissionForUser() {

        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.hasPermission(any(AcuityObjectIdentity.class), anyInt(), eq(user.toSids()))).thenReturn(false);

        Response response = target("security/acl/DrugProgramme/1/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getRolePermissionForUser(any(AcuityObjectIdentity.class), eq(user));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturnPermissions200For_getPermissionForUserDatasets() {

        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        DatasetsRequest datasetsRequest = new DatasetsRequest();
        datasetsRequest.setDatasets(newArrayList(new DetectDataset(11L), new DetectDataset(12L)));

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.hasPermission(any(AcuityObjectIdentity.class), anyInt(), eq(user.toSids()))).thenReturn(true);

        Response response = target("security/acl/haspermission/32/" + NAME).request().post(Entity.json(datasetsRequest), Response.class);

        verify(mockSecurityAclService, times(2)).hasPermission(any(AcuityObjectIdentity.class), eq(32), eq(user.toSids()));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void shouldReturnPermissions202For_hasAcuityDatasetPermissionForUserFromStudyId() {

        String studyId = "studyId";
        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);
        Optional<AcuityObjectIdentityWithPermissionAndLockDown> acuityDS = Optional.of(new AcuityDataset(1L));

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.getRoiFromStudyId(any(AcuitySidDetails.class), anyString())).thenReturn(acuityDS);

        Response response = target("security/acl/AcuityDataset/" + studyId + "/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getRoiFromStudyId(eq(user), eq(studyId));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturnPermissions406For_hasAcuityDatasetPermissionForUserFromStudyId() {

        String studyId = "studyId";
        String NAME = "user001";
        AcuitySidDetails user = AcuitySidDetails.toUser(NAME);

        when(mockCustomUserDetailsManager.loadUserByUsername(anyString())).thenReturn(user);
        when(mockSecurityAclService.getRoiFromStudyId(any(AcuitySidDetails.class), anyString())).thenReturn(Optional.empty());

        Response response = target("security/acl/AcuityDataset/" + studyId + "/" + NAME).request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getRoiFromStudyId(eq(user), eq(studyId));
        verify(mockCustomUserDetailsManager, times(1)).loadUserByUsername(NAME);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(406);
    }

    @Test
    public void shouldReturnPermissions404For_hasAcuityDatasetPermissionForUserFromStudyId() {

        String NAME1 = "user001";
        String NAME2 = "bob";
        AcuitySidDetails user1 = AcuitySidDetails.toUser(NAME1);
        AcuitySidDetails user2 = AcuitySidDetails.toUser(NAME2);
        AcuitySidDetailsWithPermissionMask sid1 = new AcuitySidDetailsWithPermissionMask(user1, 1);
        AcuitySidDetailsWithPermissionMask sid2 = new AcuitySidDetailsWithPermissionMask(user2, 1);

        when(mockSecurityAclService.getGrantedUsersForAcl(any(AcuityObjectIdentity.class))).thenReturn(newArrayList(sid1, sid2));

        Response response = target("security/acl/AcuityDataset/1").request().get(Response.class);

        List<AcuitySidDetails> returnedUsers = response.readEntity(new GenericType<List<AcuitySidDetails>>() {
        });

        System.out.println(returnedUsers);

        assertThat(returnedUsers).hasSize(2);
        assertThat(returnedUsers.get(0).getSidAsString()).isEqualTo(NAME1);

        verify(mockSecurityAclService, times(1)).getGrantedUsersForAcl(any(AcuityObjectIdentity.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturnPermissions200For_getUsersForDataset() {

        String NAME1 = "user001";
        String NAME2 = "bob";
        AcuitySidDetails user1 = AcuitySidDetails.toUser(NAME1);
        AcuitySidDetails user2 = AcuitySidDetails.toUser(NAME2);
        AcuitySidDetailsWithPermissionMask sid1 = new AcuitySidDetailsWithPermissionMask(user1, 1);
        AcuitySidDetailsWithPermissionMask sid2 = new AcuitySidDetailsWithPermissionMask(user2, 1);

        when(mockSecurityAclService.getGrantedUsersForAcl(any(AcuityObjectIdentity.class))).thenReturn(newArrayList(sid1, sid2));

        Response response = target("security/acl/AcuityDataset/1").request().get(Response.class);

        List<AcuitySidDetails> returnedUsers = response.readEntity(new GenericType<List<AcuitySidDetails>>() {
        });

        System.out.println(returnedUsers);

        assertThat(returnedUsers).hasSize(2);
        assertThat(returnedUsers.get(0).getSidAsString()).isEqualTo(NAME1);

        verify(mockSecurityAclService, times(1)).getGrantedUsersForAcl(any(AcuityObjectIdentity.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturnPermissions200For_getUsersForDatasets() {
        AcuityDataset acuityDataset10 = new AcuityDataset(10L);
        AcuityDataset acuityDataset20 = new AcuityDataset(20L);
        List<Dataset> datasetsList = newArrayList(acuityDataset10, acuityDataset20);

        String NAME1 = "user001";
        String NAME2 = "bob";
        String NAME3 = "bob3";
        AcuitySidDetails user1 = AcuitySidDetails.toUser(NAME1);
        AcuitySidDetails user2 = AcuitySidDetails.toUser(NAME2);
        AcuitySidDetails user3 = AcuitySidDetails.toUser(NAME3);
        AcuitySidDetailsWithPermissionMask sid1 = new AcuitySidDetailsWithPermissionMask(user1, 1);
        AcuitySidDetailsWithPermissionMask sid2 = new AcuitySidDetailsWithPermissionMask(user2, 1);
        AcuitySidDetailsWithPermissionMask sid3 = new AcuitySidDetailsWithPermissionMask(user3, 1);

        when(mockSecurityAclService.getGrantedUsersForAcl(eq(acuityDataset10))).thenReturn(newArrayList(sid1));
        when(mockSecurityAclService.getGrantedUsersForAcl(eq(acuityDataset20))).thenReturn(newArrayList(sid2, sid3));

        Response response = target("security/acl/datasets/listpermissions").request().post(Entity.json(new DatasetsRequest(datasetsList)), Response.class);

        List<AcuitySidDetails> returnedUsers = response.readEntity(new GenericType<List<AcuitySidDetails>>() {
        });

        System.out.println(returnedUsers);

        assertThat(returnedUsers).hasSize(3);
        assertThat(returnedUsers.get(0).getSidAsString()).isEqualTo(NAME1);

        verify(mockSecurityAclService, times(2)).getGrantedUsersForAcl(any(AcuityObjectIdentity.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
