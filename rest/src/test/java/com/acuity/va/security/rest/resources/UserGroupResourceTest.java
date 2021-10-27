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

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.common.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Glen
 */
public class UserGroupResourceTest extends AbstractSpringContextJerseyTest {

    private static final String USER = "user1";
    private static final String GROUP = "group1";

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(UserGroupResource.class);
    }

    /**
     * GET USER'S GROUPS
     */
    @Test
    public void shouldReturnGroupsWithStatus200For_GetUser_ValidUser() {

        String FULLNAME = "user1 Name";
        when(mockUserService.getUser(USER)).thenReturn(new AcuitySidDetails(USER, FULLNAME, Lists.newArrayList(new SimpleGrantedAuthority(GROUP))));
        when(mockUserService.userExists(USER)).thenReturn(true);

        Response response = target("users/" + USER + "/groups").request().get(Response.class);

        verify(mockUserService, times(1)).getUser(USER);
        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);
        List<String> returnedGrantedAuthorities = response.readEntity(ArrayList.class);
        assertThat(returnedGrantedAuthorities).containsOnly(GROUP);
    }

    @Test
    public void shouldReturnStatus404For_GetUser_InvalidUser() {

        when(mockUserService.userExists(USER)).thenReturn(false);

        Response response = target("users/" + USER + "/groups").request().get(Response.class);

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * ADD GROUP TO USER
     */
    @Test
    public void shouldAddUserToGroupWithStatus201For_AddUser_ValidUser() {

        when(mockUserService.userExists(USER)).thenReturn(true);
        when(mockUserService.groupExists(GROUP)).thenReturn(true);

        Response response = target("users/" + USER + "/groups").request().put(Entity.json(GROUP));

        verify(mockUserService, times(1)).addUserToGroup(USER, GROUP);
        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void shouldReturnStatus404For_AddUser_InvalidUser() {

        when(mockUserService.userExists(USER)).thenReturn(false);

        Response response = target("users/" + USER + "/groups").request().put(Entity.json(GROUP));

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturnStatus404For_AddUser_InvalidGroup() {

        when(mockUserService.userExists(USER)).thenReturn(true);
        when(mockUserService.groupExists(GROUP)).thenReturn(false);

        Response response = target("users/" + USER + "/groups").request().put(Entity.json(GROUP));

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * CREATE USER AND ADD GROUP TO USER
     */
    @Test
    public void shouldAddUserToGroupWithStatus201For_Create_User_ValidUser() {

        String FULLNAME = "Fred Bloggs";
        when(mockUserService.groupExists(GROUP)).thenReturn(true);
        
        Response response = target("users/" + USER + "/groups/" + GROUP + "/fullName/" + FULLNAME).request().put(Entity.json(""));
        
        verify(mockUserService, times(1)).createUser(eq(USER), eq(FULLNAME));
        verify(mockUserService, times(1)).addUserToGroup(USER, GROUP);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void shouldReturnStatus404For_CreateUser_InvalidGroup() {

        String FULLNAME = "Fred Bloggs";
        when(mockUserService.groupExists(GROUP)).thenReturn(false);

        Response response = target("users/" + USER + "/groups/" + GROUP + "/fullName/" + FULLNAME).request().put(Entity.json(""));

        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }
    
    
    /**
     * DELETE GROUP FROM USER
     */
    @Test
    public void shouldDeleteUserFromGroupWithStatus204For_DeleteGroup_ValidUser() {

        when(mockUserService.userExists(USER)).thenReturn(true);
        when(mockUserService.groupExists(GROUP)).thenReturn(true);

        Response response = target("users/" + USER + "/groups/" + GROUP).request().delete();

        verify(mockUserService, times(1)).removeUserFromGroup(USER, GROUP);
        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldReturnStatus404For_DeleteGroup_InvalidUser() {

        when(mockUserService.userExists(USER)).thenReturn(false);

        Response response = target("users/" + USER + "/groups/" + GROUP).request().delete();

        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturnStatus404For_DeleteGroup_InvalidGroup() {

        when(mockUserService.userExists(USER)).thenReturn(true);
        when(mockUserService.groupExists(GROUP)).thenReturn(false);

        Response response = target("users/" + USER + "/groups/" + GROUP).request().delete();

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }
    
    /**
     * DELETE USER FROM TRAINED USER GROUP AND REMOVE ACES
     */
    @Test
    public void shouldDeleteUserFromTrainedUserGroupWithStatus204For_DeleteGroup_ValidUser() {

        when(mockUserService.userExists(USER)).thenReturn(true);

        Response response = target("users/" + USER + "/groups/trainedUserGroup").request().delete();

        verify(mockUserService, times(1)).removeUserFromGroup(USER, Constants.TRAINED_USER_GROUP);
        verify(mockUserService, times(1)).userExists(USER);
        verify(mockSecurityAclService, times(1)).removeAllAcesAndGroupsForSid(USER);
        verifyNoMoreInteractions(mockUserService);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }
    
    @Test
    public void shouldDeleteUserFromTrainedUserGroupWithoutAcesWithStatus204For_DeleteGroup_ValidUser() {

        when(mockUserService.userExists(USER)).thenReturn(true);

        Response response = target("users/" + USER + "/groups/trainedUserGroup").queryParam("deleteAces", "false").request().delete();

        verify(mockUserService, times(1)).removeUserFromGroup(USER, Constants.TRAINED_USER_GROUP);
        verify(mockUserService, times(1)).userExists(USER);
        verifyNoMoreInteractions(mockUserService);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }
    
    /**
     * Create user and add to Group
     */
    @Test
    public void shouldCreateUserAndToGroupWithStatus201For_CreateUserAndAddToGroup_ValidUser() {

        String FULLNAME = "username including spaces";       
        when(mockUserService.groupExists(GROUP)).thenReturn(true);

        Response response = target("users/" + USER + "/groups/" + GROUP + "/fullName/" + FULLNAME).request().put(Entity.json(GROUP));

        verify(mockUserService, times(1)).userExists(USER);
        verify(mockUserService, times(1)).groupExists(GROUP);
        verify(mockUserService, times(1)).createUser(USER, FULLNAME);
        verify(mockUserService, times(1)).addUserToGroup(USER, GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void shouldReturnStatus404For_CreateUserAndAddToGroup_InvalidGroup() {

        String FULLNAME = "username including spaces";     
        when(mockUserService.groupExists(GROUP)).thenReturn(false);

        Response response = target("users/" + USER + "/groups/" + GROUP + "/fullName/" + FULLNAME).request().put(Entity.json(GROUP));
        
        verify(mockUserService, times(1)).groupExists(GROUP);
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
