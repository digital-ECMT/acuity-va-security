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

package com.acuity.va.security.acl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

/**
 * Added extra information to the UserDetails interface.
 * <p/>
 * Also added setters so can be easily deserialised into json.
 * <p>
 * This also implements Authentication for simplicity for acuity code
 *
 * @author Glen
 */
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true, value = {"credentials", "name", "details", "principal", "external", "username", "password", "accountNonExpired",
        "accountNonLocked", "credentialsNonExpired", "authenticated"})
public class AcuitySidDetails implements UserDetails, Authentication {

    //~ Instance fields ================================================================================================
    private String sid;
    private boolean isGroup = false;
    private AcuitySidDetails linkeduser;
    private String fullName;
    private Collection<SimpleGrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private boolean authenticated = false;

    public AcuitySidDetails() {
    }

    public AcuitySidDetails(String userId, String fullName, boolean accountNonExpired, boolean accountNonLocked,
                            boolean credentialsNonExpired, boolean enabled, Collection<? extends GrantedAuthority> authorities) {

        this.sid = userId;
        this.fullName = fullName;
        this.authorities = (Collection<SimpleGrantedAuthority>) authorities;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.authenticated = true;
    }

    public AcuitySidDetails(String userId, String fullName, Collection<? extends GrantedAuthority> authorities) {
        this(userId, fullName, true, true, true, true, authorities);
    }

    public AcuitySidDetails(String userId, String fullName) {
        this(userId, fullName, true, true, true, true, Lists.newArrayList());
    }

    public AcuitySidDetails(String userId, String fullName, AcuitySidDetails linkedAccount) {
        this(userId, fullName, true, true, true, true, Lists.newArrayList());
        this.linkeduser = linkedAccount;
    }

    public AcuitySidDetails(String userId) {
        this(userId, userId, true, true, true, true, Lists.newArrayList());
    }

    public AcuitySidDetails(Authentication authentication) {
        this(authentication.getName(), authentication.getName(), authentication.getAuthorities());
    }

    public static AcuitySidDetails toUser(String userId) {
        return new AcuitySidDetails(userId);
    }

    public static AcuitySidDetails toUserFromAuthority(String grantedAuthority) {
        return toUserFromGroup(GroupAuthorityConverter.toGroup(grantedAuthority));
    }

    public static AcuitySidDetails toUserFromGroup(String group) {
        AcuitySidDetails acuitySidDetails = new AcuitySidDetails(group);
        acuitySidDetails.isGroup = true;
        acuitySidDetails.authorities = newArrayList(new SimpleGrantedAuthority(GroupAuthorityConverter.toAuthority(group)));

        return acuitySidDetails;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    @Override
    public String getPassword() {
        return "password";
    }

    @Override
    @Deprecated
    public String getUsername() {
        return getSidAsString();
    }

    @Deprecated
    public String getUserId() {
        return getSidAsString();
    }

    @Deprecated
    public void setUserId(String userId) {
        this.sid = userId;
    }

    public String getSidAsString() {
        return sid;
    }

    public void setSidAsString(String sid) {
        this.sid = sid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public AcuitySidDetails getLinkeduser() {
        return linkeduser;
    }

    public void setLinkeduser(AcuitySidDetails linkeduser) {
        this.linkeduser = linkeduser;
    }

    @Override
    @JsonIgnore
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    public void setAuthorities(Collection<SimpleGrantedAuthority> authorities) {
        //this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    @JsonProperty("enabled")
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAuthoritiesAsString() {
        if (authorities == null) {
            return newArrayList();
        }
        return authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(toList());
    }

    @JsonIgnore
    public String getAuthoritiesAsStringToString() {
        return StringUtils.join(getAuthoritiesAsString(), ", ");
    }

    public void setAuthoritiesAsString(List<String> authoritiesAsString) {
        this.authorities = authoritiesAsString.stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    /**
     * this is used to produce a Sid for the user or group.
     * <p>
     * ie when the code is adding and removing aces to the db
     */
    public Sid toSid() {
        if (isGroup) {
            return new GrantedAuthoritySid(GroupAuthorityConverter.toAuthority(getSidAsString()));
        } else {
            return new PrincipalSid(getSidAsString());
        }
    }

    /**
     * This is used for permission based calls to Spring Acl (to check permission), as it needs to use the PrincipalSid and GrantedAuthoritySid
     * to see if it has permission.
     * <p>
     * generates a list of sids for this object, with PrincipalSid for the username and GrantedAuthoritySid for the roles.
     */
    public List<Sid> toSids() {
        Set<Sid> sids = Sets.newHashSet(toSid());
        if (authorities != null) {
            authorities.forEach(ga -> sids.add(new GrantedAuthoritySid(ga.getAuthority())));
        }

        return newArrayList(sids);
    }

    /**
     * is the userid a external, an external is in one like glen@securepass.mail.com, ie an email
     */
    public boolean isExternal() {
        return isExternal(sid);
    }

    public static boolean isExternal(String userId) {
        EmailValidator validator = EmailValidator.getInstance();
        return validator.isValid(userId);
    }

    /**
     * Copied from super class, added extra class variables
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getClass().getSimpleName()).append(": ");
        sb.append("UserId: ").append(getUserId()).append("; ");
        sb.append("isGroup: ").append(isGroup).append("; ");
        sb.append("Fullname: ").append(getFullName()).append("; ");
        sb.append("Password: [PROTECTED]; ");
        sb.append("Enabled: ").append(isEnabled()).append("; ");
        sb.append("AccountNonExpired: ").append(isAccountNonExpired()).append("; ");
        sb.append("credentialsNonExpired: ").append(isCredentialsNonExpired()).append("; ");
        sb.append("AccountNonLocked: ").append(isAccountNonLocked()).append("; ");

        if (CollectionUtils.isNotEmpty(getAuthorities())) {
            sb.append("Granted Authorities: ");

            boolean first = true;
            for (SimpleGrantedAuthority auth : getAuthorities()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;

                sb.append(auth);
            }
        } else {
            sb.append("Not granted any authorities");
        }
        if (linkeduser != null) {
            sb.append("Linked User: ").append(linkeduser).append("; ");
        }
        return sb.toString();
    }

    //  Authentication methods
    @Override
    public Object getCredentials() {
        return getPassword();
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }
    //  End Authentication methods
}
