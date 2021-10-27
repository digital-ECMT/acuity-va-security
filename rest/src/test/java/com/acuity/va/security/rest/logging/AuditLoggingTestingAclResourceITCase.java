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

package com.acuity.va.security.rest.logging;

import com.acuity.va.auditlogger.dao.AuditLoggerRepository;
import com.acuity.va.auditlogger.domain.LogArgEntity;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.service.SecurityAclService;
import com.acuity.va.security.config.annotation.FlatXmlNullColumnSensingDataSetLoader;
import com.acuity.va.security.rest.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.rest.resources.AclResource;
import com.acuity.va.security.rest.security.TestingAuthenication;
import com.acuity.va.security.rest.util.UserPermission;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.DEVELOPMENT_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup({"/dbunit/security/dbunit-all-security.xml"})
@DbUnitConfiguration(dataSetLoader = FlatXmlNullColumnSensingDataSetLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuditLoggingTestingAclResourceITCase {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLoggingTestingAclResourceITCase.class);

    @Autowired
    private AclResource aclResource;
    @Autowired
    private SecurityAclService securityAclService;

    @Autowired
    private AuditLoggerRepository auditLoggerRepository;

    private MutableAcl acl;
    private DrugProgramme newDrugProgramme;

    AcuitySidDetails admin = AcuitySidDetails.toUser("glen");
    TestingAuthenication testingAuthenication = new TestingAuthenication("glen", Lists.newArrayList("ACL_ADMINISTRATOR", "DEVELOPMENT_TEAM"));

    @Before()
    public void add() {
        SecurityContextHolder.getContext().setAuthentication(testingAuthenication);

        if (acl == null) {
            newDrugProgramme = new DrugProgramme(2L);
            securityAclService.addAce(newDrugProgramme, DEVELOPMENT_TEAM.getMask(), admin.toSid(), true);
        }
    }

    @After()
    public void remove() {
        if (acl != null) {
            securityAclService.removeAce(newDrugProgramme, DEVELOPMENT_TEAM.getMask(), admin.toSid());
        }
    }

    @Test
    public void shouldLog_AddPermissionForAcl_DrugProgramme() {

        // Given
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());
        userPermission.setGranting(true);

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        List<LogOperationEntity> logOperationEntitiesBefore = auditLoggerRepository.getAllLogOperations();
        List<LogArgEntity> loggedEntityBefore = auditLoggerRepository.getAllLogArgs();

        // When
        aclResource.addPermissionForASid(mockUriInfo, DrugProgramme.class.getSimpleName(), 2L, true, userPermission);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(1);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(5);

       List<String> logOperationEntityNames = logOperationEntitiesBefore.stream().map(LogOperationEntity::getName).collect(Collectors.toList());
       logOperationEntityNames.add("PERMISSIONS_ADD_ACE");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("name").contains(logOperationEntityNames.toArray());

        List<String> logOperationEntityClassNames = logOperationEntitiesBefore.stream().map(LogOperationEntity::getObjectIdentityClassname).collect(Collectors.toList());
        logOperationEntityClassNames.add(DrugProgramme.class.getSimpleName());
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityClassname").contains(logOperationEntityClassNames.toArray());

        List<Long> logOperationEntityIds = logOperationEntitiesBefore.stream().map(LogOperationEntity::getObjectIdentityId).collect(Collectors.toList());
        logOperationEntityIds.add(2L);
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityId").contains(logOperationEntityIds.toArray());

        List<String> loggedEntityName = loggedEntityBefore.stream().map(LogArgEntity::getName).collect(Collectors.toList());
        loggedEntityName.addAll((Arrays.asList("ACE_USER", "ACE_PERMISSION", "ACE_ISGROUP", "OVERWRITE_ALL_FOR_USER", "ACE_GRANTING")));
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("name").
                containsOnly(loggedEntityName.toArray());

        List<Object> loggedEntityValues = loggedEntityBefore.stream().map(LogArgEntity::getValue).collect(Collectors.toList());
        loggedEntityValues.addAll(Arrays.asList(userPermission.getAcuitySidDetails().getSidAsString(), "false", 3L, 1L, 1L));
        assertThat(auditLoggerRepository.getAllLogArgs()).extracting("value").
                containsOnly(loggedEntityValues.toArray());
    }

    @Test
    @Rollback
    public void shouldLog_RemovePermissionForAcl_DrugProgramme() {

        // Given
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());
        userPermission.setGranting(true);

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        // When
        aclResource.addPermissionForASid(mockUriInfo, DrugProgramme.class.getSimpleName(), 2L, true, userPermission);
        aclResource.removePermissionForASid(DrugProgramme.class.getSimpleName(), 2L, userPermission);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(2);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(8);
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("name").contains("PERMISSIONS_REMOVE_ACE");
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("owner").contains("Glen");
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityClassname").contains(DrugProgramme.class.getSimpleName());
        assertThat(auditLoggerRepository.getAllLogOperations()).extracting("objectIdentityId").contains(2L);

        assertThat(auditLoggerRepository.getAllLogArgs().subList(5, 8)).extracting("name").
                containsOnly("ACE_USER", "ACE_ISGROUP", "ACE_PERMISSION");
        assertThat(auditLoggerRepository.getAllLogArgs().subList(5, 8)).extracting("value").
                containsOnly(userPermission.getAcuitySidDetails().getSidAsString(), "false", 3L);
    }

    @Ignore
    @Test
    public void shouldLog_RemovePermissionForAclWithNull_DrugProgramme() {

        // Given
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());
        userPermission.setGranting(true);

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        // When
        aclResource.addPermissionForASid(mockUriInfo, DrugProgramme.class.getSimpleName(), 2L, true, userPermission);
        aclResource.removePermissionForASid(DrugProgramme.class.getSimpleName(), 2L, userPermission);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(2);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(8);
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("name").contains("PERMISSIONS_REMOVE_ACE");
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("owner").contains("Glen");

        assertThat(auditLoggerRepository.getAllLogArgs().subList(0, 2)).extracting("name").
                containsOnly("ACE_USER", "ACE_PERMISSION");
        assertThat(auditLoggerRepository.getAllLogArgs().subList(0, 2)).extracting("value").
                containsOnly(userPermission.getAcuitySidDetails().getSidAsString(), 3L);
        assertThat(auditLoggerRepository.getAllLogArgs().subList(5, 6)).extracting("value").
                containsOnly(userPermission.getAcuitySidDetails().getSidAsString());
    }

    @Test
    @Rollback
    public void shouldLog_RemovePermissionForAclPerisitingNull_DrugProgramme() {

        // Given
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());
        userPermission.setGranting(true);

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        // When
        aclResource.addPermissionForASid(mockUriInfo, DrugProgramme.class.getSimpleName(), 2L, true, userPermission);
        aclResource.removePermissionForASid(DrugProgramme.class.getSimpleName(), 2L, userPermission);

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(2);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(8);
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("name").contains("PERMISSIONS_REMOVE_ACE");
        assertThat(auditLoggerRepository.getAllLogOperations().subList(1, 2)).extracting("owner").contains("Glen");

        assertThat(auditLoggerRepository.getAllLogArgs().subList(5, 8)).extracting("name").
                containsOnly("ACE_USER", "ACE_ISGROUP", "ACE_PERMISSION");
        assertThat(auditLoggerRepository.getAllLogArgs().subList(5, 8)).extracting("value").
                containsOnly(userPermission.getAcuitySidDetails().getSidAsString(), "false", 3L);
    }

    @Test
    @Rollback
    public void shouldnotLog_RemovePermissionForAcl_DrugProgramme() {

        // Given invalid user
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User2", Lists.newArrayList("ACL_ADMINISTRATOR", "DEVELOPMENT_TEAM")));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        // When
        try {
            aclResource.removePermissionForASid(DrugProgramme.class.getSimpleName(), 2L, userPermission);

        } catch (Exception ex) {
        }

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(0);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(0);
    }

    @Test
    @Rollback
    public void shouldnotLog_AddPermissionForAcl_DrugProgramme() {

        // Given invalid user
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenication("User2", Lists.newArrayList("ACL_ADMINISTRATOR", "DEVELOPMENT_TEAM")));

        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/"));

        UserPermission userPermission = new UserPermission();
        userPermission.setAcuitySidDetails(AcuitySidDetails.toUser("User3"));
        userPermission.setPermissionMask(AUTHORISED_USER.getMask());

        int countAllLogOperationsBefore = auditLoggerRepository.countAllLogOperations();
        int countAllLogArgsBefore = auditLoggerRepository.countAllLogArgs();

        // When
        try {
            aclResource.addPermissionForASid(mockUriInfo, DrugProgramme.class.getSimpleName(), 2L, true, userPermission);
        } catch (Exception ex) {
        }

        // Then
        assertThat(auditLoggerRepository.countAllLogOperations() - countAllLogOperationsBefore).isEqualTo(0);
        assertThat(auditLoggerRepository.countAllLogArgs() - countAllLogArgsBefore).isEqualTo(0);
    }
}
