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

import com.acuity.va.security.acl.dao.AclRepository;
import com.acuity.va.security.acl.dao.UserRepository;
import com.acuity.va.security.acl.domain.GroupAuthorityConverter;
import com.acuity.va.security.acl.domain.GroupWithItsLockdownDatasets;
import com.acuity.va.security.acl.domain.GroupWithLockdown;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.UsernameFullNameAndLinkedAccount;
import com.acuity.va.security.common.Constants;
import com.acuity.va.security.common.service.PeopleResourceClient;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.acuity.va.security.common.Constants.TRAINED_USER_GROUP;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

/**
 * UserService implements all the methods needed from the UserRepository that needs extra processing or methods needing JdbcUserDetailsManager interaction.
 * <p>
 * Any methods needed for interacting with the GBAC schema is put here.
 * <p>
 * No point just making a dumb service that all it does it proxy to UserRepository.
 *
 * @author Glen
 */
@Service
@Transactional(value = "security")
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    /**
     * Role/authority added to very user, as you cannot have user without roles in spring
     */
    public static final String DEFAULT_ROLE = "DEFAULT_ROLE";
    @Autowired
    private CustomUserDetailsManager customUserDetailsManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PeopleResourceClient peopleResourceClient;
    @Autowired
    private AclRepository aclRepository;

    /**
     * Lists all the User for a particular group
     */
    @Transactional(value = "security", readOnly = true)
    public List<String> getUsersByGroup(String group) {
        return customUserDetailsManager.findUsersInGroup(group);
    }

    /**
     * Lists all the users who are a part of a group
     *
     * @param groupName for the users not in
     * @return list of AcuityUserDetails who dont belong to group groupName
     */
    @Transactional(value = "security", readOnly = true)
    public List<AcuitySidDetails> getAcuityUsersNotInGroup(String groupName) {
        List<String> userPrids = customUserDetailsManager.findUsersNotInGroup(groupName);

        return generateAcuityUserDetails(newHashSet(userPrids));
    }

    /**
     * Lists all the users who are a part of a group
     *
     * @param groupName for the users
     * @return list of AcuityUserDetails who belong to group groupName
     */
    @Transactional(value = "security", readOnly = true)
    public List<AcuitySidDetails> getAcuityUsersByGroup(String groupName) {

        List<String> userPrids = customUserDetailsManager.findUsersInGroup(groupName);

        return generateAcuityUserDetails(newHashSet(userPrids));
    }

    /**
     * Takes a list of userPrids and turns them into AcuityUserDetails objects, populating fullnames and adding links to linked users if they are linked
     *
     * @param userPrids list of usernames from the db
     * @return List of AcuityUserDetails
     */
    private List<AcuitySidDetails> generateAcuityUserDetails(Set<String> userPrids) {

        List<UsernameFullNameAndLinkedAccount> allUsernameFullNameAndLinkedAccount = userRepository.getAllUsernameFullNameAndLinkedAccount();

        List<AcuitySidDetails> acuityUserDetailsList = newArrayList();

        for (String userPrid : userPrids) {

            AcuitySidDetails linkedAccount = null;

            Optional<UsernameFullNameAndLinkedAccount> optionalUfnala = allUsernameFullNameAndLinkedAccount.stream().
                    filter(prid -> prid.getUsername().equals(userPrid)).findFirst();

            if (optionalUfnala.isPresent()) {

                AcuitySidDetails newUser = null;
                UsernameFullNameAndLinkedAccount ufnala = optionalUfnala.get();
                if (ufnala.hasLinkedAccount()) {
                    String linkedFullName = userRepository.getFullNameForUser(ufnala.getLinkedUsername());
                    linkedAccount = new AcuitySidDetails(ufnala.getLinkedUsername(), linkedFullName);
                }
                if (linkedAccount == null) {
                    newUser = new AcuitySidDetails(userPrid, ufnala.getFullname());
                } else {
                    newUser = new AcuitySidDetails(userPrid, ufnala.getFullname(), linkedAccount);
                }

                acuityUserDetailsList.add(newUser);
            }
        }

        return acuityUserDetailsList;
    }

    /**
     * Adds a user with a list of roles/authorities
     * <p>
     * Note default authority added. Authority DEFAULT_ROLE Note default group added. Authority ACL_ADMINISTRATOR_GROUP
     *
     * @param userId user prid
     * @param fullName of the user
     */
    public void createUser(String userId, String fullName, List<String> roles) {

        List<GrantedAuthority> authrorities = roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
        customUserDetailsManager.createUserWithDefaultGroups(userId, fullName, authrorities);

        userRepository.clearGetAllUsernameFullNameAndLinkedAccount();
    }

    /**
     * Creates a new user if one doesnt exist, and then once its in the db, checks if its in the group
     *
     * @param userId user prid
     * @param group group to add new user to
     */
    public void createUserIfNotExistAndAddToGroup(String userId, String group) {

        // make sure all users are trained users
        boolean exists = userExists(userId);
        if (!exists) {
            LOG.debug("Getting users fullname from web service for {} and creating the user.", userId);
            String fullName = peopleResourceClient.getFullName(userId);

            createUser(userId, fullName);
            addUserToGroup(userId, group);
        } else if (!isUserInGroup(userId, TRAINED_USER_GROUP)) {
            LOG.debug("Users {} already exists but not in trained user group, so adding", userId);
            addUserToGroup(userId, TRAINED_USER_GROUP);
        }
    }

    /**
     * Check if they prid is a valid user, if its either in the db (user already, including external), or if it isnt, checks people db
     *
     * @param userId user prid
     */
    public boolean isValidUser(String userId) {

        boolean exists = userExists(userId);
        if (!exists) {
            try {
                peopleResourceClient.getFullName(userId);
                return true;
            } catch (Exception ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Adds a user with no roles/authorities.
     * <p>
     * Note default authority added. Authority DEFAULT_ROLE
     *
     * @param userId user prid
     * @param fullName of the user
     */
    public void createUser(String userId, String fullName) {
        createUser(userId, fullName, Lists.newArrayList());
    }

    /**
     * Deletes a user and TODO removes all acl entries
     * <p>
     * Note: Should throw exception if user is own of an acl
     */
    public void deleteUser(String userId) {
        customUserDetailsManager.deleteUser(userId);

        userRepository.clearGetAllUsernameFullNameAndLinkedAccount();
        //TODO need to remove all acl entries
    }

    /**
     * Links the the user to the linked user id
     *
     * @param userId normally external/exostar id
     * @param linkToUserId to internal prid
     */
    public void linkUser(String userId, String linkedUserId) {
        checkUserExists(linkedUserId);
        checkUserExists(userId);

        customUserDetailsManager.linkUser(userId, linkedUserId);

        userRepository.clearGetAllUsernameFullNameAndLinkedAccount();
    }

    /**
     * UnLinks the the user to the linked user id
     *
     * @param userId normally external/exostar id
     */
    public void unlinkUser(String userId) {
        checkUserExists(userId);

        customUserDetailsManager.linkUser(userId, null);

        userRepository.clearGetAllUsernameFullNameAndLinkedAccount();
    }

    /**
     * Adds a user to a group
     */
    public void addUserToGroup(String userId, String group) {
        checkGroupExists(group);
        checkUserExists(userId);

        if (!isUserInGroup(userId, Constants.ACL_ADMINISTRATOR_GROUP)) {
            // always add to this group if adding
            customUserDetailsManager.addUserToGroup(userId, Constants.ACL_ADMINISTRATOR_GROUP);
        }
        customUserDetailsManager.addUserToGroup(userId, group);
    }

    /**
     * Removes a user from a group
     */
    public void removeUserFromGroup(String userId, String group) {
        checkGroupExists(group);
        checkUserExists(userId);

        customUserDetailsManager.removeUserFromGroup(userId, group);
    }

    /**
     * Updates the acuity user details
     */
    public void updateAcuityUser(AcuitySidDetails acuityUserDetails) {
        customUserDetailsManager.updateAcuityUser(acuityUserDetails);
    }

    /**
     * Check if users is in a group
     */
    public boolean isUserInGroup(String userId, String group) {
        checkGroupExists(group);
        checkUserExists(userId);

        return customUserDetailsManager.findUsersInGroup(group).contains(userId);
    }

    /**
     * Checks if the user exits, if not throws an IllegalArgumentException
     *
     * @param userId prid
     */
    private void checkUserExists(String userId) {
        if (!userExists(userId)) {
            throw new IllegalArgumentException(userId + " not found");
        }
    }

    /**
     * Checks if the group exists, if not throws an IllegalArgumentException
     *
     * @param group group name
     */
    private void checkGroupExists(String group) {
        if (!customUserDetailsManager.findAllGroups().contains(group)) {
            throw new IllegalArgumentException(group + " not found");
        }
    }

    /**
     * Get user with their authorities/roles
     */
    @Transactional(value = "security", readOnly = true)
    public AcuitySidDetails getUser(String userId) {
        return (AcuitySidDetails) customUserDetailsManager.loadUserByUsername(userId);
    }

    /**
     * Get user with their authorities/roles without linking the user
     */
    @Transactional(value = "security", readOnly = true)
    public AcuitySidDetails getUserWithOutSwappingLinkedUser(String userId) {
        return (AcuitySidDetails) customUserDetailsManager.loadUserByUsernameForTest(userId);
    }

    /**
     * Checks if user exists
     */
    @Transactional(value = "security", readOnly = true)
    public boolean userExists(String userId) {
        return customUserDetailsManager.userExists(userId);
    }

    /**
     * Checks if group exists
     */
    @Transactional(value = "security", readOnly = true)
    public boolean groupExists(String group) {
        return customUserDetailsManager.findAllGroups().contains(group);
    }

    /**
     * Checks if group exists
     */
    @Transactional(value = "security", readOnly = true)
    public List<String> listGroups() {
        return customUserDetailsManager.findAllGroups().stream().filter(g -> {
            return (!g.equals(Constants.DEVELOPMENT_GROUP) && !g.equals(Constants.TRAINED_USER_GROUP) && !g.equals(Constants.ACL_ADMINISTRATOR_GROUP));
        }).collect(toList());
    }

    @Transactional(value = "security", readOnly = true)
    public List<GroupWithLockdown> listGroupsWithLockdown() {
        List<String> listGroupsInLockdown = aclRepository.listGroupsInLockdown();

        return listGroups().stream().map(g -> {
            GroupWithLockdown gl = new GroupWithLockdown();
            gl.setGroupName(g);

            boolean inLockdown = listGroupsInLockdown.contains(g);
            gl.setInLockdownDataset(inLockdown);

            return gl;
        }).collect(toList());
    }

    @Transactional(value = "security", readOnly = true)
    public List<GroupWithItsLockdownDatasets> listGroupsWithLockdownAndDatasets() {
        List<GroupWithItsLockdownDatasets> listGroupWithItsLockdownDatasets = aclRepository.listGroupWithItsLockdownDatasets();

        return listGroups().stream().map(g -> {
            GroupWithItsLockdownDatasets gl = new GroupWithItsLockdownDatasets();
            gl.setGroupName(g);

            Optional<GroupWithItsLockdownDatasets> foundGw = listGroupWithItsLockdownDatasets.stream().filter(gw -> gw.getGroupName().equals(g)).findFirst();
            if (foundGw.isPresent()) {
                gl.setDatasetsInLockdown(foundGw.get().getDatasetsInLockdown());
            }

            return gl;
        }).collect(toList());
    }

    /**
     * Create a group
     */
    public void createGroup(String groupName) {
        customUserDetailsManager.createGroup(groupName,
                newArrayList(new SimpleGrantedAuthority(groupName + GroupAuthorityConverter.AUTHORITY)));
    }

    /**
     * Removes a group
     */
    public void removeGroup(String groupName) {
        Validate.isTrue(!groupName.equalsIgnoreCase(Constants.DEVELOPMENT_GROUP));
        Validate.isTrue(!groupName.equalsIgnoreCase(Constants.ACUITY_SUPPORT_GROUP));
        Validate.isTrue(!groupName.equalsIgnoreCase(Constants.TRAINED_USER_GROUP));
        Validate.isTrue(!groupName.equalsIgnoreCase(Constants.ACL_ADMINISTRATOR_GROUP));

        customUserDetailsManager.deleteGroup(groupName);
    }

    /**
     * Removes a user from all groups
     */
    public void removeUserFromAllGroups(String userId) {
        checkUserExists(userId);
        customUserDetailsManager.findAllGroups().forEach(g ->  customUserDetailsManager.removeUserFromGroup(userId, g));
    }
}
