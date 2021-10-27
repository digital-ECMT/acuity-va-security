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

import com.acuity.va.security.acl.domain.GroupAuthorityConverter;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.GroupWithLockdown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.GenericType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Glen
 */
public class GroupResourceTest extends AbstractSpringContextJerseyTest {

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(GroupResource.class);
    }

    /**
     * LIST ALL
     */
    @Test
    public void shouldListAllAcuityUsersWithStatus200For_listAll() {
        when(mockUserService.getAcuityUsersByGroup(anyString())).thenReturn(Lists.newArrayList(new AcuitySidDetails("User1", ""), new AcuitySidDetails("User2", "")));
        when(mockUserService.groupExists("groupA")).thenReturn(true);

        Response response = target("groups/groupA").request().get(Response.class);

        verify(mockUserService, times(1)).getAcuityUsersByGroup(anyString());
        verify(mockUserService, times(1)).groupExists("groupA");
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        String json = response.readEntity(String.class);
        String firstUserId = JsonPath.read(json, "$.[0].userId");
        String secondUserId = JsonPath.read(json, "$.[1].userId");
        List<Object> users = JsonPath.read(json, "$.[*]");
        assertThat(users).hasSize(2);
        assertThat(firstUserId).isEqualTo("User1");
        assertThat(secondUserId).isEqualTo("User2");
    }

    @Test
    public void shouldReturnStatus404for_listAll_InvalidGroup() {
        when(mockUserService.getAcuityUsersByGroup(anyString())).thenReturn(Lists.newArrayList(new AcuitySidDetails("User1", ""), new AcuitySidDetails("User2", "")));
        when(mockUserService.groupExists("groupA")).thenReturn(false);

        Response response = target("groups/groupA").request().get(Response.class);

        verify(mockUserService, times(1)).groupExists("groupA");
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * LIST ALL NOT IN
     */
    @Test
    public void shouldListAllAcuityUsersWithStatus200For_listAllNotInGroup() {
        when(mockUserService.getAcuityUsersNotInGroup(anyString())).thenReturn(Lists.newArrayList(new AcuitySidDetails("User1", ""), new AcuitySidDetails("User2", "")));
        when(mockUserService.groupExists("groupA")).thenReturn(true);

        Response response = target("groups/not/groupA").request().get(Response.class);

        verify(mockUserService, times(1)).getAcuityUsersNotInGroup(anyString());
        verify(mockUserService, times(1)).groupExists("groupA");
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        String json = response.readEntity(String.class);
        String firstUserId = JsonPath.read(json, "$.[0].userId");
        String secondUserId = JsonPath.read(json, "$.[1].userId");
        List<Object> users = JsonPath.read(json, "$.[*]");
        assertThat(users).hasSize(2);
        assertThat(firstUserId).isEqualTo("User1");
        assertThat(secondUserId).isEqualTo("User2");
    }

    /**
     * LIST ALL
     */
    @Test
    public void shouldListAllGroupsWithStatus200For_listAllGroups() {
        List<String> groups = Lists.newArrayList("A", "B");

        when(mockUserService.listGroups()).thenReturn(groups);

        Response response = target("groups").request().get(Response.class);

        verify(mockUserService, times(1)).listGroups();
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        List<String> returnedGroups = response.readEntity(new GenericType<List<String>>() {
        });

        assertThat(returnedGroups).isEqualTo(groups);
    }

    @Test
    public void shouldListAllGroupsWithLockdownStatus200For_listAllGroups() {
        List<GroupWithLockdown> groups = Lists.newArrayList(new GroupWithLockdown("A", true), new GroupWithLockdown("B", true));

        when(mockUserService.listGroupsWithLockdown()).thenReturn(groups);

        Response response = target("groups/withlockdown").request().get(Response.class);

        verify(mockUserService, times(1)).listGroupsWithLockdown();
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        List<GroupWithLockdown> returnedGroups = response.readEntity(new GenericType<List<GroupWithLockdown>>() {
        });

        assertThat(returnedGroups).isEqualTo(groups);
    }
    
    @Test
    public void shouldListAllGroupsWithDatasetsAndLockdownStatus200For_listAllGroups() {
        List<GroupWithItsLockdownDatasets> groups = newArrayList(
                new GroupWithItsLockdownDatasets("A", newArrayList()), 
                new GroupWithItsLockdownDatasets("B", newArrayList())
        );

        when(mockUserService.listGroupsWithLockdownAndDatasets()).thenReturn(groups);

        Response response = target("groups/withdatasetsandlockdown").request().get(Response.class);

        verify(mockUserService, times(1)).listGroupsWithLockdownAndDatasets();
        verifyNoMoreInteractions(mockUserService);
        assertThat(response.getStatus()).isEqualTo(200);

        List<GroupWithItsLockdownDatasets> returnedGroups = response.readEntity(new GenericType<List<GroupWithItsLockdownDatasets>>() {
        });

        assertThat(returnedGroups).isEqualTo(groups);
    }

    /**
     * REMOVE A GROUP
     */
    @Test
    public void shouldRemoveAGroupWithStatus200For_removeGroup() {
        String group = "removedGroup";

        when(mockUserService.groupExists(any())).thenReturn(true);

        Response response = target("groups/" + group).request().delete(Response.class);

        verify(mockUserService, times(1)).removeGroup(group);
        verify(mockUserService, times(1)).groupExists(group);
        verify(mockSecurityAclService, times(1)).removeAllAcesForSid(group + GroupAuthorityConverter.AUTHORITY);
        verifyNoMoreInteractions(mockUserService, mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }
}
