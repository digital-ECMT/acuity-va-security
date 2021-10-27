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

import com.acuity.va.security.acl.domain.ActiveDirectoryRecord;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Glen
 */
public class UserResourceTest extends AbstractSpringContextJerseyTest {

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(UserResource.class);
    }

    /**
     * GET USER
     */
    @Test
    public void shouldReturnUserWithStatus200For_GetUser_ValidUser() {
        String USER = "user001";
        String FULLNAME = "fullName";
        when(mockUserService.getUser(any(String.class))).thenReturn(new AcuitySidDetails(USER, FULLNAME, Lists.newArrayList(new SimpleGrantedAuthority("GROUP"))));
        when(mockUserService.userExists(any(String.class))).thenReturn(true);

        Response response = target("user/" + USER).request().get(Response.class);

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).getUser(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        String json = response.readEntity(String.class);
        String userId = JsonPath.read(json, "$.userId");
        String fullName = JsonPath.read(json, "$.fullName");
        String firstGroup = JsonPath.read(json, "$.authoritiesAsString.[0]");
        List<Object> groups = JsonPath.read(json, "$.authoritiesAsString.[*]");
        Boolean enabled = JsonPath.read(json, "$.enabled");

        assertThat(userId).isEqualTo(USER);
        assertThat(fullName).isEqualTo(FULLNAME);
        assertThat(firstGroup).isEqualTo("GROUP");
        assertThat(groups).hasSize(1);
        assertThat(enabled).isTrue();
    }

    @Test
    public void shouldReturnStatus404For_GetUser_UnknownUser() {
        String USER = "user001";
        when(mockUserService.userExists(any(String.class))).thenReturn(false);

        Response response = target("user/" + USER).request().get(Response.class);

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * DELETE USER
     */
    @Test
    public void shouldDeleteUserWithStatus204For_DeleteUser_ValidUser() {
        String USER = "user001";
        when(mockUserService.userExists(USER)).thenReturn(true);

        Response response = target("user/" + USER).request().delete();

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).deleteUser(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldReturnNotFoundWithStatus404For_DeleteUser_UnknownUser() {
        String USER = "user001";
        when(mockUserService.userExists(USER)).thenReturn(false);

        Response response = target("user/" + USER).request().delete();

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * CREATE USER
     */
    @Test
    public void shouldCreateUserWithStatus201For_CreateUser_ValidUser() {
        String USER = "user001";
        String FULLNAME = "fullName";
        when(mockUserService.userExists(USER)).thenReturn(false);

        Response response = target("user/" + USER + "/" + FULLNAME).request().put(Entity.json(""));

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).createUser(USER, FULLNAME);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.readEntity(String.class)).isEqualTo(USER);
        assertThat(response.getLocation().getPath()).endsWith("/user/" + USER + "/" + FULLNAME);
    }

    @Test
    public void shouldCreateUserWithStatus409For_CreateUser_UnknownUser() {
        String USER = "user001";
        String FULLNAME = "fullName";
        when(mockUserService.userExists(USER)).thenReturn(true);

        Response response = target("user/" + USER + "/" + FULLNAME).request().put(Entity.json(""));

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(409);
    }

    @Test
    public void shouldSearchWithStatus200For_SearchUsers() {

        List<ActiveDirectoryRecord> users = Collections
                .singletonList(ActiveDirectoryRecord.builder()
                        .prid("name.surname").givenName("name").surname("surname")
                        .displayName("name surname").mail("mail").build());

        String FULLNAME = "fullName";
        when(mockPeopleResourceClient.searchUsersByName(any())).thenReturn(users);

        Response response = target("user/search/" + FULLNAME).request().get();

        verify(mockPeopleResourceClient, times(1)).searchUsersByName(any());
        verifyNoMoreInteractions(mockPeopleResourceClient);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldLinkUserWithStatus200For_linkUsers() {

        Response response = target("user/linkFromUser/link/linkToUser").request().post(Entity.json(""));

        verify(mockUserService, times(1)).linkUser(eq("linkFromUser"), eq("linkToUser"));
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldntLinkUserWithStatus409For_linkUsers() {

        doThrow(new IllegalArgumentException("not found")).when(mockUserService).linkUser(any(), any());

        Response response = target("user/linkFromUser/link/linkToUser").request().post(Entity.json(""));

        verify(mockUserService, times(1)).linkUser(any(), any());
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(409);
    }

    @Test
    public void shouldunLinkUserWithStatus200For_unlinkUsers() {

        Response response = target("user/linkFromUser/unlink").request().post(Entity.json(""));

        verify(mockUserService, times(1)).unlinkUser(any());
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(204);
    }
}
