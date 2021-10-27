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

'use strict';

/* Controllers */

angular.module('acuityApp.myAnalyticsUserAccessControllers')

        /**
         * Controller for displaying the page for user access
         */
        .controller('UserAccessController',
                ['$scope', '$log', '$filter', 'SecurityServices', 'Person', "MyAnalyticsUtils", "SecurityModel",
                    function ($scope, $log, $filter, SecurityServices, Person, MyAnalyticsUtils, SecurityModel) {

                        $scope.MODEL = {
                            loading: false,
                            //Edit user filter
                            searchUsersTerm: {
                                name: "",
                                showBy: "name"
                            },
                            /**
                             * Used in counting the number of filtered search acls per search term
                             *
                             * @type {{developmentTeamUsers: Number, trainedUsers: Number}}
                             */
                            filteredCounts: {
                                developmentTeamUsers: 0,
                                trainedUsers: 0, // this is the count for list of trained users - developmentTeamUsers
                                allTrainedUsers: 0 // all trainedUsers
                            },
                            users: {
                                trainedUsers: [], // this is the count for list of trained users - developmentTeamUsers
                                developmentTeamUsers: [],
                                allTrainedUsers: [] // all trainedUsers
                            }
                        };

                        /**
                         * Gets the user lists for the trained users and development team from the server
                         */
                        $scope.getEditUserAccessInfo = function () {

                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";

                            SecurityServices.getAllEnabledUsers().then(function (response) {

                                $scope.MODEL.users.trainedUsers = _.map(response.data, function (user) {
                                    return new Person(user.fullName, user.userId);
                                });

                                SecurityServices.getAllDevelopmentTeamUsers().then(function (response) {
                                    $scope.MODEL.users.developmentTeamUsers = _.map(response.data, function (user) {
                                        return new Person(user.fullName, user.userId);
                                    });
                                    MyAnalyticsUtils.checkIfArrayEmpty($scope.MODEL.users.developmentTeamUsers);
                                    MyAnalyticsUtils.removeFromUsers($scope.MODEL.users.developmentTeamUsers, $scope.MODEL.users.trainedUsers);

                                    // set the default users count lengths
                                    $scope.MODEL.filteredCounts.trainedUsers = $scope.MODEL.users.trainedUsers.length;
                                    $scope.MODEL.filteredCounts.developmentTeamUsers = $scope.MODEL.users.developmentTeamUsers.length;
                                }).finally(function () {
                                    $scope.MODEL.loading = false;
                                });
                            });
                        };

                        /**
                         * Watch the model object, searchTerm.name and on any changes to it, apply the same filter as in the html
                         * and count the results.
                         *
                         * This is not ideal as its filtered twice, but the size shouldnt be large.
                         * I tried ng-repeat="dp in filteredDP  = (drugProgrammes | filterOr:searchTerm)' and then use
                         * filteredDP in the other page but it didnt work, seems different scopes.
                         */
                        var searchUserTermListener = $scope.$watch("MODEL.searchUsersTerm.name", function (text) {
                            refreshFilteredCounts();
                        });

                        /**
                         * Refresh the filtered counts
                         */
                        var refreshFilteredCounts = function () {
                            $log.debug("search " + $scope.MODEL.searchUsersTerm.name + " " + $scope.MODEL.searchUsersTerm.showBy);
                            $scope.MODEL.filteredCounts.trainedUsers = $filter("filterBySearchTerm")($scope.MODEL.users.trainedUsers, $scope.MODEL.searchUsersTerm).length;
                            $scope.MODEL.filteredCounts.developmentTeamUsers = $filter("filterBySearchTerm")($scope.MODEL.users.developmentTeamUsers, $scope.MODEL.searchUsersTerm).length;
                        };

                        /**
                         * A Person object has ben dropped onto another list, so this needs to be removed to the list they were in
                         *
                         * @param {Object of type person but without function isEmpty} person to be removed
                         * @param {Person} array to be removed from
                         * @returns {undefined}
                         */
                        $scope.dropSuccessHandler = function (person, array) {
                            var index = array.indexOf(person);
                            array.splice(index, 1);
                            MyAnalyticsUtils.checkIfArrayEmpty(array);
                            $log.info("Removed " + angular.toJson(person) + " from list");
                        };

                        /**
                         * A Person object has ben dropped onto the Dev Team list, this needs to be sent to the server to add user
                         * to the DEv TEam group in the database and then the person object added to the list
                         *
                         * @param {Person} $data person added to be added to the array
                         * @param {Person} array of people in the list
                         * @returns {undefined}
                         */
                        $scope.onDropAddUserToDevTeam = function ($data, array) {
                            $scope.MODEL.loading = true;

                            SecurityServices.addUserToDevTeamGroup($data.userId).then(function (response) {
                                MyAnalyticsUtils.addPersonToList($data, array);
                                refreshFilteredCounts();
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                                $scope.MODEL.searchUsersTerm.name = ""; // remove all filtering
                            });
                        };

                        /**
                         * A Person object has ben dropped onto the Trained User list, this needs to be sent to the server to remove user
                         * from the Dev Team group in the database and then the person object added to the list
                         *
                         * @param {Person} $data person added to be added to the array
                         * @param {Person} array of people in the list
                         * @returns {undefined}
                         */
                        $scope.onDropRemoveUserFromDevTeam = function ($data, array) {
                            $scope.MODEL.loading = true;

                            SecurityServices.removeUserFromDevTeamGroup($data.userId).then(function (response) {
                                MyAnalyticsUtils.addPersonToList($data, array);
                                refreshFilteredCounts();
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                                $scope.searchUsersTerm.name = ""; // remove all filtering
                            });
                        };

                        /**
                         * Checks if the currently logged on person is the same as the person object passed in
                         *
                         * @param {Person} person object to check
                         * @returns {Boolean} if the person object is the currently logged on user
                         */
                        $scope.isCurrentUser = function (person) {
                            return SecurityModel.getUserId() === person.userId;
                        };

                        var init = function () {
                            $scope.getEditUserAccessInfo();
                        };

                        $scope.$on('$destroy', function () {
                            searchUserTermListener();
                        });

                        init();
                    }
                ])

        /**
         * Controller for displaying the page for user access
         */
        .controller('IndividualUsersController',
                ['$scope', '$log', '$filter', 'SecurityServices', 'Person', "MyAnalyticsUtils", "SecurityModel", "SecurityAuth",
                    function ($scope, $log, $filter, SecurityServices, Person, MyAnalyticsUtils, SecurityModel, SecurityAuth) {

                        $scope.MODEL = {
                            loading: false,
                            //Edit user filter
                            searchUsersTerm: {
                                name: "",
                                showBy: "name"
                            },
                            searchInstanceTerm: "",
                            access: {
                                permitted: true,
                                denied: true
                            },
                            trainedUsers: [], // this is the list of trained users - developmentTeamUsers
                            filteredAcls: {}
                        };

                        $scope.selectUser = function (user) {
                            $scope.MODEL.loading = true;
                            $scope.selectedUser = user;
                            SecurityServices.getAllUserPermisions(user).then(function (response) {
                                transformUserAcls(response.data);
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        /**
                         * Gets the user lists for the trained users and development team from the server
                         */
                        $scope.getEditUserAccessInfo = function () {
                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";
                            $scope.MODEL.searchInstanceTerm = "";

                            SecurityServices.getAllEnabledUsers().then(function (response) {
                                $scope.MODEL.trainedUsers = _.map(response.data, function (user) {
                                    return new Person(user.fullName, user.userId);
                                });
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        var transformUserAcls = function (acls) {
                            if (!_.isEmpty(acls)) {
                                $scope.MODEL.aclsByStudyandDrugProgramme = {};
                                _.forEach(_.groupBy(acls, 'drugProgramme'), function (array, drugProgrammeName) {
                                    $scope.MODEL.aclsByStudyandDrugProgramme[drugProgrammeName] = _.groupBy(array, 'studyName');
                                });
                            } else {
                                $scope.MODEL.aclsByStudyandDrugProgramme = null;
                            }
                            $scope.filterAclBySearchTerm();
                        };

                        $scope.filterAclBySearchTerm = function () {
                            var searchTerm = $scope.MODEL.searchInstanceTerm.toLowerCase();
                            $scope.MODEL.filteredAcls = {};
                            _.forEach($scope.MODEL.aclsByStudyandDrugProgramme, function (drugProgramme, dpKey) {
                                $scope.MODEL.filteredAcls[dpKey] = {};
                                _.forEach(drugProgramme, function (study, name) {
                                    $scope.MODEL.filteredAcls[dpKey][name] = _.filter(study, function (acl) {
                                        return acl.datasetName.toLowerCase().indexOf(searchTerm) !== -1 ||
                                                acl.studyName.toLowerCase().indexOf(searchTerm) !== -1 ||
                                                acl.drugProgramme.toLowerCase().indexOf(searchTerm) !== -1;
                                    });
                                });
                            });
                            filterAclByPermission();
                        };

                        function filterAclByPermission() {
                            _.forEach($scope.MODEL.filteredAcls, function (drugProgramme, dpKey) {
                                _.forEach(drugProgramme, function (study, name) {
                                    $scope.MODEL.filteredAcls[dpKey][name] = _.filter(study, function (acl) {
                                        return acl.hasPermission && $scope.MODEL.access.permitted ||
                                                !acl.hasPermission && $scope.MODEL.access.denied;
                                    });
                                    if (_.isEmpty($scope.MODEL.filteredAcls[dpKey][name])) {
                                        delete $scope.MODEL.filteredAcls[dpKey][name];
                                    }
                                });
                                if (_.isEmpty($scope.MODEL.filteredAcls[dpKey])) {
                                    delete $scope.MODEL.filteredAcls[dpKey];
                                }
                            });
                        }

                        $scope.getRoleFromMask = function (permissionMask) {
                            return SecurityAuth.getRoleFromMask(permissionMask);
                        };

                        var init = function () {
                            $scope.getEditUserAccessInfo();
                        };

                        $scope.$on('$destroy', function () {
                        });

                        init();
                    }
                ])

        /**
         * Controller for displaying the page for no roles access
         */
        .controller('NoneTrainedUsersController',
                ['$scope', '$log', '$filter', 'SecurityServices', 'Person', "MyAnalyticsUtils",
                    function ($scope, $log, $filter, SecurityServices, Person, MyAnalyticsUtils) {

                        $scope.MODEL = {
                            loading: false,
                            //Edit user filter
                            searchUsersTerm: {
                                name: "",
                                showBy: "name"
                            },
                            /**
                             * Used in counting the number of filtered search acls per search term
                             *
                             * @type {{notTrainedUsers: Number}}
                             */
                            filteredCounts: {
                                notTrainedUsers: 0
                            },
                            users: {
                                notTrainedUsers: []
                            }
                        };

                        /**
                         * Gets the user lists for all the users without a role from the server
                         */
                        $scope.getNotTrainedUserAccessInfo = function () {

                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";

                            SecurityServices.getAllNoneTrainedUsers().then(function (response) {

                                $scope.MODEL.users.notTrainedUsers = _.map(response.data, function (user) {
                                    return new Person(user.fullName, user.userId);
                                });

                                MyAnalyticsUtils.checkIfArrayEmpty($scope.MODEL.users.notTrainedUsers);

                                // set the default users count lengths
                                $scope.MODEL.filteredCounts.notTrainedUsers = $scope.MODEL.users.notTrainedUsers.length;
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        /**
                         * Watch the model object, searchTerm.name and on any changes to it, apply the same filter as in the html
                         * and count the results.
                         *
                         * This is not ideal as its filtered twice, but the size shouldnt be large.
                         * I tried ng-repeat="dp in filteredDP  = (drugProgrammes | filterOr:searchTerm)' and then use
                         * filteredDP in the other page but it didnt work, seems different scopes.
                         */
                        var searchUserTermListener = $scope.$watch("MODEL.searchUsersTerm.name", function (text) {
                            refreshFilteredCounts();
                        });

                        /**
                         * Refresh the filtered counts
                         */
                        var refreshFilteredCounts = function () {
                            $log.debug("search " + $scope.MODEL.searchUsersTerm.name + " " + $scope.MODEL.searchUsersTerm.showBy);
                            $scope.MODEL.filteredCounts.notTrainedUsers = $filter("filterBySearchTerm")($scope.MODEL.users.notTrainedUsers, $scope.MODEL.searchUsersTerm).length;
                        };

                        /**
                         * Add the user as a trained user, then remove them from the list
                         *
                         * @param {Person} person
                         */
                        $scope.addAsTrainedUser = function (person) {
                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";

                            SecurityServices.addUserToTrainedUserGroup(person.userId).then(function (response) {

                                var index = _.findIndex($scope.MODEL.users.notTrainedUsers, {"userId": person.userId});
                                $scope.MODEL.users.notTrainedUsers.splice(index, 1);
                                MyAnalyticsUtils.checkIfArrayEmpty($scope.MODEL.users.notTrainedUsers);

                                refreshFilteredCounts();
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        var init = function () {
                            $scope.getNotTrainedUserAccessInfo();
                        };

                        $scope.$on('$destroy', function () {
                            searchUserTermListener();
                        });

                        init();
                    }
                ])
        /**
         * Controller for displaying the page in list view or in grid view
         */
        .controller('TrainedUsersController',
                ['$scope', '$log', '$filter', '$timeout', 'SecurityServices', 'Person', "MyAnalyticsUtils", "SecurityModel", "linkUsersDialog", "termsOfUseDialog",
                    "$q", "$ngBootbox",
                    function ($scope, $log, $filter, $timeout, SecurityServices, Person, MyAnalyticsUtils, SecurityModel, linkUsersDialog, termsOfUseDialog, $q, $ngBootbox) {

                        $scope.MODEL = {
                            loading: true,
                            //Edit user filter
                            searchUsersTerm: {
                                name: "",
                                showBy: "name"
                            },
                            /**
                             * Used to search for a new user, results shown in users list
                             */
                            newUsersTerm: {
                                name: "",
                                showBy: "name"
                            },
                            /*
                             * Used to specify the users fullName (new user with email only)
                             */
                            newUsersFullName: {
                                name: ""
                            },
                            /**
                             * Used in counting the number of filtered search acls per search term
                             *
                             * @type {{developmentTeamUsers: Number, trainedUsers: Number}}
                             */
                            filteredCounts: {
                                developmentTeamUsers: 0,
                                allTrainedUsers: 0, // all trainedUsers
                                newUsers: 0 // new users matching criteria in new users search input
                            },
                            users: {
                                developmentTeamUsers: [],
                                allTrainedUsers: [], // all trainedUsers
                                newUsers: [] // all trainedUsers
                            },
                            linkedUser: {
                                linkFromPerson: null, // user wanting to link from, passed into linkedUSer dialog
                                selectedLinkToPerson: null //selected user to link to
                            }
                        };

                        // Timeout to delay calls to the server between user entry of name for AD lookup
                        $scope.timeout = null;
                        $scope.canceller = null;

                        $scope.$on("$destroy", function () {
                            if ($scope.timeout) {
                                if ($scope.canceller !== null) {
                                    $scope.canceller.resolve();
                                }
                                $timeout.cancel($scope.timeout);
                            }
                        });

                        /**
                         * Gets the user lists for the trained users from the server
                         */
                        $scope.getEditUserAccessInfo = function () {

                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";

                            SecurityServices.getAllEnabledUsers().then(function (response) {

                                $scope.MODEL.users.allTrainedUsers = _.map(response.data, function (user) {
                                    if (user.linkeduser) {
                                        var linkedPerson = new Person(user.linkeduser.fullName, true, user.linkeduser.userId);
                                        return new Person(user.fullName, user.userId, true, linkedPerson);
                                    } else {
                                        return new Person(user.fullName, user.userId);
                                    }
                                });

                                //$scope.MODEL.users.allTrainedUsers = _.cloneDeep($scope.MODEL.users.trainedUsers);

                                SecurityServices.getAllDevelopmentTeamUsers().then(function (response) {
                                    $scope.MODEL.users.developmentTeamUsers = _.map(response.data, function (user) {
                                        return new Person(user.fullName, user.userId);
                                    });
                                    MyAnalyticsUtils.checkIfArrayEmpty($scope.MODEL.users.developmentTeamUsers);

                                    // set the default users count lengths
                                    $scope.MODEL.filteredCounts.developmentTeamUsers = $scope.MODEL.users.developmentTeamUsers.length;
                                    $scope.MODEL.filteredCounts.allTrainedUsers = $scope.MODEL.users.allTrainedUsers.length;
                                }).finally(function () {
                                    $scope.MODEL.loading = false;
                                });
                            });
                        };

                        /**
                         * Watch the model object, searchTerm.name and on any changes to it, apply the same filter as in the html
                         * and count the results.
                         *
                         * This is not ideal as its filtered twice, but the size shouldnt be large.
                         * I tried ng-repeat="dp in filteredDP  = (drugProgrammes | filterOr:searchTerm)' and then use
                         * filteredDP in the other page but it didnt work, seems different scopes.
                         */
                        var searchUserTermListener = $scope.$watch("MODEL.searchUsersTerm.name", function (text) {
                            refreshFilteredCounts();
                        });

                        /**
                         * Watch the model object, newIUsersTerm.name and on any changes to it, check if content contains @ (an email eddress)
                         * and if so do nothing, otherwise refresh new users list with matching name value.
                         * Functionality now in triggerChanged so we can delay and cancel concurrent events where user is still typing.
                         *
                         */
                        var newUserTermListener = $scope.$watch("MODEL.newUsersTerm.name", function (text) {
                            $log.debug("new user " + $scope.MODEL.newUsersTerm.name + " " + $scope.MODEL.newUsersTerm.showBy);
                            triggerChanged();
                        });

                        /**
                         * A Person object has ben dropped onto another list, so this needs to be removed to the list they were in
                         *
                         * @param {Object of type person but without function isEmpty} person to be removed
                         * @param {Person} array to be removed from
                         * @returns {undefined}
                         */
                        $scope.dropSuccessHandler = function (person, array) {
                            var index = array.indexOf(person);
                            array.splice(index, 1);
                            MyAnalyticsUtils.checkIfArrayEmpty(array);
                            $log.info("Removed " + angular.toJson(person) + " from list");
                        };

                        /**
                         * Refresh the filtered counts
                         */
                        var refreshFilteredCounts = function () {
                            $log.debug("search " + $scope.MODEL.searchUsersTerm.name + " " + $scope.MODEL.searchUsersTerm.showBy);
                            $scope.MODEL.filteredCounts.allTrainedUsers = $filter("filterBySearchTerm")($scope.MODEL.users.allTrainedUsers, $scope.MODEL.searchUsersTerm).length;
                        };

                        /**
                         * A Person object has ben dropped onto the Trained User list, this needs to be sent to the server to add user
                         * to the Trained User group in the database and then the person object added to the list
                         *
                         * @param {Person} $data person added to be added to the array
                         * @param {Person} array of people in the list
                         * @returns {undefined}
                         */
                        $scope.onDropAddUserToTrainedUsers = function ($data, array) {
                            $scope.MODEL.loading = true;

                            // we might get a 404 error here,
                            SecurityServices.addExistingUserToTrainedUserGroup($data.userId).then(
                                    function (response) {
                                        MyAnalyticsUtils.addPersonToList($data, array);
                                        refreshFilteredCounts();
                                    },
                                    function (error) {
                                        if (error.status === 404) { // handle the 404 and send to alternative endpoint
                                            $log.info('User ' + $data.userId + ' was not found attempting to create user');
                                            SecurityServices.addNewUserToTrainedUserGroup($data.userId, $data.name).then(function (response) {
                                                MyAnalyticsUtils.addPersonToList($data, array);
                                            });

                                        }
                                    }).finally(function () {
                                $scope.MODEL.loading = false;
                                $scope.MODEL.searchUsersTerm.name = ""; // remove all filtering                        
                                $scope.MODEL.newUsersTerm.name = ""; // remove previous serach
                            });
                        };

                        /**
                         * A Person object has ben clicked on it's remove link, so
                         * show a dialog to ask them if they want to delete all user aces, then call method removeUserFromTrainedUserGroup
                         *
                         * @param {Person} $data person added to be added to the array
                         * @param {Person} array of people in the list
                         * @returns {undefined}
                         */
                        $scope.onClickRemoveUserFromTrainedUsers = function ($data, array) {

                            $ngBootbox.customDialog({
                                message: "Do you want to delete all of " + $data.name + "'s permissions and \ndelete the user from all the groups aswell?",
                                title: "Delete permissions",
                                buttons: {
                                    no: {
                                        label: "No",
                                        className: "btn-default",
                                        callback: function () {
                                            removeUserFromTrainedUserGroup($data, array, false);
                                        }
                                    },
                                    yes: {
                                        label: "Yes",
                                        className: "btn-success",
                                        callback: function () {
                                            removeUserFromTrainedUserGroup($data, array, true);
                                        }
                                    }
                                }
                            });
                        };

                        /**
                         * Removes a user from the Trained User group in the database and then the person object removed from the list,
                         * also optionally remove all their aces
                         *
                         * @param {Person} $data person added to be added to the array
                         * @param {Person} array of people in the list
                         * @param {booelan} deleteAces delete all user aces
                         * @returns {undefined}
                         */
                        function removeUserFromTrainedUserGroup($data, array, deleteAces) {
                            $scope.MODEL.loading = true;

                            SecurityServices.removeUserFromTrainedUserGroup($data.userId, deleteAces).then(function (response) {

                                MyAnalyticsUtils.removeFromUsers([$data], array);
                                refreshFilteredCounts();

                            }).finally(function () {
                                $scope.MODEL.loading = false;
                                $scope.MODEL.searchUsersTerm.name = ""; // remove all filtering
                                $scope.MODEL.newUsersTerm.name = ""; // remove previous serach
                            });
                        }

                        /**
                         * User has clicked add person, so add persons details to the database from the new users form fields
                         *
                         * @returns {undefined}
                         */
                        $scope.onClickAddUserToTrainedUsers = function (email, name, array) {

                            $scope.MODEL.loading = true;

                            SecurityServices.addNewUserToTrainedUserGroup(email, name).then(function (response) {

                                if (response.status === 201 || response.status === 200) {
                                    $scope.MODEL.newUsersFullName.name = '';
                                    $scope.MODEL.newUsersTerm.name = '';

                                    MyAnalyticsUtils.addPersonToList(new Person(name, email), array);
                                    refreshFilteredCounts();
                                }

                            }).finally(function () {
                                $scope.MODEL.loading = false;
                                $scope.MODEL.searchUsersTerm.name = ""; // remove all filtering
                            });
                        };

                        var triggerChanged = function () {

                            if (!$scope.MODEL.newUsersTerm.name) {
                                MyAnalyticsUtils.removeAllUsers($scope.MODEL.users.newUsers);
                                return;
                            }

                            if ($scope.MODEL.newUsersTerm.name.indexOf("@") === -1 && $scope.MODEL.newUsersTerm.name) {

                                if ($scope.timeout !== null) {
                                    if ($scope.canceller !== null) {
                                        $scope.canceller.resolve("timeout cancelled next request");
                                    }
                                    $timeout.cancel($scope.timeout);
                                    $scope.timeout = null;
                                }

                                $scope.timeout = $timeout(function () { // outer timeout waits for user input to stop

                                    if (!$scope.MODEL.newUsersTerm.name) {
                                        return;
                                    }

                                    $scope.MODEL.loading = true;
                                    var result = SecurityServices.getAllMatchingUsers($scope.MODEL.newUsersTerm.name);

                                    $scope.canceller = result.canceller;
                                    result.promise.finally(function () {
                                        $scope.canceller = null;
                                    });

                                    $timeout(function () { // inner timeout waiting for server response to abort hanging connections
                                        if ($scope.canceller !== null) {
                                            $scope.canceller.resolve("timeout cancelled request too long");
                                            $scope.canceller = null;
                                            triggerChanged();
                                        }
                                    }, 5000);

                                    result.promise.then(function (response) {

                                        $scope.MODEL.loading = true;
                                        if (response && response.data && response.status === 200) {
                                            var records = angular.fromJson(response.data);

                                            if (records) {
                                                $scope.MODEL.users.newUsers = _.map(response.data, function (user) {
                                                    return new Person(user.displayName, user.cn);
                                                });

                                                for (var i = 0; i < records.length; i++) {
                                                    $log.debug("record : " + records[i].displayName + " (" + records[i].cn + ")");
                                                }
                                            }
                                        }
                                    }).finally(function () {
                                        $scope.MODEL.loading = false;
                                    });
                                }, 500);
                            }
                        };

                        /**
                         * Checks if the currently logged on person is the same as the person object passed in
                         *
                         * @param {Person} person object to check
                         * @returns {Boolean} if the person object is the currently logged on user
                         */
                        $scope.isCurrentUser = function (person) {
                            return SecurityModel.getUserId() === person.userId;
                        };

                        /**
                         * Checks person is already in the trained user group
                         *
                         * @param {Person} person object to check
                         * @returns {Boolean} if the person objectin the trained user group
                         */
                        $scope.isAlreadyInTrainedUserList = function (person) {
                            return _.some($scope.MODEL.users.allTrainedUsers, function (user) {
                                return person.userId === user.userId;
                            });
                        };

                        //
                        //  Linked user methods
                        //
                        /**
                         * Opens the link user dialog, passes in the $scope
                         *
                         * @param {Person} person current person to link from
                         */
                        $scope.openLinkUserDialog = function (person) {
                            $scope.MODEL.linkedUser.linkFromPerson = person;
                            $scope.MODEL.linkedUser.selectedLinkToPerson = null;
                            $scope.MODEL.linkedUser.linkToPerson = null;

                            linkUsersDialog.openModal($scope);
                        };

                        /**
                         * Angular filter used in typeahead in html
                         *
                         * @param {String} state current item to do search against
                         * @param {String} viewValue search term
                         */
                        $scope.contains = function (state, viewValue) {
                            if (state && _.isString(state)) {
                                return _.contains(state.toLowerCase(), viewValue.toLowerCase());
                            } else if (state && state.nameAndId) { //if person
                                return _.contains(state.nameAndId.toLowerCase(), viewValue.toLowerCase());
                            } else {
                                return false;
                            }
                        };

                        /**
                         * Sets the selectedLinkToPerson to the person selected on the drop down
                         *
                         * @param {Person} person to be linked
                         */
                        $scope.onTypeHeadSelect = function (person) {
                            $scope.MODEL.linkedUser.selectedLinkToPerson = person;
                        };

                        /**
                         * unlinks a person
                         *
                         * @param {Person} person to be unlinked
                         */
                        $scope.unlink = function (person) {
                            $scope.MODEL.loading = true;

                            SecurityServices.unLinkUser(person.userId).then(function (response) {
                                person.linkedPerson = undefined;
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        /**
                         * links a person to anthor person
                         *
                         * @param {Person} personFrom to be linked from
                         * @param {Person} personTo to be linked to
                         */
                        $scope.link = function (personFrom, personTo) {
                            $scope.MODEL.loading = true;

                            return SecurityServices.linkUser(personFrom.userId, personTo.userId).then(function (response) {
                                personFrom.linkedPerson = personTo;
                                return true;
                            }, function (response) {
                                return false;
                            }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        };

                        /**
                         * Determines if a user has been selected on the link users dialog
                         *
                         * @returns {Boolean} if user has been selected
                         */
                        $scope.hasSelectedLinkedUser = function () {
                            if ($scope.MODEL.linkedUser.selectedLinkToPerson) {
                                return true;
                            } else {
                                return false;
                            }
                        };

                        var init = function () {
                            $scope.getEditUserAccessInfo();
                        };

                        $scope.$on('$destroy', function () {
                            searchUserTermListener();
                            newUserTermListener();
                        });

                        init();

                    }]);
