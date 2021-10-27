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

package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.annotation.TransactionalMyBatisDBUnitH2Test;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.GroupWithLockdown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.common.Constants;
import com.acuity.va.security.config.annotation.FlatXmlNullDataSetLoader;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.acuity.va.security.acl.domain.AcuitySidDetails.toUserFromGroup;
import static com.acuity.va.security.acl.permissions.AcuityPermissions.VIEW_VISUALISATIONS;
import static com.acuity.va.security.acl.service.UserService.DEFAULT_ROLE;
import static com.acuity.va.security.common.Constants.ACL_ADMINISTRATOR_AUTHORITY;
import static com.acuity.va.security.common.Constants.ACL_ADMINISTRATOR_GROUP;
import static com.acuity.va.security.common.Constants.DEVELOPMENT_GROUP;
import static com.acuity.va.security.common.Constants.ACUITY_SUPPORT_GROUP;
import static com.acuity.va.security.common.Constants.TRAINED_USER_AUTHORITY;
import static com.acuity.va.security.common.Constants.TRAINED_USER_GROUP;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionalMyBatisDBUnitH2Test
@DatabaseSetup("/dbunit/security/dbunit-all-security.xml")
@DbUnitConfiguration(dataSetLoader = FlatXmlNullDataSetLoader.class)
public class WhenUsingUserService {

    @Autowired
    UserService userService;
    @Autowired
    SecurityAclService securityAclService;

    @Autowired
    private CustomUserDetailsManager customUserDetailsManager;

    @Test
    public void shouldListAllUsersNotInTrainedUserGroup() {

        List<AcuitySidDetails> noneTrainedUsers = userService.getAcuityUsersNotInGroup(TRAINED_USER_GROUP);

        assertThat(noneTrainedUsers).hasSize(6);
        assertThat(noneTrainedUsers).extracting("userId").contains("ksdf5465667", "User1", "User2", "User3", "User4", "User5");
        assertThat(noneTrainedUsers).extracting("linkeduser.userId").isEqualTo(newArrayList(null, null, null, null, null, null));
    }

    @Test
    public void shouldListAllUsersByTrainedUserGroup() {

        List<AcuitySidDetails> trainedUsers = userService.getAcuityUsersByGroup(TRAINED_USER_GROUP);

        assertThat(trainedUsers).hasSize(3);
        assertThat(trainedUsers).extracting("userId").contains("kfgt12354", "user001", "User6@securepass.mail.com");
        assertThat(trainedUsers).extracting("linkeduser.userId").contains("user001");
    }

    @Test
    public void shouldListAllUsersByUserGroup() {

        List<AcuitySidDetails> trainedUsers = userService.getAcuityUsersByGroup("Users");

        assertThat(trainedUsers).hasSize(1);
        assertThat(trainedUsers).extracting("userId").contains("kfgt12354");
    }

    @Test
    public void shouldAddUserWithCorrectAuthorities() {

        String USERID = "test";
        String FULLNAME = "test full name";

        userService.createUser(USERID, FULLNAME, newArrayList("TEST"));

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly("TEST", DEFAULT_ROLE, ACL_ADMINISTRATOR_AUTHORITY);
    }

    @Test
    public void shouldAddUserWithNoAuthorities() {

        String USERID = "test";
        String FULLNAME = "test full name";

        userService.createUser(USERID, FULLNAME);

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly(DEFAULT_ROLE, ACL_ADMINISTRATOR_AUTHORITY);
    }

    @Test
    public void shouldAddUnknownUserWithNoDefaultAuthorities() {

        String USERID = "kvmd129";

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly(DEFAULT_ROLE, ACL_ADMINISTRATOR_AUTHORITY);
    }

    @Test
    public void shouldSwapExternalUserWithNormalUser() {

        String EXTERNAL_USERID = "User6@securepass.mail.com";
        String LINKED_USERID = "user001";

        AcuitySidDetails user = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(EXTERNAL_USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isExternal()).isFalse();
        assertThat(user.getUserId()).isEqualTo(LINKED_USERID);
    }

    @Test
    public void shouldAddExternalUserWithFullnameAsUserWithDefaultAuthorities() {

        String USERID = "glen@securepass.mail.com";
        AcuitySidDetails user = (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isExternal()).isTrue();
        assertThat(user.getFullName()).isEqualTo(user.getUserId());
        assertThat(user.getAuthorities()).extracting("authority").containsOnly(DEFAULT_ROLE, ACL_ADMINISTRATOR_AUTHORITY);
    }

    @Test
    public void shouldListUsersForGroup() {

        List<String> users = userService.getUsersByGroup("Administrators");

        assertThat(users).hasSize(1);
        assertThat(users).containsExactly("user001");
    }

    @Test
    public void shouldGetIsUserInGroup() {

        boolean isInGroup = userService.isUserInGroup("user001", "Administrators");

        assertThat(isInGroup).isTrue();
    }

    @Test
    public void shouldNotGetIsUserInGroup() {

        boolean isInGroup = userService.isUserInGroup("user001", "DEVELOPMENT_GROUP");

        assertThat(isInGroup).isFalse();
    }

