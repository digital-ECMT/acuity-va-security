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

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import com.acuity.va.security.acl.domain.AcuityDataset;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityImpl;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.Dataset;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.PROACT_ONLY_USER;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.mockito.Mockito;

import static java.lang.System.out;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Glen
 */
public class MyResourceTest extends AbstractSpringContextJerseyTest {

    @Override
    protected Set<Class<?>> getResourcesToLoad() {
        return Sets.newHashSet(MyResource.class);
    }

    /**
     * WHOAMI
     */
    @Test
    public void shouldReturnUserNameWithStatus200For_whoami() {

        when(mockSecurity.getAcuityUserDetails()).thenReturn(new AcuitySidDetails("user001", "fullName"));

        Response response = target("my/user/whoami").request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        verifyNoMoreInteractions(mockSecurity);
        assertThat(response.getStatus()).isEqualTo(200);

        String json = response.readEntity(String.class);
        String userId = JsonPath.read(json, "$.userId");
        String fullName = JsonPath.read(json, "$.fullName");
        assertThat(userId).isEqualTo("user001");
        assertThat(fullName).isEqualTo("fullName");
    }

    @Test
    public void shouldReturnUnauthorisedWithStatus401For_whoami_WithSecurityOff() {

        when(mockSecurity.getAcuityUserDetails()).thenThrow(new IllegalStateException());

        Response response = target("my/user/whoami").request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        verifyNoMoreInteractions(mockSecurity);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    /**
     * LIST CURRENT USER ACLS
     */
    @Test
    public void shouldReturnListOfAclForUserWithStatus200For_acl() {

        AcuitySidDetails USER = new AcuitySidDetails("user001");
        ClinicalStudy clinicalStudy = new ClinicalStudy(1L, "Study 1");
        clinicalStudy.setDrugProgramme("drugP");
        clinicalStudy.setViewPermissionMask(132);
        clinicalStudy.setRolePermissionMask(133);
        
        when(mockSecurity.getAcuityUserDetails()).thenReturn(USER);
        when(mockSecurityAclService.getUserObjectIdentities(USER, false)).
                thenReturn(Lists.newArrayList(new DrugProgramme(2L, "Drug A", 32), clinicalStudy));

        Response response = target("my/acl").request().get(Response.class);

        verify(mockSecurityAclService, Mockito.times(1)).getUserObjectIdentities(USER, false);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
        List<AcuityObjectIdentityImpl> returnedObjectIdentitys = response.readEntity(new GenericType<List<AcuityObjectIdentityImpl>>() {
        });
        assertThat(returnedObjectIdentitys).hasSize(2);
        assertThat(returnedObjectIdentitys).extracting("identifier").containsExactly(2L, 1L);
        assertThat(returnedObjectIdentitys).extracting("name").containsExactly("Drug A", "Study 1");
        assertThat(returnedObjectIdentitys).extracting("type").
            containsExactly("com.acuity.va.security.acl.domain.DrugProgramme", "com.acuity.va.security.acl.domain.ClinicalStudy");
        assertThat(returnedObjectIdentitys).extracting("permissionMask").containsExactly(32, 133);

        returnedObjectIdentitys.forEach(out::println);
    }

    @Test
    public void shouldReturnListOfAclForUserWithStatus200For_aclDatasetType() {

        Dataset dataset = new AcuityDataset(21L, "Drug B", 12);
        dataset.setDrugProgramme("DP5");
        AcuitySidDetails USER = new AcuitySidDetails("user001");
        when(mockSecurity.getAcuityUserDetails()).thenReturn(USER);
        when(mockSecurityAclService.getUserObjectIdentities(USER, false)).thenReturn(Lists.newArrayList(dataset));

        Response response = target("my/acl").request().get(Response.class);

        verify(mockSecurityAclService, Mockito.times(1)).getUserObjectIdentities(USER, false);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
        List<AcuityDataset> returnedObjectIdentitys = response.readEntity(new GenericType<List<AcuityDataset>>() {
        });
        assertThat(returnedObjectIdentitys).hasSize(1);
        assertThat(returnedObjectIdentitys).extracting("identifier").containsExactly(21L);
        assertThat(returnedObjectIdentitys).extracting("name").containsExactly("Drug B");
        assertThat(returnedObjectIdentitys).extracting("type").containsExactly("com.acuity.va.security.acl.domain.AcuityDataset");
        assertThat(returnedObjectIdentitys).extracting("permissionMask").containsExactly(12);
        assertThat(returnedObjectIdentitys.get(0).getDrugProgramme()).isEqualTo("DP5");

        returnedObjectIdentitys.forEach(out::println);
    }
    
    @Test
    public void shouldReturnListOfAclWithoutAuthusersForUserWithStatus200For_aclDatasetType() {

        Dataset dataset = new AcuityDataset(20L, "Drug B", 12);
        dataset.setDrugProgramme("DP5");
        dataset.setRolePermissionMask(AUTHORISED_USER.getMask());
        
        Dataset dataset1 = new AcuityDataset(21L, "Drug B1", 12);
        dataset1.setDrugProgramme("DP5");
        dataset1.setRolePermissionMask(DEVELOPMENT_TEAM.getMask());
        
        Dataset dataset2 = new AcuityDataset(22L, "Drug B2", 12);
        dataset2.setDrugProgramme("DP5");
        dataset2.setRolePermissionMask(PROACT_ONLY_USER.getMask());
        
        AcuitySidDetails USER = new AcuitySidDetails("user001");
        when(mockSecurity.getAcuityUserDetails()).thenReturn(USER);
        when(mockSecurityAclService.getUserObjectIdentities(USER, false)).thenReturn(Lists.newArrayList(dataset, dataset1, dataset2));

        Response response = target("my/aclswithoutauthorisedusers").request().get(Response.class);

        verify(mockSecurityAclService, Mockito.times(1)).getUserObjectIdentities(USER, false);
        verifyNoMoreInteractions(mockSecurityAclService);
        assertThat(response.getStatus()).isEqualTo(200);
        List<AcuityDataset> returnedObjectIdentitys = response.readEntity(new GenericType<List<AcuityDataset>>() {
        });
        assertThat(returnedObjectIdentitys).hasSize(1);
        assertThat(returnedObjectIdentitys).extracting("identifier").containsExactly(21L);
        assertThat(returnedObjectIdentitys).extracting("name").containsExactly("Drug B1");
        assertThat(returnedObjectIdentitys).extracting("type").containsExactly("com.acuity.va.security.acl.domain.AcuityDataset");
        assertThat(returnedObjectIdentitys).extracting("rolePermissionMask").containsExactly(DEVELOPMENT_TEAM.getMask());
        assertThat(returnedObjectIdentitys.get(0).getDrugProgramme()).isEqualTo("DP5");

        returnedObjectIdentitys.forEach(out::println);
    }

    @Test
    public void shouldReturnUnauthorisedWithStatus401For_acl_WithSecurityOff() {

        when(mockSecurity.getAcuityUserDetails()).thenThrow(new IllegalStateException());

        Response response = target("my/acl").request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    /**
     * HAS CURRENT USER GOT PERMISSION
     */
    @Test
    public void shouldGivePermissionWithStatus202For_hasPermission_DrugProgram() {

        AcuitySidDetails USER = new AcuitySidDetails("user001");
        int MASK = 32;
        DrugProgramme drugProgram = new DrugProgramme(2L, "Drug A");
        when(mockSecurity.getAcuityUserDetails()).thenReturn(USER);
        when(mockSecurityAclService.hasPermission(eq(drugProgram), eq(MASK), eq(USER.toSids()))).thenReturn(true);

        Response response = target("my/acl/DrugProgramme/2").
                queryParam("permissionMask", MASK).
                request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        verify(mockSecurityAclService, times(1)).hasPermission(eq(drugProgram), eq(MASK), eq(USER.toSids()));
        verifyNoMoreInteractions(mockSecurityAclService, mockSecurity);
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void shouldReturnUnauthorisedWithStatus401For_hasPermission_WithSecurityOff() {

        int MASK = 32;
        when(mockSecurity.getAcuityUserDetails()).thenThrow(new IllegalStateException());

        Response response = target("my/acl/DrugPrograme/2").
                queryParam("permissionMask", MASK).
                request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        verifyNoMoreInteractions(mockSecurityAclService, mockSecurity);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void shouldReturnUnauthorisedWithStatus401For_hasPermission_UserNoPermission() {

        AcuitySidDetails USER = new AcuitySidDetails("user001");
        int MASK = 32;
        ClinicalStudy clinicalStudy = new ClinicalStudy(4L, "ClinicalStudy A");
        when(mockSecurity.getAcuityUserDetails()).thenReturn(USER);
        when(mockSecurityAclService.hasPermission(eq(clinicalStudy), eq(MASK), eq(USER.toSids()))).thenReturn(false);

        Response response = target("my/acl/ClinicalStudy/4").
                queryParam("permissionMask", MASK).
                request().get(Response.class);

        verify(mockSecurity, times(1)).getAcuityUserDetails();
        verify(mockSecurityAclService, times(1)).hasPermission(eq(clinicalStudy), eq(MASK), eq(USER.toSids()));
        verifyNoMoreInteractions(mockSecurityAclService, mockSecurity);
        assertThat(response.getStatus()).isEqualTo(401);
    }
    
    @Test
    public void shouldReturnStatus200For_clearCache() {

        Cache mockCache  = Mockito.mock(Cache.class);
        CacheManager mockCacheManager  = Mockito.mock(CacheManager.class);
        when(mockRefreshCachesTask.getCacheManager()).thenReturn(mockCacheManager);
        when(mockCacheManager.getCache(any())).thenReturn(mockCache);

        Response response = target("my/clearcache/cacheName").request().get(Response.class);

        verify(mockRefreshCachesTask, times(1)).getCacheManager();
        verifyNoMoreInteractions(mockRefreshCachesTask);
        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    public void shouldReturnStatus200For_clearCachesandFilters() {

        Response response = target("my/refresh").request().post(null);

        verify(mockRefreshCachesTask, times(1)).runHourly();
        verify(mockRefreshCachesTask, times(1)).runNightly();
     
        verifyNoMoreInteractions(mockRefreshCachesTask);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
