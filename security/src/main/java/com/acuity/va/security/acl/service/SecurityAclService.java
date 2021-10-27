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
import com.acuity.va.security.acl.domain.AcuityObjectIdentity;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermission;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithPermissionAndLockDown;
import com.acuity.va.security.acl.domain.ClinicalStudy;
import com.acuity.va.security.acl.domain.Dataset;
import com.acuity.va.security.acl.domain.DetectDataset;
import com.acuity.va.security.acl.domain.DrugProgramme;
import com.acuity.va.security.acl.domain.DrugStudyDatasetWithPermission;
import com.acuity.va.security.acl.domain.AcuityObjectIdentityWithParent;
import com.acuity.va.security.acl.domain.AcuitySidDetails;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMask;
import com.acuity.va.security.acl.domain.AcuitySidDetailsWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.domain.SidWithPermissionMaskAndGranted;
import com.acuity.va.security.acl.domain.UsernameFullNameAndLinkedAccount;
import com.acuity.va.security.acl.permissions.AclPermissionCalculator;
import com.acuity.va.security.acl.permissions.AcuityPermissionViewPackagesManager;
import com.google.common.collect.Sets;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.acuity.va.security.acl.permissions.AcuityCumulativePermissionsAsRoles.AUTHORISED_USER;
import static com.acuity.va.security.common.Constants.HOURLY_REFRESHABLE_CACHE;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * SecurityAclService (Renamed because spring has an AclService) implements all the methods of the aclObjectRepository that needs extra processing or methods
 * needing MutableAclService interaction.
 * <p>
 * Any methods needed for interacting with the ACL schema are put here.
 * <p>
 * No point just making a dumb service that all it does it proxy to aclObjectRepository.
 *
 * @author Glen
 */