    @Test
    public void shouldDeleteUser() {

        userService.deleteUser("kfgt12354");

        assertThat(customUserDetailsManager.userExists("kfgt12354")).isFalse();
    }

    @Test
    public void shouldAddUserToGroup() {

        userService.createUser("glenbob", "glen bob");
        userService.addUserToGroup("glenbob", "Users");

        assertThat(customUserDetailsManager.findUsersInGroup("Users")).hasSize(3);
        boolean added2Admin = userService.isUserInGroup("glenbob", Constants.ACL_ADMINISTRATOR_GROUP);
        assertThat(added2Admin).isTrue();

        userService.removeUserFromGroup("glenbob", "Users");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForAddingUserToUnknownGroup() {

        userService.addUserToGroup("ksdf5465667", "UnknownGroup");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForAddingUnknownUserToGroup() {

        userService.addUserToGroup("ksdf5465667ddd", "Users");
    }

    @Test
    public void shouldRemoveUserFromGroup() {

        userService.removeUserFromGroup("kfgt12354", "Users");

        assertThat(customUserDetailsManager.findUsersInGroup("Users")).hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForRemovingUserToUnknownGroup() {

        userService.removeUserFromGroup("kfgt12354", "UnknownGroup");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForRemovingUnKnownUserToGroup() {

        userService.removeUserFromGroup("kfgt12354ddd", "Users");
    }

    @Test
    public void shouldRemoveUserFromAllGroups() {
        String USERID = "kdnp321";
        userService.createUser(USERID, "Test User");
        userService.addUserToGroup(USERID, TRAINED_USER_GROUP);
        userService.addUserToGroup(USERID, DEVELOPMENT_GROUP);
        userService.addUserToGroup(USERID, ACL_ADMINISTRATOR_GROUP);

        assertThat(userService.isUserInGroup(USERID, TRAINED_USER_GROUP)).isTrue();
        assertThat(userService.isUserInGroup(USERID, DEVELOPMENT_GROUP)).isTrue();
        assertThat(userService.isUserInGroup(USERID, ACL_ADMINISTRATOR_GROUP)).isTrue();
        userService.removeUserFromAllGroups(USERID);
        assertThat(userService.userExists(USERID)).isEqualTo(true);
        assertThat(userService.getUser(USERID).getAuthorities()).extracting("authority").containsOnly(DEFAULT_ROLE);
    }

    @Test
    public void shouldGetUser() {

        String USERID = "kfgt12354";

        AcuitySidDetails user = userService.getUser(USERID);

        assertThat(user.getUserId()).isEqualTo(USERID);
        assertThat(user.getFullName()).isEqualTo("Glen D");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly("ROLE_USER", "TRAINED_USER");
    }

    @Test
    public void shouldGetUserWithoutSwapping() {

        String USERID = "User6@securepass.mail.com";

        AcuitySidDetails user = userService.getUserWithOutSwappingLinkedUser(USERID);

        assertThat(user.getUserId()).isEqualTo(USERID);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").containsOnly("TRAINED_USER");

        AcuitySidDetails userSwapped = userService.getUser(USERID);

        assertThat(userSwapped.getUserId()).isEqualTo("user001");
    }

    @Test
    public void shouldGetLinkedUser() {

        String EXTERNAL_USERID = "User6@securepass.mail.com";
        String LINKED_USERID = "user001";

        AcuitySidDetails user = userService.getUser(EXTERNAL_USERID);

        assertThat(user.getUserId()).isEqualTo(LINKED_USERID);
    }

    @Test
    public void shouldCheckIfUserExists() {

        String USERID = "kfgt12354";

        boolean userExists = userService.userExists(USERID);

        assertThat(userExists).isTrue();

        boolean userDoesntExist = userService.userExists("silly user");

        assertThat(userDoesntExist).isFalse();
    }

    @Test
    public void shouldLinkAUser() {

        String USERID = "ksdf5465667";
        String LINKTO_USERID = "kfgt12354";

        userService.linkUser(USERID, LINKTO_USERID);
        AcuitySidDetails acuityUserDetails = userService.getUser(USERID);

        assertThat(acuityUserDetails.getLinkeduser().getName()).isEqualTo(LINKTO_USERID);

        userService.unlinkUser(USERID);

        AcuitySidDetails acuityUserDetails2 = userService.getUser(USERID);
        assertThat(acuityUserDetails2.getLinkeduser()).isNull();
    }

    @Test(expected = DuplicateKeyException.class)
    public void shouldntAllow2LinkedUsers() {

        String USERID = "ksdf5465667";
        String LINKTO_USERID = "user001";

        try {
            userService.linkUser(USERID, LINKTO_USERID);
        } catch (Exception ex) {
            System.out.println(ex);
            throw ex;
        }
    }

    @Ignore
    @Test
    public void shouldCreateUserIfNotExistAndAddToGroup_ForNoneExistantUser_CreateAndAddToGroup() {

        String USERID = "kxnb675";
        userService.createUserIfNotExistAndAddToGroup(USERID, TRAINED_USER_GROUP);

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").contains(DEFAULT_ROLE, ACL_ADMINISTRATOR_AUTHORITY, TRAINED_USER_AUTHORITY);
    }

    @Test
    public void shouldCreateUserIfNotExistAndAddToGroup_ForUserNotInGroup_AddToGroupOnly() {

        String USERID = "ksdf5465667";
        userService.createUserIfNotExistAndAddToGroup(USERID, TRAINED_USER_GROUP);

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").contains(TRAINED_USER_AUTHORITY);

        userService.removeUserFromGroup(USERID, TRAINED_USER_GROUP);
    }

    @Test
    public void shouldCreateUserIfNotExistAndAddToGroup_ForNoneExistantUser_DoNothing() {

        String USERID = "user001";
        userService.createUserIfNotExistAndAddToGroup(USERID, TRAINED_USER_GROUP);

        UserDetails user = customUserDetailsManager.loadUserByUsername(USERID);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting("authority").contains(TRAINED_USER_AUTHORITY);
    }

    @Test
    public void shouldCheckIfValidUser() {

        String USERID_IN_DB = "user001";
        String INVALID_USERID = "dfgdfgdfg";

        boolean validUserInDb = userService.isValidUser(USERID_IN_DB);
        boolean invalidUser = userService.isValidUser(INVALID_USERID);

        assertThat(validUserInDb).isTrue();
        assertThat(invalidUser).isFalse();
    }

    @Ignore
    @Test
    public void shouldCheckIfValidUserAndNotInDB() {
        String USERID_NOT_IN_DB = "kfbg570";
        boolean validUserNotInDb = userService.isValidUser(USERID_NOT_IN_DB);
        assertThat(validUserNotInDb).isTrue();
    }

    @Test
    public void shouldListAllGroups() {

        List<String> groups = userService.listGroups();

        assertThat(groups).hasSize(3);
        assertThat(groups).containsOnly("Users", "Administrators", "Group1");
        assertThat(groups).doesNotContain(ACL_ADMINISTRATOR_GROUP, DEVELOPMENT_GROUP, TRAINED_USER_GROUP);
    }

    @Test
    public void shouldListGroupsWithLockdown() {

        DetectDataset detectDataset = new DetectDataset(50L, "Detect_Lockdown2");

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        securityAclService.setLockdownStatus(detectDataset, true);
        customUserDetailsManager.createGroup("TEST", newArrayList());
        securityAclService.addAce(detectDataset, VIEW_VISUALISATIONS.getMask(), toUserFromGroup("TEST").toSid(), true);

        List<GroupWithLockdown> groupsWithLockdowns = userService.listGroupsWithLockdown();

        assertThat(groupsWithLockdowns).hasSize(4);
        assertThat(groupsWithLockdowns).extracting("groupName").containsSequence("Users", "Administrators", "Group1", "TEST");
        assertThat(groupsWithLockdowns).extracting("isInLockdownDataset").containsSequence(false, false, false, true);
    }

    @Test
    public void shouldListGroupsWithLockdownAndDatasets() {

        DetectDataset detectDataset = new DetectDataset(50L, "Detect_Lockdown");

        SecurityContextHolder.getContext().setAuthentication(new AuthenicationToken("ACUITY_SERVER_USER"));

        securityAclService.setLockdownStatus(detectDataset, true);
        customUserDetailsManager.createGroup("TEST", newArrayList());
        securityAclService.addAce(detectDataset, VIEW_VISUALISATIONS.getMask(), toUserFromGroup("TEST").toSid(), true);

        List<GroupWithItsLockdownDatasets> groupsWithLockdownAndDatasets = userService.listGroupsWithLockdownAndDatasets();

        assertThat(groupsWithLockdownAndDatasets).hasSize(4);
        assertThat(groupsWithLockdownAndDatasets).extracting("groupName").containsSequence("Users", "Administrators", "Group1", "TEST");
        assertThat(groupsWithLockdownAndDatasets).extracting("inLockdownDataset").containsSequence(false, false, false, true);
        assertThat(groupsWithLockdownAndDatasets.get(3).getDatasetsInLockdown()).containsOnly("Detect_Lockdown");
    }

    @Test
    public void shouldCreateGroup() {

        String newGroup = "new";
        userService.createGroup(newGroup);

        assertThat(userService.groupExists(newGroup)).isTrue();

        List<String> groups = userService.listGroups();
        assertThat(groups).contains(newGroup);
    }

    @Test
    public void shouldDeleteGroup() {

        String newGroup = "new";
        userService.createGroup(newGroup);
        assertThat(userService.groupExists(newGroup)).isTrue();

        userService.removeGroup(newGroup);
        assertThat(userService.groupExists(newGroup)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldntDeleteDevGroup() {
        userService.removeGroup(DEVELOPMENT_GROUP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldntDeleteTrainingGroup() {
        userService.removeGroup(TRAINED_USER_GROUP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldntDeleteAclGroup() {
        userService.removeGroup(ACL_ADMINISTRATOR_GROUP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldntDeleteSupportGroup() {
        userService.removeGroup(ACUITY_SUPPORT_GROUP);
    }
}
