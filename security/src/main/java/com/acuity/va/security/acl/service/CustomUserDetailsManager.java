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

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.common.Constants;
import com.acuity.va.security.common.service.PeopleResourceClient;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.acuity.va.security.acl.service.UserService.DEFAULT_ROLE;

/**
 * Extend the JdbcUserDetailsManager for new or overridding methods on the User schema
 *
 * @author Glen
 */
public class CustomUserDetailsManager extends JdbcUserDetailsManager {

    private static final Logger LOG = LoggerFactory.getLogger(CustomUserDetailsManager.class);

    public static final String DEF_CUSTOM_USERS_BY_USERNAME_QUERY
            = "select username, fullName, enabled "
            + "from users "
            + "where username = ?";
    public static final String DEF_CUSTOM_USERS_BY_LINKED_USERNAME_QUERY
            = "select linked_username "
            + "from users "
            + "where username = ?";
    public static final String DEF_CUSTOM_CREATE_USER_SQL
            = "insert into users (username, fullName, password, enabled) values (?,?,?,?)";
    public static final String DEF_CUSTOM_USERS_NOT_IN_GROUP_SQL
            = "select username from users "
            + " where "
            + " username not in ( select distinct username from group_members gm, groups g where gm.group_id = g.id and g.group_name = ?)";
    public static final String DEF_UPDATE_USER_SQL
            = "update users set password = ?, fullName = ?, enabled = ? where username = ?";
    public static final String DEF_UPDATE_LINK_USER_SQL
            = "update users set linked_username = ? where username = ?";

    private final String deleteUserAuthoritiesSql = DEF_DELETE_USER_AUTHORITIES_SQL;
    private final String createAuthoritySql = DEF_INSERT_AUTHORITY_SQL;
    private final String updateUserSql = DEF_UPDATE_USER_SQL;
    private final String updateLinkUserSql = DEF_UPDATE_LINK_USER_SQL;
    private final String findUsersNotInGroup = DEF_CUSTOM_USERS_NOT_IN_GROUP_SQL;

    private PeopleResourceClient peopleResourceClient;
    private static final SimpleGrantedAuthority SIMPLE_GA_DEFAULT_ROLE = new SimpleGrantedAuthority(DEFAULT_ROLE);

    public CustomUserDetailsManager() {
        setEnableAuthorities(true);
        setEnableGroups(true);
    }

    @Override
    @Deprecated
    public void createUser(final UserDetails user) {
        throw new IllegalStateException("Not implemented now");
    }

    /**
     * Extend the createUser method to add fullName along side the userId.
     * <p>
     * Note default authority added. Authority DEFAULT_ROLE. Note default group added. Authority ACL_ADMINISTRATOR_GROUP.
     */
    public void createUserWithDefaultGroups(String userId, String fullName, List<GrantedAuthority> authorities) {
        if (!authorities.contains(SIMPLE_GA_DEFAULT_ROLE)) {
            authorities.add(SIMPLE_GA_DEFAULT_ROLE);
        }
        createUser(userId, fullName, authorities);
        addUserToGroup(userId, Constants.ACL_ADMINISTRATOR_GROUP);
    }

