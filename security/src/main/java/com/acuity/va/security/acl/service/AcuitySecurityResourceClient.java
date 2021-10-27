package com.acuity.va.security.acl.service;

import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interacts with the remove acuity security web services
 *
 * @author Glen
 */
@Service
public class AcuitySecurityResourceClient {

    private static final Logger LOG = LoggerFactory.getLogger(AcuitySecurityResourceClient.class);

//    @Value
    private String url;
    @Value("${acuity.security.basic.username:none}")
    private String username;
    @Value("${acuity.security.basic.password:none}")
    private String password;
    private static final String PATH = "resources/security";
    protected WebTarget target = null;

    @PostConstruct
    public void addBasicAuth() {
        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials(username, password)
                .build();
        Client client = ClientBuilder.newClient();
        client.register(authFeature);
        /**
         * this target object was previously build on reference to corporate server
         * I guess it won't be used anymore
         */
//        target = client.target(url).path(PATH);
    }

    /**
     * Loads a AcuityUserDetails from the userId
     *
     * @param userId userId to load
     * @return AcuityUserDetails object of name, fullname and authorities
     */
    public AcuitySidDetails loadUserByUsername(String userId) throws IllegalAccessException {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Looking up userId {} details", userId);

        WebTarget newTarget = target.path("/loadUserByUsername/" + userId);

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalAccessException("Username and/or password incorrect for remote server basic auth");
        } else if (response.getStatus() == 404) { // user not found
            throw new UsernameNotFoundException("Username " + userId + " not found");
        }

        return response.readEntity(AcuitySidDetails.class);
    }

    /**
     * Gets all the AcuityObjectIdentityWithPermission for the user
     *
     * @param userId userId to get permissions
     * @return List<AcuityObjectIdentityWithPermission> that the user has access to
     */
    public List<AcuityObjectIdentityWithPermission> getAclsForUser(String userId) throws IllegalAccessException {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Get AcuityObjectIdentityWithPermissions for userId {}", userId);

        WebTarget newTarget = target.path("/acls/" + userId);

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).get(Response.class);
        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalAccessException("Username and/or password incorrect for remote server basic auth");
        } else if (response.getStatus() == 404) { // user not found
            throw new UsernameNotFoundException("Username " + userId + " not found");
        }

        return response.readEntity(new GenericType<List<AcuityObjectIdentityWithPermission>>() {
        });
    }

    /**
     * Checks if the user has permissionMask for the acuityObjectIdentity
     *
     * @param userId prid of the user
     * @param acuityObjectIdentity acuityObjectIdentity of what to query on, ie DrugProgramme 2
     * @param permissionMask permissionMask of the permission to check for, ie 32 is ViewDataset
     * @return boolean has permission or not
     */
    @Cacheable("AcuitySecurityResourceClient-hasPermissionForUser")
    public boolean hasPermissionForUser(String userId, AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask) {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Checking hasPermissionForUser for user {} for {} with permission {}", userId, acuityObjectIdentity, permissionMask);

        WebTarget newTarget = target.path("/acl/" + acuityObjectIdentity.getClass().getSimpleName() + "/"
                + acuityObjectIdentity.getId() + "/" + permissionMask + "/" + userId);

        try {
            LOG.debug("URL " + newTarget.getUri());
            Response response = newTarget.request(APPLICATION_JSON).get(Response.class);

            if (response.getStatus() == 401) { // invalid username or password
                throw new IllegalStateException("Username and/or password incorrect for remote server basic auth");
            } else {
                return response.getStatus() == HttpStatus.ACCEPTED.value() || response.getStatus() == HttpStatus.OK.value();
            }
        } catch (Exception ex) {
            LOG.error("Exception calling hasPermissionForUser", ex);
            return false;
        }
    }

    /**
     * Gets the users permissionMask for the acuityObjectIdentity
     *
     * @param userId prid of the user
     * @param acuityObjectIdentity acuityObjectIdentity of what to query on, ie DrugProgramme 2
     * @return boolean permissionMask
     */
    @Cacheable("AcuitySecurityResourceClient-getPermissionForUser")
    public int getPermissionForUser(String userId, AcuityObjectIdentity acuityObjectIdentity) throws IllegalAccessException {
        if (target == null) {
            addBasicAuth();
        }

        LOG.debug("Checking getPermissionForUser for user {} for {}", userId, acuityObjectIdentity);

        WebTarget newTarget = target.path("/acl/" + acuityObjectIdentity.getClass().getSimpleName() + "/" + acuityObjectIdentity.getId() + "/" + userId);

        LOG.debug("URL " + newTarget.getUri());
        Response response = newTarget.request(APPLICATION_JSON).get(Response.class);

        if (response.getStatus() == 401) { // invalid username or password
            throw new IllegalAccessException("Username and/or password incorrect for remote server basic auth");
        } else if (response.getStatus() == 404) { // user not found
            throw new UsernameNotFoundException("Username " + userId + " not found");
        }

        return response.readEntity(Integer.class);
    }
}
