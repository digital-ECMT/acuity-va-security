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

import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithParent;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.acuity.va.security.rest.util.UserPermission;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;

import static com.acuity.va.security.acl.permissions.AcuityPermissions.*;
import com.acuity.va.security.rest.util.ViewUserPermission;
import static org.mockito.Mockito.*;

import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import org.quartz.SchedulerException;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Glen
 */
public class AclResourceTest extends AbstractSpringContextJerseyTest {

    @Autowired
    SecurityAclService securityAclService;

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(AclResource.class);
    }


    /**
     * LIST PERMISSIONS
     */
    @Test
    public void shouldListPermissionWithStatus200For_listPermissons() {

        when(mockSecurityAclService.getAllPermissionForAcl(any())).
                thenReturn(Lists.newArrayList(
                        new AcuitySidDetailsWithPermissionMaskAndGranted(new AcuitySidDetails("User1", "FN"), EDIT_AUTHORISERS.getMask(), true),
                        new AcuitySidDetailsWithPermissionMaskAndGranted(new AcuitySidDetails("User2", "FN2"), EDIT_ADMINISTRATORS.getMask(), false),
                        new AcuitySidDetailsWithPermissionMaskAndGranted(AcuitySidDetails.toUserFromGroup("Group1"), EDIT_ADMINISTRATORS.getMask(), false)
                )
                );

        Response response = target("acl/DrugProgramme/100/ace").request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getAllPermissionForAcl(any());
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);

        // Returned Json is, using JsonPath to parse
        // [{"user":{"userId":"User1","fullName":"FN","authorities":[],"enabled":true},"permissionMask":256}]
        String json = response.readEntity(String.class);
        String userId1 = JsonPath.read(json, "$.[0].user.userId");
        String sid1 = JsonPath.read(json, "$.[0].user.sidAsString");
        String fullName1 = JsonPath.read(json, "$.[0].user.fullName");
        Integer permissionMask1 = JsonPath.read(json, "$.[0].permissionMask");
        boolean granting1 = JsonPath.read(json, "$.[0].granted");

        String userId2 = JsonPath.read(json, "$.[1].user.userId");
        String fullName2 = JsonPath.read(json, "$.[1].user.fullName");
        Integer permissionMask2 = JsonPath.read(json, "$.[1].permissionMask");
        boolean granting2 = JsonPath.read(json, "$.[1].granted");
        List<Object> objects = JsonPath.read(json, "$.[*]");

        String userId3 = JsonPath.read(json, "$.[2].user.userId");
        Boolean isGroup = JsonPath.read(json, "$.[2].user.group");

        assertThat(userId1).isEqualTo(sid1).isEqualTo("User1");
        assertThat(fullName1).isEqualTo("FN");
        assertThat(permissionMask1).isEqualTo(EDIT_AUTHORISERS.getMask());
        assertThat(granting1).isTrue();

        assertThat(userId2).isEqualTo("User2");
        assertThat(fullName2).isEqualTo("FN2");
        assertThat(permissionMask2).isEqualTo(EDIT_ADMINISTRATORS.getMask());
        assertThat(granting2).isFalse();
        assertThat(objects).hasSize(3);

        assertThat(userId3).isEqualTo("Group1");
        assertThat(isGroup).isTrue();
    }

    @Test
    public void shouldFailWithStatus404For_listPermissons_InvalidId() {

        when(mockSecurityAclService.getAllPermissionForAcl(any())).thenThrow(new NotFoundException("not found"));

        Response response = target("acl/DrugProgramme/2323/ace").request().get(Response.class);

        verify(mockSecurityAclService, times(1)).getAllPermissionForAcl(any());
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * ADD PERMISSIONS
     */
    @Test
    public void shouldAddPermissionsWithStatus201For_addPermissons_ValidUserAndObject() {

        UserPermission userPermission1 = new UserPermission();
        userPermission1.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission1.setPermissionMask(32);
        userPermission1.setGranting(true);

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setAcuitySidDetails(AcuitySidDetails.toUser("Bob"));
        userPermission2.setPermissionMask(32);
        userPermission2.setGranting(false);

        when(mockUserService.isValidUser(any())).thenReturn(true);

        Response response = target("acl/DrugProgramme/1/aces").request().post(Entity.json(newArrayList(userPermission1, userPermission2)), Response.class);

        verify(mockUserService, times(2)).isValidUser(anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission1.getAcuitySidDetails().getSidAsString()), anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission2.getAcuitySidDetails().getSidAsString()), anyString());
        verify(mockSecurityAclService, times(2)).getSidsAceForAcl(any(DrugProgramme.class), any());

        verify(mockAclRestService, times(2)).addPermissionForASid(any(UriRoutingContext.class), eq("DrugProgramme"), eq(1L), eq(true), any());
        verify(mockSecurityAclService).getAllPermissionForAcl(any());
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldAddPermissionsWithStatus201For_addPermissonsCheck_ValidUserAndObject() {

        UserPermission userPermission1 = new UserPermission();
        userPermission1.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission1.setPermissionMask(32);
        userPermission1.setGranting(true);

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setAcuitySidDetails(AcuitySidDetails.toUser("Bob"));
        userPermission2.setPermissionMask(32);
        userPermission2.setGranting(false);

        when(mockUserService.isValidUser(any())).thenReturn(true);

        Response response = target("acl/DrugProgramme/1/aces").queryParam("check", true).request().post(Entity.json(newArrayList(userPermission1, userPermission2)), Response.class);

        verify(mockUserService, times(2)).isValidUser(anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission1.getAcuitySidDetails().getSidAsString()), anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission2.getAcuitySidDetails().getSidAsString()), anyString());
        verify(mockSecurityAclService, times(2)).getSidsAceForAcl(any(DrugProgramme.class), any());
        verify(mockAclRestService, times(2)).addPermissionForASid(any(UriRoutingContext.class), eq("DrugProgramme"), eq(1L), eq(true), any());
        //verify(mockAclRestService).addPermissionForASid(any(UriRoutingContext.class), eq("DrugProgramme"), eq(1L), eq(true), any());
        verify(mockSecurityAclService).getAllPermissionForAcl(any());
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldSwapAddPermissionsWithStatus201For_swapaddPermissons_ValidUserAndObject() {

        UserPermission userPermission1 = new UserPermission();
        userPermission1.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission1.setPermissionMask(32);
        userPermission1.setGranting(true);

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setAcuitySidDetails(AcuitySidDetails.toUser("Bob"));
        userPermission2.setPermissionMask(32);
        userPermission2.setGranting(false);

        when(mockUserService.isValidUser(any())).thenReturn(true);

        Response response = target("acl/DrugProgramme/1/aces").queryParam("check", false).request().post(Entity.json(newArrayList(userPermission1, userPermission2)), Response.class);

        verify(mockUserService, times(2)).isValidUser(anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission1.getAcuitySidDetails().getSidAsString()), anyString());
        verify(mockUserService).createUserIfNotExistAndAddToGroup(eq(userPermission2.getAcuitySidDetails().getSidAsString()), anyString());

        verify(mockAclRestService, times(2)).addPermissionForASid(any(UriRoutingContext.class), eq("DrugProgramme"), eq(1L), eq(true), any());
        verify(mockSecurityAclService).getAllPermissionForAcl(any());
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldNotAddPermissionsWithStatus400For_addPermissons_InvalidValidUserAndObject() {

        UserPermission userPermission1 = new UserPermission();
        userPermission1.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission1.setPermissionMask(32);
        userPermission1.setGranting(true);

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setAcuitySidDetails(AcuitySidDetails.toUser("Bob"));
        userPermission2.setPermissionMask(32);
        userPermission2.setGranting(false);

        when(mockUserService.isValidUser(any())).thenReturn(false);

        Response response = target("acl/DrugProgramme/1/aces").request().post(Entity.json(newArrayList(userPermission1, userPermission2)), Response.class);

        verify(mockUserService, times(2)).isValidUser(anyString());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldFailAddPermissionsWithStatus201For_addPermissons_DifferentPermissions() {

        UserPermission userPermission1 = new UserPermission();
        userPermission1.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission1.setPermissionMask(32);
        userPermission1.setGranting(true);

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setAcuitySidDetails(AcuitySidDetails.toUser("Bob"));
        userPermission2.setPermissionMask(345);
        userPermission2.setGranting(false);

        Response response = target("acl/DrugProgramme/1/aces").request().post(Entity.json(newArrayList(userPermission1, userPermission2)), Response.class);

        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(412);
    }

    /**
     * ADD PERMISSION
     */
    @Test
    public void shouldAddPermissionWithStatus201For_addPermissons_ValidUserAndObject() {

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission.setPermissionMask(32);
        userPermission.setGranting(true);

        Response response = target("acl/DrugProgramme/1/ace").request().post(Entity.json(userPermission), Response.class);

        verify(mockAclRestService, times(1)).addPermissionForASid(any(), eq("DrugProgramme"), eq(1L), eq(true), any(UserPermission.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldAddPermissionWithStatus201For_addPermissonsOverwriteTrue_ValidUserAndObject() {

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission.setPermissionMask(32);
        userPermission.setGranting(true);

        Response response = target("acl/DrugProgramme/1/ace").queryParam("overwrite", "true").request().
                post(Entity.json(userPermission), Response.class);

        verify(mockAclRestService, times(1)).addPermissionForASid(any(), eq("DrugProgramme"), eq(1L), eq(true), any(UserPermission.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldAddPermissionWithStatus201For_addPermissonsOverwriteFalse_ValidUserAndObject() {

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("Glen"));
        userPermission.setPermissionMask(32);
        userPermission.setGranting(true);

        Response response = target("acl/DrugProgramme/1/ace").
                queryParam("overwrite", "false").request().post(Entity.json(userPermission), Response.class);

        verify(mockAclRestService, times(1)).addPermissionForASid(any(), eq("DrugProgramme"), eq(1L), eq(false), any(UserPermission.class));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    /**
     * REMOVE PERMISSION/ACE
     */
    @Test
    public void shouldRemovePermissionWithStatus201For_removePermissons_ValidUserAndObject() throws SchedulerException {

        UserPermission userPermission = new UserPermission();
        AcuitySidDetails acuitySidDetails = AcuitySidDetails.toUser("Glen");
        userPermission.setAcuitySidDetails(acuitySidDetails);
        userPermission.setPermissionMask(32);

        Response response = target("acl/DrugProgramme/1/deleteace").request().post(Entity.json(userPermission), Response.class);

        verify(mockSecurityAclService, times(1)).removeAce(any(DrugProgramme.class), eq(userPermission.getPermissionMask()), eq(acuitySidDetails.toSid()));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldRemoveAllPermissionsWithStatus201For_removePermissons_ValidUserAndObject() throws SchedulerException {

        UserPermission userPermission = new UserPermission();
        AcuitySidDetails acuitySidDetails = AcuitySidDetails.toUser("Glen");
        userPermission.setAcuitySidDetails(acuitySidDetails);

        Response response = target("acl/DrugProgramme/1/deleteace").request().post(Entity.json(userPermission), Response.class);

        verify(mockSecurityAclService, times(1)).removeAces(any(DrugProgramme.class), eq(acuitySidDetails.toSid()));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldNotRemovePermissionWithStatus404For_removePermissons_InvalidDrugProgram() {

        UserPermission userPermission = new UserPermission();
        AcuitySidDetails acuitySidDetails = AcuitySidDetails.toUser("Glen");
        userPermission.setAcuitySidDetails(acuitySidDetails);
        userPermission.setPermissionMask(32);

        doThrow(new NotFoundException("not found")).when(mockSecurityAclService).removeAce(any(DrugProgramme.class), eq(32), eq(acuitySidDetails.toSid()));

        Response response = target("acl/DrugProgramme/1/deleteace").request().post(Entity.json(userPermission), Response.class);

        verify(mockSecurityAclService, times(1)).removeAce(any(DrugProgramme.class), eq(32), eq(acuitySidDetails.toSid()));
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    /**
     * CREATE ACL
     */
    @Test
    public void shouldAddAclWithStatus201For_addAcl_ValidUserAndObject() {

        String userId = "Glen";
        AcuityObjectIdentityWithParent drugProgram = new DrugProgramme(10L, "Drug A");
        drugProgram.setParent(new ClinicalStudy(101L, "ClinicalStudy BA"));

        Response response = target("acl").queryParam("ownerId", userId).request().post(Entity.json(drugProgram), Response.class);

        verify(mockAclRestService, times(1)).createAcl(any(), eq(userId), any(DrugProgramme.class));
        verifyNoMoreInteractions(mockAclRestService);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldNotAddAclWithStatus409For_addAcl_InvalidDrugProgram() {

        String userId = "Glen";
        Integer permissionMask = 32;
        DrugProgramme drugProgram = new DrugProgramme(10L, "Drug A");
        doThrow(new WebApplicationException("not found", Response.Status.CONFLICT)).when(mockAclRestService).createAcl(any(), eq(userId), any(DrugProgramme.class));

        Response response = target("acl").queryParam("ownerId", userId).request().post(Entity.json(drugProgram), Response.class);

        verify(mockAclRestService, times(1)).createAcl(any(), eq(userId), any(DrugProgramme.class));
        verifyNoMoreInteractions(mockAclRestService);
        assertThat(response.getStatus()).isEqualTo(409);
    }
   
    /**
     * SET LOCKDOWN
     */
    @Test
    public void shouldSetLockdownStatusForForAclWithStatus204() {

        when(mockSecurityAclService.setLockdownStatus(any(DetectDataset.class), eq(true))).thenReturn(true);

        Response response = target("acl/DetectDataset/1/setlockdown").request().post(Entity.json(""), Response.class);

        verify(mockSecurityAclService, times(1)).setLockdownStatus(any(DetectDataset.class), eq(true));
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    /**
     * UNSET LOCKDOWN
     */
    @Test
    public void shouldUnsetLockdownStatusForForAclWithStatus204() {

        when(mockSecurityAclService.setLockdownStatus(any(DetectDataset.class), eq(true))).thenReturn(true);

        Response response = target("acl/DetectDataset/1/unsetlockdown").request().post(Entity.json(""), Response.class);

        verify(mockSecurityAclService, times(1)).setLockdownStatus(any(DetectDataset.class), eq(false));
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    /**
     * SET INHERIT PERMISSION
     */
    @Test
    public void shouldSetInheritPermissionsForForAclWithStatus204() {

        when(mockSecurityAclService.setInheritPermissions(any(DetectDataset.class), eq(true))).thenReturn(true);

        Response response = target("acl/DetectDataset/1/setInheritPermission").request().post(Entity.json(""), Response.class);

        verify(mockSecurityAclService, times(1)).setInheritPermissions(any(DetectDataset.class), eq(true));
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldUnsetInheritPermissionsForForAclWithStatus204() {

        when(mockSecurityAclService.setInheritPermissions(any(DetectDataset.class), eq(false))).thenReturn(true);

        Response response = target("acl/DetectDataset/1/unsetInheritPermission").request().post(Entity.json(""), Response.class);

        verify(mockSecurityAclService, times(1)).setInheritPermissions(any(DetectDataset.class), eq(false));
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(204);
    }
    
    @Test
    public void shouldSetViewModulesPermissionsWithStatus204() {

        ViewUserPermission viewuserPermission = new ViewUserPermission();
        AcuitySidDetails acuitySidDetails = AcuitySidDetails.toUser("Glen");
        viewuserPermission.setAcuitySidDetails(acuitySidDetails);
        viewuserPermission.setViewPermissionMasks(newArrayList(32));

        Response response = target("acl/DetectDataset/1/viewpackagesaces").request().post(Entity.json(viewuserPermission), Response.class);

        verify(mockSecurityAclService, times(1)).setViewPackagesAces(any(DetectDataset.class), any(), any());
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    public void shouldSetExtraViewModulesPermissionsWithStatus204() {

        ViewUserPermission viewuserPermission = new ViewUserPermission();
        AcuitySidDetails acuitySidDetails = AcuitySidDetails.toUser("Glen");
        viewuserPermission.setAcuitySidDetails(acuitySidDetails);
        viewuserPermission.setViewPermissionMasks(newArrayList(32));

        Response response = target("acl/DetectDataset/1/viewextrapackagesaces").request().post(Entity.json(viewuserPermission), Response.class);

        verify(mockSecurityAclService, times(1)).setExtraViewPackagesAces(any(DetectDataset.class), any(), any());
        verifyNoMoreInteractions(mockSecurityAclService, mockAcuityObjectService, mockUserRepository);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
