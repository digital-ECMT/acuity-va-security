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

package com.acuity.va.security.acl.dao;

import java.util.List;

import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.UsernameFullNameAndLinkedAccount;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for Acl queries.
 *
 * @author Glen
 */
@Repository
@Transactional(value = "security", readOnly = true)
public interface UserRepositoryImpl {

    /**
     * Gets all the users for a particular role assigned to a group and then union that will all the users from the role assigned directly to the user
     *
     * Result of this query should not be cached, because group_members and group_authorities tables can be modified by Spring Security
     *
     * @param role/authority the user is assigned to
     */
    @Select("SELECT distinct users.username FROM users "
        + "JOIN group_members ON group_members.username = users.username "
        + "JOIN group_authorities ON group_authorities.group_id = group_members.group_id "
        + "WHERE group_authorities.authority = #{role} UNION "
        + "SELECT username FROM authorities WHERE authorities.authority = #{role}")
    @Options(useCache = false, flushCache = true)
    List<String> listUsersByRole(String role);

    /**
     * Gets all the users
     */
    @Select("SELECT distinct username AS userId, fullname AS fullName FROM users "
        + "WHERE enabled = true")
    @Options(useCache = false, flushCache = true)
    List<AcuitySidDetails> listAllEnabledUsers();

    /**
     * Gets fullname from userId
     */
    @Select("SELECT fullname FROM users "
        + "WHERE username = #{userId}")
    @Options(useCache = false, flushCache = true)
    String getFullNameForUser(String userId);

    /**
     * Gets fullname from all users
     */
    @Select("SELECT username, fullname, linked_username AS linkedUsername FROM users WHERE enabled = true")
    @Options(useCache = false, flushCache = true)
    List<UsernameFullNameAndLinkedAccount> getAllFullNameAndLinkedAccount();
}
