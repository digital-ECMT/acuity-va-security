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

package com.acuity.va.auditlogger.service;

import com.acuity.va.auditlogger.dao.LockdownRepository;
import com.acuity.va.auditlogger.domain.DataOwner;
import com.acuity.va.auditlogger.domain.Group;
import com.acuity.va.auditlogger.domain.LockdownAccessHistory;
import com.acuity.va.auditlogger.domain.LockdownPeriod;
import com.acuity.va.auditlogger.domain.LogOperationEntity;
import com.acuity.va.auditlogger.domain.AcuityObjectIdentityForAudit;
import com.acuity.va.auditlogger.domain.RoiGrantedAccess;
import com.acuity.va.auditlogger.domain.User;
import com.acuity.va.auditlogger.domain.UserAccessItem;
import com.acuity.va.auditlogger.domain.UserWithDate;
import com.acuity.va.security.common.service.PeopleResourceClient;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(value = "security", readOnly = true)
public class LockdownService {

    private static final Logger LOG = LoggerFactory.getLogger(LockdownService.class);

    @Autowired
    private LockdownRepository lockdownRepository;

    @Autowired
    private PeopleResourceClient peopleResourceClient;

    public String getLatestLockdown(AcuityObjectIdentityForAudit roi, AcuityObjectIdentityForAudit parent) throws IOException {
        List<LockdownPeriod> lockdowns = lockdownRepository.getLockdowns(roi);

        if (!CollectionUtils.isEmpty(lockdowns)) {
            LockdownAccessHistory history = generateHistory(roi, parent, lockdowns.get(0));

            return saveToCSV(history);
        } else {
            return null;
        }
    }

    /**
     * Hack at the moment
     */
    public String saveToCSV(LockdownAccessHistory lockdownAccessHistory) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Dataset," + lockdownAccessHistory.getRoi().getName() + ",,,\n");
        sb.append(",,,,\n");
        sb.append("Lockdown start," + lockdownAccessHistory.getLockdownPeriod().getStartDate().toString() + ",,,\n");
        sb.append("Lockdown finish," + lockdownAccessHistory.getLockdownPeriod().getEndDate().toString() + ",,,\n");
        if (lockdownAccessHistory.getRemovedLockdownUser() != null) {
            sb.append("Lockdown removed by," + escapeAndifNull(lockdownAccessHistory.getRemovedLockdownUser()) + ",,,\n");
        }
        sb.append(",,,,\n");
        sb.append("User,Date,Administrator,Data owner,Authorisors\n");

