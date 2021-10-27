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

/**
 * Created by Glen on 15/07/2015.
 */

(function () {
    'use strict';

    angular
        .module('acuityApp.myAnalyticsUserAccessControllers', [])
        .controller('GroupUserAccessController', GroupUserAccessController);

    GroupUserAccessController.$inject = ['$scope', '$log', '$filter', 'SecurityServices', 'Person', 'MyAnalyticsUtils', 'SecurityModel',
        'httpNotification', '$ngBootbox', 'ngDialog', '$q'];

    function GroupUserAccessController ($scope, $log, $filter, SecurityServices, Person, MyAnalyticsUtils, SecurityModel,
                                        httpNotification, $ngBootbox, ngDialog, $q) {

        /* jshint validthis: true */
        var group = this;

        /**
         * User groups in the security db
         */
        var trainedUserGroup = "TRAINED_USER_GROUP";
        var developmentTeamGroup = "DEVELOPMENT_GROUP";
        var acuitySupportGroup = "ACUITY_SUPPORT";

        group.MODEL = {
            loading: false,
            //Edit user filter
            searchUsersTerm: {
                name: '',
                showBy: 'name'
            },
            users: {
                trainedUsers: [], // this is the count for list of trained users - groupUsers
                groupUsers: [],
                allTrainedUsers: [] // all trainedUsers
            },
            groups: {
                selectedGroup: '',
                isEmpty: true,
                allGroups: []
            }
        };

        //group.getEditUserAccessInfo = getEditUserAccessInfo;
        group.onDropAddUserToSelectedGroup = onDropAddUserToSelectedGroup;
        group.onClickRemoveUserFromGroup = removeUserFromSelectedGroup;
        group.isCurrentUser = isCurrentUser;
        group.selectGroupHandler = selectGroupHandler;
        group.addGroup = addGroup;
        group.deleteCurrentGroup = deleteCurrentGroup;
        group.isCurrentGroupDeletable = isCurrentGroupDeletable;

        init();

        /////////////////////////////////////////////

        /**
         * Can the current group be deleted, static groups cant, user defined ones can
         */
        function isCurrentGroupDeletable () {
            var currentGroupName = group.MODEL.groups.selectedGroup.groupName;
            return !(currentGroupName === null || currentGroupName.toUpperCase() === developmentTeamGroup ||
            currentGroupName.toUpperCase() === trainedUserGroup ||
            currentGroupName.toUpperCase() === acuitySupportGroup || currentGroupName === '');
        }

        /**
         * Adds a group to the security db
         */
        function addGroup (groupName) {
            // validate
            if (groupName) {
                var index = _.findIndex(group.MODEL.groups.allGroups, function (group) {
                    return group.groupName === groupName;
                });
                if (index !== -1) {
                    httpNotification.info("Group name already exists", "Please type in another group name");
                } else {

                    return SecurityServices.createGroup(groupName).then(function (response) {
                        var groupWithLockdown = {
                            groupName: groupName,
                            inLockdownDataset: false
                        };
                        group.MODEL.groups.allGroups.push(groupWithLockdown);
                        selectGroupHandler(groupWithLockdown);
                    });
                }
            } else {
                httpNotification.info("Invalid (empty) group name", "Please type in a valid group name");
            }
        }

        /**
         * Removes a group to the security db
         */
        function deleteCurrentGroup () {
            var currentGroup = group.MODEL.groups.selectedGroup;

            $ngBootbox.customDialog({
                message: "Are you sure you want to delete " + currentGroup.groupName + " and all of " + currentGroup.groupName + "'s permissions?",
                title: "Delete permissions",
                buttons: {
                    no: {
                        label: "No",
                        className: "btn-default",
                        callback: function () {
                            return true;
                        }
                    },
                    yes: {
                        label: "Yes",
                        className: "btn-success",
                        callback: function () {
                            return SecurityServices.deleteGroup(currentGroup.groupName).then(function (response) {
                                var index = group.MODEL.groups.allGroups.indexOf(currentGroup);
                                if (index > -1) {
                                    group.MODEL.groups.allGroups.splice(index, 1);
                                }
                                selectGroupHandler(group.MODEL.groups.allGroups[0]);
                            });
                        }
                    }
                }
            });
        }

        /**
         * Gets all the groups from the server and sets the local model
         */
        function getAllGroups () {
            return SecurityServices.listAllGroupsWithLockdown().then(function (response) {
                group.MODEL.groups.allGroups = response.data;
                if (group.MODEL.groups.allGroups.length !== 0) {
                    group.MODEL.groups.selectedGroup = response.data[0];
                    group.MODEL.groups.isEmpty = false;
                } else {
                    group.MODEL.groups.isEmpty = true;
                }
            });
        }

        /**
         * Sets the selected group
         */
        function selectGroupHandler (groupItem) {
            if (groupItem) {
                group.MODEL.groups.selectedGroup = groupItem;
                group.MODEL.groups.isEmpty = false;
                $log.debug("group is " + groupItem);

                //now reload all the group
                group.MODEL.loading = true;
                group.MODEL.users.trainedUsers = _.cloneDeep(group.MODEL.users.allTrainedUsers);
                getAllUserForGroup().finally(function () {
                    group.MODEL.loading = false;
                    group.searchUsersTerm.name = ""; // remove all filtering
                });
            } else {
                group.MODEL.groups.selectedGroup = '';
                group.MODEL.groups.isEmpty = true;
            }
        }

        /**
         * Gets the user lists for the trained users and development team from the server
         */
        function getAllUsers () {

            group.MODEL.loading = true;
            group.MODEL.searchUsersTerm.name = "";

            return SecurityServices.getAllEnabledUsers().then(function (response) {

                group.MODEL.users.trainedUsers = _.map(response.data, function (user) {
                    return new Person(user.fullName, user.userId);
                });
                group.MODEL.users.allTrainedUsers = _.cloneDeep(group.MODEL.users.trainedUsers);
            });
        }

        /**
         * Gets the user lists for the trained users and development team from the server
         */
        function getAllUserForGroup () {
            return SecurityServices.getAllUserForGroup(group.MODEL.groups.selectedGroup.groupName).then(function (response) {
                group.MODEL.users.groupUsers = _.map(response.data, function (user) {
                    return new Person(user.fullName, user.userId);
                });
                MyAnalyticsUtils.checkIfArrayEmpty(group.MODEL.users.groupUsers);
                MyAnalyticsUtils.removeFromUsers(group.MODEL.users.groupUsers, group.MODEL.users.trainedUsers);
            });
        }

        /**
         * A Person object has ben dropped onto the group Team list, this needs to be sent to the server to add user
         * to the group in the database and then the person object added to the list
         *
         * @param {Person} $data person added to be added to the array
         * @param {Person} arrayTo of people in the list where the user is going to
         * @param {Person} arrayFrom of people in the list where the user is coming from
         * @returns {undefined}
         */
        function onDropAddUserToSelectedGroup ($data, arrayTo, arrayFrom) {

            checkLockDownConfirm(true).then(function (success) {
                group.MODEL.loading = true;

                SecurityServices.addUserToGroup($data.userId, group.MODEL.groups.selectedGroup.groupName).then(function (response) {
                    MyAnalyticsUtils.removeFromUsers([$data], arrayFrom); // remove from current list
                    MyAnalyticsUtils.addPersonToList($data, arrayTo);
                }).finally(function () {
                    group.MODEL.loading = false;
                    group.MODEL.searchUsersTerm.name = ""; // remove all filtering
                });
            }, function (cancel) {
            });
        }

        /**
         * A Person object has ben removed from the group list, this needs to be sent to the server to remove user
         * from the group in the database and then the person object added to the list
         *
         * @param {Person} $data person added to be added to the array
         * @param {Person} arrayTo of people in the list they are going to
         * @param {Person} arrayFrom of people in the list they have came from
         * @returns {undefined}
         */
        function removeUserFromSelectedGroup ($data, arrayTo, arrayFrom) {
            group.MODEL.loading = true;

            SecurityServices.removeUserFromGroup($data.userId, group.MODEL.groups.selectedGroup.groupName).then(function (response) {
                MyAnalyticsUtils.removeFromUsers([$data], arrayFrom); // remove from current list
                MyAnalyticsUtils.addPersonToList($data, arrayTo);
            }).finally(function () {
                group.MODEL.loading = false;
                group.searchUsersTerm.name = ""; // remove all filtering
            });
        }

        /**
         * Checks if the currently logged on person is the same as the person object passed in
         *
         * @param {Person} person object to check
         * @returns {Boolean} if the person object is the currently logged on user
         */
        function isCurrentUser (person) {
            return SecurityModel.getUserId() === person.userId;
        }

        /**
         * Presents a confirm message to the user when changing permission
         */
        function checkLockDownConfirm (granting) {

            var meassgeForGranting = granting ? 'add a user to' : 'remove a user from';
            var datasetName = (group.MODEL.groups.selectedGroup.datasetsInLockdown && group.MODEL.groups.selectedGroup.datasetsInLockdown.length > 0) ? group.MODEL.groups.selectedGroup.datasetsInLockdown[0] : 'dataset';
            if (group.MODEL.groups.selectedGroup.inLockdownDataset) {
                return ngDialog.openConfirm({
                    plain: true,
                    showClose: false,
                    className: ' ngdialog-theme-default custom-large-width theme-warning',
                    template: ' <div class="ngdialog-content-small">' +
                    '    <div class="ngdialog-message">' +
                    '        <h4 class="warning-dialog-title">Lockdown warning</h4>' +
                    '          <div class="row">' +
                    '           <div class="col-md-3">' +
                    '               <img src="images/warning.png" class="warning-dialog-image" width="100" height="100">' +
                    '           </div>' +
                    '           <div class="col-md-9">      ' +
                    '             You are about to ' + meassgeForGranting + ' ' + group.MODEL.groups.selectedGroup.groupName + '. ' + group.MODEL.groups.selectedGroup.groupName + ' has access to a ' + datasetName + ', which is in lockdown. <br/><br/>' +
                    'Granting a user access who is not on the Insiders List could result in criminal charges being brought against the system owner.<br/><br/>' +
                    'Ensure you have confirmation from the Drug Programme Data Owner or Authoriser the user is on the Insiders List before proceeding <br/><br/>' +
                    '<button ng-click="closeThisDialog()">Cancel</button> ' +
                    '<button ng-click="confirm()">Confirm</button> ' +
                    '           </div>' +
                    '       </div>' +
                    '   </div>' +
                    '</div>'
                });
            } else {
                var deferred = $q.defer();
                deferred.resolve('group not in lockdown, so ok');
                return deferred.promise;
            }
        }

        function init () {
            group.MODEL.loading = true;

            getAllGroups();
            getAllUsers().then(function () {
                getAllUserForGroup();
            })
                .finally(function () {
                    group.MODEL.loading = false;
                    group.searchUsersTerm.name = ""; // remove all filtering
                });
        }

        $scope.$on('$destroy', function () {
        });
    }
})();