    /**
     * Extend the createUser method to add fullName along side the userId
     */
    public void createUser(String userId, String fullName, List<GrantedAuthority> authorities) {
        validateUserDetails(userId, fullName);
        validateAuthorities(authorities);
        getJdbcTemplate().update(DEF_CUSTOM_CREATE_USER_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, userId);
                ps.setString(2, fullName);
                ps.setString(3, "password");
                ps.setBoolean(4, true);
            }
        });

        if (getEnableAuthorities()) {
            insertUserAuthorities(userId, authorities);
        }
    }

    public void updateAcuityUser(final AcuitySidDetails user) {
        validateUserDetails(user);
        getJdbcTemplate().update(updateUserSql, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, user.getPassword());
                ps.setString(2, user.getFullName());
                ps.setBoolean(3, user.isEnabled());
                ps.setString(4, user.getUsername());
            }
        });

        if (getEnableAuthorities()) {
            deleteUserAuthorities(user.getUsername());
            insertUserAuthorities(user);
        }
    }

    /**
     * Links the the user to the linked user id
     *
     * @param userId       normally external/exostar id
     * @param linkToUserId to internal prid
     */
    public void linkUser(final String userId, final String linkToUserId) {
        getJdbcTemplate().update(updateLinkUserSql, ps -> {
            ps.setString(1, linkToUserId);
            ps.setString(2, userId);
        });
    }

    private void validateUserDetails(UserDetails user) {
        Assert.hasText(user.getUsername(), "Username may not be empty or null");
        validateAuthorities(user.getAuthorities());
    }

    private void deleteUserAuthorities(String username) {
        getJdbcTemplate().update(deleteUserAuthoritiesSql, username);
    }

    private void insertUserAuthorities(UserDetails user) {
        for (GrantedAuthority auth : user.getAuthorities()) {
            getJdbcTemplate().update(createAuthoritySql, user.getUsername(), auth.getAuthority());
        }
    }

    /**
     * Overrides the loadUsersByUsername method to add the fullName and return.
     * <p>
     * If the user isnt present, they are added with DEFAULT_ROLE
     */
    @Override
    protected List<UserDetails> loadUsersByUsername(String userId) {
        // check if exists first
        boolean userExists = userExists(userId);
        if (!userExists) {
            //check if its an email, therefore exostar external user
            boolean isExternal = AcuitySidDetails.isExternal(userId);

            if (isExternal) {
                LOG.info("User " + userId + " not found in db, this is an external user, creating new user with fullname {} roles {}",
                        userId, UserService.DEFAULT_ROLE);
                createUserWithDefaultGroups(userId, userId, Lists.newArrayList());
            } else {
                //create a user with default roles
                String fullname = null;
                try {
                    fullname = peopleResourceClient.getFullName(userId);
                } catch (Exception ex) {
                    // fullname not found in people service
                    LOG.warn("UserId " + userId + " has no record in people service for fullname, defaulting to userId", ex);
                    fullname = userId;
                }
                LOG.info("User " + userId + " not found in db, creating new user with fullname {} roles {}", fullname, UserService.DEFAULT_ROLE);
                createUserWithDefaultGroups(userId, fullname, Lists.newArrayList());
            }
        }

        return loadUsersByUsernameForTest(swapUserIdIfExternalLinked(userId));
    }

    /**
     * If the userId is an exostar/external id, then check if its present in the linked table, if so load the user that is linked and not the exostar/external
     * one
     *
     * @param userId user to swap if exostar/external and has linked account
     * @return new linked account id
     */
    protected String swapUserIdIfExternalLinked(String userId) {
//        if (AcuityUserDetails.isExternal(userId)) {
        Optional<String> findLinkedAccount = findLinkedAccount(userId);
        LOG.debug("Checking if linked account");
        if (findLinkedAccount.isPresent()) {
            LOG.debug(userId + " is linked to " + findLinkedAccount.get() + " returning linked userId instead");
            return findLinkedAccount.get();
        }
        //      }
        return userId;
    }

    /**
     * Finds the linked account for a user
     */
    protected Optional<String> findLinkedAccount(String userId) {
        try {
            String linkedUserId = getJdbcTemplate().queryForObject(DEF_CUSTOM_USERS_BY_LINKED_USERNAME_QUERY,
                    new String[]{userId}, (rs, rowNum) -> rs.getString(1));
            if (linkedUserId != null) {
                LOG.debug("user {} has linked account to {}", userId, linkedUserId);
                return Optional.of(linkedUserId);
            }
        } catch (EmptyResultDataAccessException er) {
        }

        return Optional.empty();
    }

    /**
     * Finds users not in a group
     *
     * @param groupName that the users arent in groupName
     * @return list of usernames
     */
    public List<String> findUsersNotInGroup(String groupName) {
        Assert.hasText(groupName);
        return getJdbcTemplate().queryForList(findUsersNotInGroup, new String[]{groupName}, String.class);
    }

    /**
     * Used for testing, only loads a user, doesnt create one
     * <p>
     * Copied straight from JdbcDaoImpl
     */
    public UserDetails loadUserByUsernameForTest(String username) throws UsernameNotFoundException {
        List<UserDetails> users = loadUsersByUsernameForTest(username);

        if (users.size() == 0) {
            logger.debug("Query returned no results for user '" + username + "'");

            throw new UsernameNotFoundException(
                    messages.getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"));
        }

        UserDetails user = users.get(0); // contains no GrantedAuthority[]

        Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

        if (getEnableAuthorities()) {
            dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));
        }

        if (getEnableGroups()) {
            dbAuthsSet.addAll(loadGroupAuthorities(user.getUsername()));
        }

        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);

        addCustomAuthorities(user.getUsername(), dbAuths);

        if (dbAuths.size() == 0) {
            logger.debug("User '" + username + "' has no authorities and will be treated as 'not found'");

            throw new UsernameNotFoundException(
                    messages.getMessage("JdbcDaoImpl.noAuthority",
                            new Object[]{username}, "User {0} has no GrantedAuthority"));
        }

        return createUserDetails(username, user, dbAuths);
    }

    /**
     * Used for testing loadUsersByUsername method to add the fullName and return. but doesnt create a user
     */
    public List<UserDetails> loadUsersByUsernameForTest(String userId) {
        LOG.debug("Running query: " + DEF_CUSTOM_USERS_BY_USERNAME_QUERY + ", with params: " + userId);
        return getJdbcTemplate().query(DEF_CUSTOM_USERS_BY_USERNAME_QUERY, new String[]{userId}, new RowMapper<UserDetails>() {
            public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                String userId = rs.getString(1);
                String fullName = rs.getString(2);
                boolean enabled = rs.getBoolean(3);
                return new AcuitySidDetails(userId, fullName, true, true, true, enabled, AuthorityUtils.NO_AUTHORITIES);
            }
        });
    }

    /**
     * Overrides method just to return the original UserDetails which is the AcuityUserDetails but add the combinedAuthorities
     */
    @Override
    protected UserDetails createUserDetails(String userId, UserDetails userFromUserQuery, List<GrantedAuthority> combinedAuthorities) {
        LOG.debug("Creating user details for " + userId);
        Optional<String> linkedAccount = findLinkedAccount(userId);
        if (linkedAccount.isPresent()) {
            LOG.debug("Linked account for " + userId + " to " + linkedAccount.get());
            AcuitySidDetails linkedAcuityUserDetails = (AcuitySidDetails) loadUserByUsernameForTest(linkedAccount.get());
            AcuitySidDetails mainAccount = new AcuitySidDetails(userFromUserQuery.getUsername(), ((AcuitySidDetails) userFromUserQuery).getFullName(), true,
                    true, true, userFromUserQuery.isEnabled(), combinedAuthorities);
            mainAccount.setLinkeduser(linkedAcuityUserDetails);
            return mainAccount;
        } else {
            return new AcuitySidDetails(userFromUserQuery.getUsername(), ((AcuitySidDetails) userFromUserQuery).getFullName(), true,
                    true, true, userFromUserQuery.isEnabled(), combinedAuthorities);
        }
    }

    private void insertUserAuthorities(String userId, List<GrantedAuthority> authorities) {
        for (GrantedAuthority auth : authorities) {
            getJdbcTemplate().update(DEF_INSERT_AUTHORITY_SQL, userId, auth.getAuthority());
        }
    }

    private void validateUserDetails(String userId, String fullName) {
        Assert.hasText(userId, "userID may not be empty or null");
        Assert.hasText(fullName, "fullName may not be empty or null");
    }

    private void validateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Authorities list must not be null");

        for (GrantedAuthority authority : authorities) {
            Assert.notNull(authority, "Authorities list contains a null entry");
            Assert.hasText(authority.getAuthority(), "getAuthority() method must return a non-empty string");
        }
    }

    @Autowired
    public void setCustomDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    @Autowired
    public void setPeopleResourceClient(PeopleResourceClient peopleResourceClient) {
        this.peopleResourceClient = peopleResourceClient;
    }
}