        for (UserAccessItem item : lockdownAccessHistory.getUserAccessItems()) {

            String authorisors = Joiner.on("; ").join(item.getAuthorisors());

            sb.append(escapeAndifNull(item.getUser()) + ",");
            sb.append(item.getAccessDateTime() + ",");
            sb.append(escapeAndifNull(item.getAdministrator()) + ",");
            sb.append(escapeAndifNull(item.getDataOwner()) + ",");
            sb.append(escapeAndifNull(authorisors) + "\n");
        }
        return sb.toString();
    }

    private String escapeAndifNull(Object cellValue) {
        if (cellValue == null) {
            return "";
        }

        return StringEscapeUtils.escapeCsv(cellValue.toString());
    }

    public LockdownAccessHistory generateHistory(AcuityObjectIdentityForAudit roi, AcuityObjectIdentityForAudit parentRoi, LockdownPeriod lockdownPeriod) {

        List<LogOperationEntity> views = lockdownPeriod.getLogOperations().stream().filter(logOp -> logOp.getName().equals("STUDY_METADATA")).collect(toList());
        LockdownAccessHistory lockdownAccessHistory = new LockdownAccessHistory();
        lockdownAccessHistory.setLockdownPeriod(lockdownPeriod);
        lockdownAccessHistory.setRoi(roi);

        User removedLockdownUser = lockdownRepository.getWhoRemovedLockdown(roi, DateUtils.addMinutes(lockdownPeriod.getEndDate(), 1));
        if (removedLockdownUser != null) {
            String removedLockdownUserFullName = getFullName(removedLockdownUser.getPrid());
            removedLockdownUser.setFullName(removedLockdownUserFullName);
        }

        lockdownAccessHistory.setRemovedLockdownUser(removedLockdownUser);

        views.forEach(view -> {
            LOG.info("" + view);

            User viewUser = null;
            User dataownerUser = null;
            List<User> authorisorsUsers = newArrayList();

            User accessGrantedBy = whoGaveAccess(roi, parentRoi, view.getOwner(), view.getDateCreated());

            String viewFullName = getFullName(view.getOwner());
            viewUser = new User(view.getOwner(), viewFullName);

            DataOwner dataownerPrid = lockdownRepository.getDataowner(parentRoi, accessGrantedBy.getGrantedTime());

            if (dataownerPrid != null) {
                boolean hasBeenRemoved = lockdownRepository.
                        hasBeenRemovedAsDataOwner(parentRoi, dataownerPrid.getCreated(), accessGrantedBy.getGrantedTime(), dataownerPrid.getName());

                if (!hasBeenRemoved) {
                    String dataownerFullName = getFullName(dataownerPrid.getName());

                    dataownerUser = new User(dataownerPrid.getName(), dataownerFullName);
                }
            }

            List<String> authorisorsPrids = lockdownRepository.getAuthorisors(parentRoi, accessGrantedBy.getGrantedTime());

            authorisorsUsers = authorisorsPrids.stream().filter(prid -> prid != null).map(aPrid -> {
                String fullName = getFullName(aPrid);

                return new User(aPrid, fullName);

            }).collect(toList());

            lockdownAccessHistory.addUserAccessItem(new UserAccessItem(viewUser, view.getDateCreated(), accessGrantedBy, authorisorsUsers, dataownerUser));
        });

        return lockdownAccessHistory;
    }

    public User whoGaveAccess(AcuityObjectIdentityForAudit roi, AcuityObjectIdentityForAudit parentRoi, String user, Date dateAccessed) {
        // find out who gave the user the access
        List<RoiGrantedAccess> userAccess = lockdownRepository.getWhosGotAccess(roi, dateAccessed, user);
        RoiGrantedAccess userGrantedAccess = userAccess.isEmpty() ? null : userAccess.get(0);

        List<RoiGrantedAccess> parentUserAccess = lockdownRepository.getWhosGotAccess(parentRoi, dateAccessed, user);
        RoiGrantedAccess userParentGrantedAccess = parentUserAccess.isEmpty() ? null : parentUserAccess.get(0);

        // find out who gave the user the access by group
        List<Group> groupsForOwner = lockdownRepository.getGroups(dateAccessed, user, null);

        List<RoiGrantedAccess> groupAccess = lockdownRepository.getWhosGotAccess(roi, dateAccessed).stream().filter(a -> a.isGroup()).collect(toList());

        // groupsWithOwnerIn is a list of groups that that owner is in at the time of access
        // groupsWithOwnerIn give the date and who granted it to the dataset        
        List<RoiGrantedAccess> groupsWithOwnerIn = groupAccess.stream()
                .filter(ga -> groupsForOwner.stream().anyMatch(gfo -> gfo.getName().equals(ga.getSid()))).collect(toList());

        swapGrantedByForGroups(groupsWithOwnerIn, groupsForOwner, user);

        List<RoiGrantedAccess> parentGroupAccess = lockdownRepository.getWhosGotAccess(parentRoi, dateAccessed).stream()
                .filter(RoiGrantedAccess::isGroup).collect(toList());
        List<RoiGrantedAccess> parentGroupsWithOwnerIn = parentGroupAccess.stream()
                .filter(ga -> groupsForOwner.stream().anyMatch(gfo -> gfo.getName().equals(ga.getSid()))).collect(toList());

        swapGrantedByForGroups(parentGroupsWithOwnerIn, groupsForOwner, user);

        // need to find first has access for group
        List<RoiGrantedAccess> addGroupsWithOwnerIn = ListUtils.union(groupsWithOwnerIn, parentGroupsWithOwnerIn);
        RoiGrantedAccess earliestGroupRoiGrantedAccess = findEarliest(addGroupsWithOwnerIn, groupsForOwner, user);
        RoiGrantedAccess earliestUserGrantedAccess = findEarliest(userGrantedAccess, userParentGrantedAccess);

        return getEarliestGrantedByUser(earliestUserGrantedAccess, earliestGroupRoiGrantedAccess);
    }

    private String getFullName(String prid) {
        try {
            return peopleResourceClient.getFullName(prid);
        } catch (Exception ex) {
            LOG.error("Unable to get full name from people service", ex);
            return "Unknown Fullname";
        }
    }

    /**
     * if the user is added to the group after the group is given access we need to user the user added to group date and granted by
     * <p>
     * need to work out if the user was added to the group before/after the group was added to view the dataset
     */
    private void swapGrantedByForGroups(List<RoiGrantedAccess> groupsWithOwnerIn, List<Group> groupsForOwner, String user) {
        // need to work out if the user was added to the group before/after the group was added to view the dataset
        groupsWithOwnerIn.forEach(g -> {
            Group group = groupsForOwner.stream().filter(og -> og.getName().equals(g.getSid())).findFirst().get();
            UserWithDate userAddedToGroupWithDate = group.getUsers().stream().filter(u -> u.getPrid().equals(user)).findFirst().get();

            // if user added after the group added permission swap time and granted by
            if (userAddedToGroupWithDate.getAdded().after(g.getDate())) {
                g.setDate(userAddedToGroupWithDate.getAdded());
                g.setGrantedBy(userAddedToGroupWithDate.getGrantedBy());
            }
        });
    }

    private User getEarliestGrantedByUser(RoiGrantedAccess userGrantedAccess, RoiGrantedAccess earliestGroupRoiGrantedAccess) {
        RoiGrantedAccess accessGranted;

        if (userGrantedAccess != null) {
            if (earliestGroupRoiGrantedAccess == null || userGrantedAccess.getDate().before(earliestGroupRoiGrantedAccess.getDate())) {
                accessGranted = userGrantedAccess;
            } else {
                accessGranted = earliestGroupRoiGrantedAccess;
            }
        } else if (earliestGroupRoiGrantedAccess == null) {
            return null;
        } else {
            accessGranted = earliestGroupRoiGrantedAccess;
        }

        String accessGrantedByFullName = getFullName(accessGranted.getGrantedBy());
        return new User(accessGranted.getGrantedBy(), accessGrantedByFullName, accessGranted.getDate());
    }

    private RoiGrantedAccess findEarliest(RoiGrantedAccess userGrantedAccess, RoiGrantedAccess parentUserGrantedAccess) {
        if (userGrantedAccess != null) {
            if (parentUserGrantedAccess == null || userGrantedAccess.getDate().before(parentUserGrantedAccess.getDate())) {
                return userGrantedAccess;
            } else {
                return parentUserGrantedAccess;
            }
        } else if (parentUserGrantedAccess == null) {
            return null;
        } else {
            return parentUserGrantedAccess;
        }
    }

    private RoiGrantedAccess findEarliest(List<RoiGrantedAccess> groupsWithOwnerIn, List<Group> groupsForOwner, String ownerPrid) {
        // need to find first has access for group
        return groupsWithOwnerIn.stream().map(g -> {
            // 1) if user added to the group after the group added to study, then use user added to group time
            // 2) if user added to the group before the group added to study, then use the group added time

            Date groupAddedDate = g.getDate();
            Group groupList = groupsForOwner.stream().filter(gitem -> gitem.getName().equals(g.getSid())).findFirst().get();
            UserWithDate userAddedToGroupDate = groupList.getUsers().stream().filter(u -> u.getPrid().equals(ownerPrid)).findFirst().get();

            if (groupAddedDate.before(userAddedToGroupDate.getAdded())) {
                g.setDate(userAddedToGroupDate.getAdded()); // 2)
            } else {
                g.setDate(groupAddedDate); // 1)
            }

            return g;
        }).sorted((g1, g2) -> g1.getDate().compareTo(g2.getDate())).findFirst().orElse(null);
    }
}