@Service
@Transactional(value = "security", readOnly = true)
public class SecurityAclService {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAclService.class);

    @Autowired
    private AclRepository aclObjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MutableAclService mutableAclService;
    @Autowired
    private PermissionFactory permissionFactory;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AclCache aclCache;
    @Autowired
    private AclPermissionCalculator aclPermissionCalculator;
    @Autowired
    private AcuityPermissionViewPackagesManager permissionViewPackagesManager;

    /**
     * Lists all the aces for an acl that has a isGranting = true permission and returns a list of AcuitySidDetailsWithPermissionMask.
     * <p>
     * This doesnt include parent drug programmes
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public List<AcuitySidDetailsWithPermissionMaskAndGranted> getAllPermissionForAcl(AcuityObjectIdentity acuityAclObjectIdentity) {
        Acl acl = find(acuityAclObjectIdentity);

        List<UsernameFullNameAndLinkedAccount> users = userRepository.getAllUsernameFullNameAndLinkedAccount();

        List<AcuitySidDetailsWithPermissionMaskAndGranted> usersAcuitySidDetails
                = acl.getEntries().stream().filter(e -> e.getSid() instanceof PrincipalSid).map(e -> {

            String fullName = "Unknown";
            Optional<UsernameFullNameAndLinkedAccount> userFindFirst = users.stream().
                    filter(user -> user.getUsername().equals(((PrincipalSid) e.getSid()).getPrincipal())).findFirst();

            if (userFindFirst.isPresent()) {
                fullName = userFindFirst.get().getFullname();
            }
            AcuitySidDetails acuityUserDetails = new AcuitySidDetails(((PrincipalSid) e.getSid()).getPrincipal(), fullName);
            Integer permissionMask = e.getPermission().getMask();
            boolean granted = e.isGranting();

            return new AcuitySidDetailsWithPermissionMaskAndGranted(acuityUserDetails, permissionMask, granted);
        }).collect(toList());

        List<AcuitySidDetailsWithPermissionMaskAndGranted> groupsAcuitySidDetails
                = acl.getEntries().stream().filter(e -> e.getSid() instanceof GrantedAuthoritySid).map(e -> {

            String grantedAuthority = ((GrantedAuthoritySid) e.getSid()).getGrantedAuthority();
            AcuitySidDetails acuityGroupDetails = AcuitySidDetails.toUserFromAuthority(grantedAuthority);
            Integer permissionMask = e.getPermission().getMask();
            boolean granted = e.isGranting();

            return new AcuitySidDetailsWithPermissionMaskAndGranted(acuityGroupDetails, permissionMask, granted);
        }).collect(toList());

        return newArrayList(concat(usersAcuitySidDetails, groupsAcuitySidDetails));
    }

    /**
     * Find the sids permission for a acuity Object Identity, ie DrugProgramme 10
     *
     * @param acuityAclObjectIdentity Ie DrugProgramme 10
     * @param sid  user
     * @return user permission
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public AccessControlEntry getSidsAceForAcl(AcuityObjectIdentity acuityAclObjectIdentity, Sid sid) {
        Acl acl = find(acuityAclObjectIdentity);
        // start from lowest permission and go higher until its not granted.
        Optional<AccessControlEntry> ace = acl.getEntries().stream().filter(e -> e.getSid().equals(sid)).findFirst();
        return ace.orElse(null);
    }

    /**
     * Gets the counts of permissions for a DrugProgramme, including groups users
     *
     * @param drugProgramme Ie DrugProgramme 10
     * @return List MaskCounts
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public int getGrantedUsersAmountForDrugProgramme(DrugProgramme drugProgramme) {
        return getGrantedUsersForDrugProgramme(drugProgramme).size();
    }

    /**
     * Gets the counts of permissions for a dataset, including the parent drug programme and groups users
     *
     * @param dataset Ie Dataset 10
     * @return List MaskCounts
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public int getGrantedUsersAmountForDataset(Dataset dataset) {
        return getGrantedUsersForDataset(dataset).size();
    }

    /**
     * Lists all the granted sids for the acl, including groups users and inherited drug programme users and groups
     *
     * @param acuityAclObjectIdentity Ie DrugProgramme 10
     * @return List AcuitySidDetailsWithPermissionMask
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public List<AcuitySidDetailsWithPermissionMask> getGrantedUsersForAcl(AcuityObjectIdentity acuityAclObjectIdentity) {
        LOG.debug("AcuityObjectIdentity: " + acuityAclObjectIdentity);

        if (acuityAclObjectIdentity instanceof DrugProgramme) {
            return getGrantedUsersForDrugProgramme((DrugProgramme) acuityAclObjectIdentity);
        } else if (acuityAclObjectIdentity instanceof ClinicalStudy) {
            return getGrantedUsersForClinicalStudy((ClinicalStudy) acuityAclObjectIdentity);
        } else if (acuityAclObjectIdentity instanceof Dataset) {
            return getGrantedUsersForDataset((Dataset) acuityAclObjectIdentity);
        } else {
            throw new IllegalStateException("Unknown AcuityObjectIdentity " + acuityAclObjectIdentity);
        }
    }

    /**
     * Lists all the granted sids for the DrugProgramme, including groups users
     *
     * @param drugProgramme Ie DrugProgramme 10
     * @return List AcuitySidDetailsWithPermissionMask
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    protected List<AcuitySidDetailsWithPermissionMask> getGrantedUsersForDrugProgramme(DrugProgramme drugProgramme) {
        LOG.debug("DrugProgramme: " + drugProgramme);
        Acl acl = find(drugProgramme);

        List<SidWithPermissionMaskAndGranted> drugProgrammePermissions
                = aclObjectRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(drugProgramme.getClass().getName(), drugProgramme.getId());

        return listAllGrantedUsers(acl, newArrayList(), drugProgrammePermissions);
    }

    /**
     * Lists all the granted sids for the Dataset, including the parent drug programme and groups users
     *
     * @param dataset Ie Dataset 10
     * @return List AcuitySidDetailsWithPermissionMask
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    protected List<AcuitySidDetailsWithPermissionMask> getGrantedUsersForDataset(Dataset dataset) {
        LOG.debug("Dataset: " + dataset);
        Acl acl = find(dataset);

        AcuityObjectIdentity parentDrugProgramme = aclObjectRepository.getDatasetParentDrugProgrammeFromDB(dataset);

        LOG.debug("Dataset drug programme id: " + parentDrugProgramme);

        List<SidWithPermissionMaskAndGranted> datasetPermissions
                = aclObjectRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(dataset.getClass().getName(), dataset.getId());

        List<SidWithPermissionMaskAndGranted> drugProgrammePermissions = newArrayList();
        if (acl.isEntriesInheriting()) {
            drugProgrammePermissions
                    = aclObjectRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(
                    dataset.getDrugProgrammeClass().getName(), parentDrugProgramme.getId());
        }

        return listAllGrantedUsers(acl, datasetPermissions, drugProgrammePermissions);
    }

    /**
     * Lists all the granted sids for the ClinicalStudy, including the parent drug programme and groups users
     *
     * @param clinicalStudy Ie ClinicalStudy 10
     * @return List AcuitySidDetailsWithPermissionMask
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    protected List<AcuitySidDetailsWithPermissionMask> getGrantedUsersForClinicalStudy(ClinicalStudy clinicalStudy) {
        LOG.debug("ClinicalStudy: " + clinicalStudy);
        Acl acl = find(clinicalStudy);

        AcuityObjectIdentity parentDrugProgramme = aclObjectRepository.getClinicalStudyParentDrugProgrammeFromDB(clinicalStudy);
        List<SidWithPermissionMaskAndGranted> clinicalStudyPermissions
                = aclObjectRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(clinicalStudy.getClass().getName(), clinicalStudy.getId());

        List<SidWithPermissionMaskAndGranted> drugProgrammePermissions = newArrayList();

        if (acl.isEntriesInheriting()) {
            drugProgrammePermissions
                    = aclObjectRepository.getAcuityObjectIdentitySidsWithPermissionMaskAndGranted(
                    clinicalStudy.getDrugProgrammeClass().getName(), parentDrugProgramme.getId());
        }

        return listAllGrantedUsers(acl, clinicalStudyPermissions, drugProgrammePermissions);
    }

    private List<AcuitySidDetailsWithPermissionMask> listAllGrantedUsers(Acl acl, List<SidWithPermissionMaskAndGranted> datasetOrStudy,
                                                                         List<SidWithPermissionMaskAndGranted> parentDrugProgramme) {

        Set<String> parentDrugProgrammeGrantedUsers = parentDrugProgramme.stream()
                .filter(p -> p.isGranted() && p.isUser()).map(SidWithPermissionMaskAndGranted::getSid).collect(toSet());
        Set<String> parentDrugProgrammeGrantedGroups = parentDrugProgramme.stream()
                .filter(p -> p.isGranted() && !p.isUser()).map(SidWithPermissionMaskAndGranted::getSid).collect(toSet());

        Set<String> datasetOrStudyGrantedUsers = datasetOrStudy.stream()
                .filter(p -> p.isGranted() && p.isUser()).map(SidWithPermissionMaskAndGranted::getSid).collect(toSet());
        Set<String> datasetOrStudyGrantedGroups = datasetOrStudy.stream()
                .filter(p -> p.isGranted() && !p.isUser()).map(SidWithPermissionMaskAndGranted::getSid).collect(toSet());

        parentDrugProgrammeGrantedGroups.forEach(g -> {
            AcuitySidDetails group = AcuitySidDetails.toUserFromAuthority(g);
            parentDrugProgrammeGrantedUsers.addAll(userService.getUsersByGroup(group.getSidAsString()));
        });

        datasetOrStudyGrantedGroups.forEach(g -> {
            AcuitySidDetails group = AcuitySidDetails.toUserFromAuthority(g);
            datasetOrStudyGrantedUsers.addAll(userService.getUsersByGroup(group.getSidAsString()));
        });

        LOG.debug("Counting users for DP: " + parentDrugProgrammeGrantedUsers.size()
                + ",  and datasetOrStudy: " + datasetOrStudyGrantedUsers.size());

        Set<String> allUsersToCheck = Sets.union(parentDrugProgrammeGrantedUsers, datasetOrStudyGrantedUsers);

        List<AcuitySidDetailsWithPermissionMask> users = newArrayList();

        allUsersToCheck.forEach(user -> {
            AcuitySidDetails rsd = userService.getUserWithOutSwappingLinkedUser(user);
            Permission permission = aclPermissionCalculator.getRolePermission(acl, rsd);
            if (permission != null) {
                users.add(new AcuitySidDetailsWithPermissionMask(rsd, permission.getMask()));
            }
        });

        return users;
    }

    /**
     * Lists all the ObjectIdentities configured in the acuity db and their associated permission on that object.
     * <p>
     * Ie for user Glen DrugProgram A might have permission VIEW_VISUALISATION 32 Clinical Study 1 might have permission EDIT_TRAINED_USERS 64
     */
    public List<AcuityObjectIdentityWithPermissionAndLockDown> getUserObjectIdentities(AcuitySidDetails user) {
        return getUserObjectIdentities(user, true);
    }

    public List<AcuityObjectIdentityWithPermissionAndLockDown> getUserObjectIdentities(AcuitySidDetails user, boolean useCache) {
        List<AcuityObjectIdentityWithPermissionAndLockDown> allObjectIdentities;
        if (useCache) {
            allObjectIdentities = aclObjectRepository.listObjectIdentities();
        } else {
            allObjectIdentities = aclObjectRepository.listObjectIdentitiesNotCached();
        }

        final AtomicInteger index = new AtomicInteger(0);

        // AcuityObjectIdentityWithPermission objects are cached to need to create new object from them to manipulate
        return allObjectIdentities.stream().map(o -> (AcuityObjectIdentityWithPermissionAndLockDown) o.cloneAndReset())
                .filter(clonedObjId -> {
                    index.incrementAndGet();
                    try {
                        Acl acl;
                        try {
                            long start = System.nanoTime();

                            acl = find(clonedObjId, user.toSids());

                            if (index.get() % 50 == 0) { //just log one out of 50
                                LOG.debug(NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds to find " + clonedObjId);
                            }
                        } catch (NotFoundException nfe) {
                            return false;
                        }

                        // start from lowest permission and go higher until its not granted.
                        Permission rolePermission = aclPermissionCalculator.getRolePermission(acl, user);

                        if (rolePermission != null) { // has some sort of permission

                            clonedObjId.setRolePermissionMask(rolePermission.getMask());

                            /*
                             * Here a user could have role permission DEVELOPMENT_TEAM/Global Admin and NO view permissions
                             * Hence they would have rolePermission = DEVELOPMENT_TEAM, viewPermission null, canView = false
                             */
                            Permission viewPermission = aclPermissionCalculator.getViewPermission(acl, user);

                            if (viewPermission != null && viewPermission.getMask() != 0) {
                                clonedObjId.setCanView(true);

                                clonedObjId.setViewPermissionMask(viewPermission.getMask());

                            } else {
                                clonedObjId.setViewPermissionMask(0);
                                clonedObjId.setCanView(false);
                            }
                        } else {
                            clonedObjId.setCanView(false);
                        }

                        return clonedObjId.hasRolePermission();
                    } catch (Exception ex) {
                        LOG.error("Unable to check permssion", ex);
                        return false;
                    }

                }).collect(toList());
    }

    /**
     * Get the role permission for a user for a AcuityAclObjectIdentity.
     *
     * @param acuityObjectIdentity Ie DrugProgramme 10
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public Permission getRolePermissionForUser(AcuityObjectIdentity acuityObjectIdentity, AcuitySidDetails user) {

        MutableAcl acl = find(acuityObjectIdentity);

        return aclPermissionCalculator.getRolePermission(acl, user);
    }

    /**
     * Get the view permission for a user for a AcuityAclObjectIdentity.
     *
     * @param acuityObjectIdentity Ie DrugProgramme 10
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public Permission getViewPermissionForUser(AcuityObjectIdentity acuityObjectIdentity, AcuitySidDetails user) {

        MutableAcl acl = find(acuityObjectIdentity);

        return aclPermissionCalculator.getViewPermission(acl, user);
    }

    /**
     * Finds an Acl by the AcuityAclObjectIdentity.
     *
     * @param acuityObjectIdentity Ie DrugProgramme 10
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public MutableAcl find(AcuityObjectIdentity acuityObjectIdentity) {
        if (acuityObjectIdentity == null) {
            return null;
        }

        // This doesnt work readAclById(acuityObjectIdentity)
        // JBDCAclService l:110, this check stops it working, different objects been checked
        return (MutableAcl) mutableAclService.readAclById(new ObjectIdentityImpl(acuityObjectIdentity));
    }

    /**
     * Finds an Acl by the AcuityAclObjectIdentity.
     *
     * @param acuityObjectIdentity Ie DrugProgramme 10
     */
    @Transactional(value = "security", readOnly = true, noRollbackFor = NotFoundException.class)
    public MutableAcl find(AcuityObjectIdentity acuityObjectIdentity, List<Sid> sids) {
        if (acuityObjectIdentity == null) {
            return null;
        }

        // This doesnt work readAclById(acuityObjectIdentity)
        // JBDCAclService l:110, this check stops it working, different objects been checked
        return (MutableAcl) mutableAclService.readAclById(new ObjectIdentityImpl(acuityObjectIdentity), sids);
    }

    /**
     * Creates an ACL entry for a particular ObjectIdentity the the owner.
     *
     * @param objectIdentity to be added, can contain a parent acl already in the db
     * @param owner          of the newly created acl
     * @return acl
     */
    @Transactional(value = "security")
    public MutableAcl createAcl(AcuityObjectIdentityWithParent objectIdentity, String owner) {
        MutableAcl acl = mutableAclService.createAcl(new ObjectIdentityImpl(objectIdentity));
        LOG.debug("Setting owner for acl as " + owner + " for " + objectIdentity);
        acl.setOwner(new PrincipalSid(owner));
        acl.setEntriesInheriting(true);

        MutableAcl parentAcl = find(objectIdentity.getParent());
        if (parentAcl != null) {
            LOG.debug("Setting parent acl " + parentAcl + " for " + objectIdentity);
            acl.setParent(parentAcl);
        }

        mutableAclService.updateAcl(acl);

        return acl;
    }

    /**
     * Add an ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace
     */
    @Transactional(value = "security")
    public void addGrantingAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {
        addAce(acuityObjectIdentity, permissionMask, sid, true);
    }

    /**
     * Add a revoking ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace
     */
    @Transactional(value = "security")
    public void addRevokingAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {
        addAce(acuityObjectIdentity, permissionMask, sid, false);
    }

    /**
     * Adds a revoking/inserting ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace
     * @param granted             whether giving or removing permission
     */
    @Transactional(value = "security")
    public void addAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid, boolean granted) {

        Permission permission = permissionFactory.buildFromMask(permissionMask);

        LOG.debug("Finding.. " + acuityObjectIdentity);
        MutableAcl acl = find(acuityObjectIdentity);

        //mutableAclService.updateAcl(acl); //should reindex the indexes of all the entries
        String type = (granted) ? "Inserting.. " : "Inserting revoking.. ";
        LOG.debug(type + permission + " for user " + sid + " at " + acl.getEntries().size());
        acl.insertAce(acl.getEntries().size(), permission, sid, granted);
        mutableAclService.updateAcl(acl);
    }

    /**
     * Add an ACE entry for a particular objectIdentity for a role with its permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 role of the ace
     */
    @Deprecated
    @Transactional(value = "security")
    public void addRoleAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {

        Permission permission = permissionFactory.buildFromMask(permissionMask);

        MutableAcl acl = find(acuityObjectIdentity);

        mutableAclService.updateAcl(acl); //should reindex the indexes of all the entries
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
        mutableAclService.updateAcl(acl);
    }

    /**
     * Remove a granting ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace to be removed
     */
    @Transactional(value = "security")
    public void removeGrantingAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {
        removeAceWithGranting(acuityObjectIdentity, permissionMask, sid, true);
    }

    /**
     * Remove a revoking ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace to be removed
     */
    @Transactional(value = "security")
    public void removeRevokingAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {
        removeAceWithGranting(acuityObjectIdentity, permissionMask, sid, false);
    }

    /**
     * Removes all ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace to be removed
     */
    @Transactional(value = "security")
    public void removeAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid) {
        removeAceWithGranting(acuityObjectIdentity, permissionMask, sid, null);
    }

    /**
     * Remove an ACE entry for a particular objectIdentity for a user with their permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner/role of the ace to be removed
     * @param granted             whether giving or removing permission
     */
    @Transactional(value = "security")
    public void removeAceWithGranting(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid, Boolean granted) {

        Permission permission = permissionFactory.buildFromMask(permissionMask);

        MutableAcl acl = find(acuityObjectIdentity);
        LOG.debug("Found Acl {}", acl);

        for (int i = 0; i < acl.getEntries().size(); i++) {
            AccessControlEntry e = acl.getEntries().get(i);

            if (granted == null) {
                if (e.getPermission().equals(permission) && e.getSid().equals(sid)) {
                    acl.deleteAce(i);
                    LOG.debug("Deleting Acl entry index {} with AccessControlEntry {}", i, e);
                    break;
                }
            } else if (e.getPermission().equals(permission) && (e.getSid().equals(sid)) && e.isGranting() == granted) {
                acl.deleteAce(i);
                LOG.debug("Deleting Acl entry index {} with AccessControlEntry {}", i, e);
                break;
            }
        }

        mutableAclService.updateAcl(acl);
    }

    /**
     * Remove all ACE entries for a particular objectIdentity for a permission mask.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     */
    @Transactional(value = "security")
    public void removeAllAces(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask) {

        Permission permission = permissionFactory.buildFromMask(permissionMask);

        MutableAcl acl = find(acuityObjectIdentity);
        LOG.debug("Found Acl {}", acl);

        List<AccessControlEntry> entries = acl.getEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            AccessControlEntry e = entries.get(i);
            if (e.getPermission().equals(permission)) {
                acl.deleteAce(i);
                LOG.debug("Deleting Acl entry index {} with AccessControlEntry {}", i, e);
            }
        }

        mutableAclService.updateAcl(acl);
    }

    /**
     * Removes all ACE entries for a particular objectIdentity for a user.
     *
     * @param acuityObjectIdentity in the acl db
     * @param sid                 owner of the ace to be removed
     */
    @Transactional(value = "security")
    public void removeAces(AcuityObjectIdentity acuityObjectIdentity, Sid sid) {

        MutableAcl acl = find(acuityObjectIdentity);
        LOG.debug("Found Acl {}", acl);

        List<AccessControlEntry> entries = acl.getEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            AccessControlEntry e = entries.get(i);
            if (e.getSid().equals(sid)) {
                acl.deleteAce(i);
                LOG.debug("Deleting Acl entry index {} with AccessControlEntry {}", i, e);
            }
        }

        LOG.debug("Finished deleting... ");
        mutableAclService.updateAcl(acl);
    }

    /**
     * Replaces the current aces for a user for another one with permissionMask
     * <p>
     * This first deletes the current dataowner and then adds the new one.
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sid                 owner of the ace to be replaced
     * @param granted             whether giving or removing permission
     */
    @Transactional(value = "security")
    public void replaceAce(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, Sid sid, boolean granted) {
        removeAces(acuityObjectIdentity, sid);
        addAce(acuityObjectIdentity, permissionMask, sid, granted);
    }

    /**
     * Checks if the user as the correct permission for a particular ObjectIdentity.
     * <p>
     * Ie has user user001 for VIEW_VISUALISATIONS (32) permission on DrugProgram 100
     *
     * @param acuityObjectIdentity in the acl db
     * @param permissionMask      integer value of the permission mask
     * @param sids                List<Sid> sids of the user got permission
     */
    public boolean hasPermission(AcuityObjectIdentity acuityObjectIdentity, Integer permissionMask, List<Sid> sids) {
        Permission permission = permissionFactory.buildFromMask(permissionMask);

        return hasPermission(acuityObjectIdentity, permission, sids);
    }

    /**
     * Removed all the user aces/group aces from the db
     * <p>
     * This is done using a SQL query, so it calls refresh cache afterwards
     */
    @Transactional(value = "security")
    public synchronized boolean removeAllAcesForSid(String sid) {
        aclObjectRepository.removeAllAcesForSid(sid);
        clearAclCache();
        return true;
    }

    /**
     * Clears caches involved with the acls
     */
    @Transactional(value = "security")
    public void clearAclCache() {
        aclCache.clearCache();
        cacheManager.clearAllStartingWith(HOURLY_REFRESHABLE_CACHE);
    }

    /**
     * Removes a user from all groups
     */
    @Transactional(value = "security")
    public void removeAllAcesAndGroupsForSid(String userId) {
        removeAllAcesForSid(userId);
        userService.removeUserFromAllGroups(userId);
    }

    /**
     * Checks if the user as the correct permission for a particular ObjectIdentity.
     * <p>
     * Ie has user user001 for VIEW_VISUALISATIONS permission on DrugProgram 100
     */
    private boolean hasPermission(AcuityObjectIdentity acuityObjectIdentity, Permission p, List<Sid> sids) {
        Acl acl = find(acuityObjectIdentity);
        return aclPermissionCalculator.isGranted(acl, sids, p);
    }

    /*
     * Sets the lockdown status of a DetectDataset
     */
    @Transactional(value = "security")
    public boolean setLockdownStatus(DetectDataset vis, boolean lockdownStatus) {
        return aclObjectRepository.setLockdownStatus(vis, lockdownStatus);
    }

    /*
     * Sets the inherit permissions flag of a Dataset
     */
    @Transactional(value = "security")
    public boolean setInheritPermissions(Dataset dataset, boolean inheritPermissionsStatus) {

        MutableAcl acl = find(dataset);

        // set inherited permissions
        acl.setEntriesInheriting(inheritPermissionsStatus);

        // save the new state
        mutableAclService.updateAcl(acl);

        return true;
    }

    ///////////////////////////////////////////////////////
    /////////  Start of view modules permissons ///////////
    ///////////////////////////////////////////////////////
    @Transactional("security")
    public void setViewPackagesAces(AcuityObjectIdentity acuityObjectIdentity, List<Integer> permissionMasks, AcuitySidDetails acuitySidDetails) {
        // remove permissions that are not view packages permissions
        List<Integer> filteredPermissionMask = permissionMasks.stream().distinct().filter(pm -> {
            Permission permission = permissionFactory.buildFromMask(pm);

            return permissionViewPackagesManager.getAllViewPackages().contains(permission) || pm == AUTHORISED_USER.getMask();
        }).collect(toList());

        setViewPermissionAce(acuityObjectIdentity, filteredPermissionMask, acuitySidDetails);
    }

    @Transactional("security")
    public void setExtraViewPackagesAces(AcuityObjectIdentity acuityObjectIdentity, List<Integer> extraPermissionMasks, AcuitySidDetails acuitySidDetails) {
        // remove permissions that are not view packages permissions
        List<Integer> filteredExtraPermissionMask = extraPermissionMasks.stream().distinct().filter(pm -> {
            Permission permission = permissionFactory.buildFromMask(pm);

            return permissionViewPackagesManager.getExtraViewPackages().contains(permission);
        }).collect(toList());

        filteredExtraPermissionMask.add(AUTHORISED_USER.getMask());

        setViewPermissionAce(acuityObjectIdentity, filteredExtraPermissionMask, acuitySidDetails);
    }

    private void setViewPermissionAce(AcuityObjectIdentity acuityObjectIdentity, List<Integer> filteredPermissionMasks, AcuitySidDetails acuitySidDetails) {

        //if(!filteredPermissionMasks.contains(VIEW_VISUALISATIONS.getMask())) {
//            filteredPermissionMasks.add(VIEW_VISUALISATIONS.getMask());
        //      }
        int newPermissionMask = filteredPermissionMasks.stream().mapToInt(Integer::intValue).sum();

        removeAces(acuityObjectIdentity, acuitySidDetails.toSid());
        addGrantingAce(acuityObjectIdentity, newPermissionMask, acuitySidDetails.toSid());
    }
    ///////////////////////////////////////////////////////
    /////////  End of view modules permissons ///////////
    ///////////////////////////////////////////////////////

    /**
     * List all the datasets with the permissions for the user
     */
    public List<DrugStudyDatasetWithPermission> getUserDatasetPermissions(AcuitySidDetails acuityUserDetails) {

        // list all the datasets for the user with their permission
        List<AcuityObjectIdentityWithPermission> grantedDatasets = getUserObjectIdentities(acuityUserDetails)
                .stream().filter(AcuityObjectIdentity::thisDatasetType)
                .collect(toList());

        // list all the datasets in the db
        List<AcuityObjectIdentityWithPermissionAndLockDown> allDatasets = aclObjectRepository.listObjectIdentities()
                .stream().filter(AcuityObjectIdentity::thisDatasetType)
                .collect(toList());

        return allDatasets.stream().map(ds -> {
            Optional<AcuityObjectIdentityWithPermission> isGranted = grantedDatasets.stream().filter(gds -> gds.equals(ds)).findFirst();
            if (isGranted.isPresent()) {
                return new DrugStudyDatasetWithPermission((Dataset) ds, isGranted.get());
            } else {
                return new DrugStudyDatasetWithPermission((Dataset) ds, null);
            }
        }).collect(toList());
    }

    public Optional<AcuityObjectIdentityWithPermissionAndLockDown> getRoiFromStudyId(AcuitySidDetails user, String studyId) {

        Long vasecurityIdFromStudyId = aclObjectRepository.findVasecurityIdFromStudyId(studyId);

        if (vasecurityIdFromStudyId == null) {
            throw new NotFoundException("StudyId " + studyId + " doesnt exist");
        }

        return getUserObjectIdentities(user, false).stream().
                filter(roi -> roi.thisAcuityType() && roi.getId().equals(vasecurityIdFromStudyId) && roi.getCanView()).findFirst();
    }
}
