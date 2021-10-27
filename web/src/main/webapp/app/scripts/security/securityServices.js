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
/* Services */
const  DEVELOPMENT_TEAM = 522240;
const DRUG_PROGRAMME_DATA_OWNER = 450575;
const AUTHROISERS = 442383;
const ADMINISTRATOR = 32783;
angular.module('acuityApp.securityServices', [])
        /**
         * Security Model, this interacts with the REST security services using the session
         * information to pass to java to identify the user and a list of acls.
         *
         * Exposed the following to the application
         *
         * username = Username from the security context with the server
         * acls = list of acls for the user
         * list of drug programmes, studies and datasets
         */
        .factory('SecurityModel', ['$http', '$log', 'httpNotification', 'AcuityAclObjectEnum',
            function ($http, log, httpNotification, AcuityAclObjectEnum) {

                // userId and fullname from the server
                var userId = null;
                var fullName = null;
                var authorities = [];
                // List of ACLs for user
                //Put in for performance reasons as acl query was slow on oracle
                var acls = [];
                var viewPackagePermissons = [];
                var viewExtraPackagePermissons = [];
                var acuityPermissions = [];
                /**
                 * Filters and sorts the list of acls
                 */
                var filterAndSortAcls = function (supertype) {
                    return _.chain(acls).
                            filter(function (acl) {
                                return acl.supertype === supertype; // ie com.acuity.acuity.security.acl.domain.ClinicalStudy
                            }).
                            sortBy('name').
                            value();
                };
                var service = {
                    //expose vars
                    getFullName: function () {
                        return fullName;
                    },
                    getUserId: function () {
                        return userId;
                    },
                    getAuthorities: function () {
                        return authorities;
                    },
                    getAcls: function () {
                        return acls;
                    },
                    getViewPackagePermissons: function() {
                        return viewPackagePermissons;
                    },
                    getViewExtraPackagePermissons: function() {
                        return viewExtraPackagePermissons;
                    },
                    getAcuityPermissions: function() {
                        return acuityPermissions;
                    },
                    /**
                     * Initialise the username from the server.
                     * On success of the rest call, return a $promise that returns the user
                     *
                     * @return $promise
                     */
                    initWhoami: function () {
                        var promise = $http.get('/resources/my/user/whoami', {cache: true})
                                .success(function (data) {
                                    log.info("Username from the server is " + data.userId + ", fullName " + data.fullName);
                                    userId = data.userId;
                                    fullName = data.fullName;
                                    authorities = data.authoritiesAsString;
                                    return userId;
                                }).error(function (data, status) {
                            httpNotification.error("Error establishing your user details", status);
                        });
                        return promise;
                    },
                    /**
                     * Initialise the username from the server.
                     * On success of the rest call, return a $promise that returns the user
                     *
                     * @return $promise
                     */
                    initPermissionsConfiguration: function () {
                        var promise = $http.get('/resources/acl/viewextrapackages', {cache: true})
                            .success(function (data) {
                                viewPackagePermissons = data.view;
                                viewExtraPackagePermissons = data.extra;
                                acuityPermissions = data.all;
                                return data;
                            }).error(function (data, status) {
                                httpNotification.error("Error establishing plugins configuration", status);
                            });
                        return promise;
                    },
                    /*
                     * Initialise the list of acls the user can see from the server
                     * On sucess of the rest call, return a $promise that returns list of acls on sucesss
                     *
                     * @return $promise
                     */
                    initAcls: function () {
                        var promise = $http.get('/resources/my/aclswithoutauthorisedusers', {cache: true})
                                .success(function (data) {
                                    acls = data;
                                    return data; // list of acls for user
                                }).error(function (data, status) {
                            httpNotification.error("Error getting your access lists", status);
                        });
                        return promise;
                    },
                    /**
                     * Lists all the acls of type drug programme and sorts alphabetically
                     *
                     * @returns Drug Programmes user has access to
                     */
                    listDrugProgrammes: function () {
                        return filterAndSortAcls(AcuityAclObjectEnum.DrugProgramme.superClassName);
                    },
                    /**
                     * Lists all the acls of type clinical studies and sorts alphabetically
                     *
                     * @returns Clinical studies user has access to
                     */
                    listClinicalStudies: function () {
                        return filterAndSortAcls(AcuityAclObjectEnum.ClinicalStudy.superClassName);
                    },
                    /**
                     * Lists all the acls of type Dataset and sorts alphabetically
                     *
                     * @returns Datasets user has access to
                     */
                    listDatasets: function () {
                        return filterAndSortAcls(AcuityAclObjectEnum.AcuityDataset.superClassName);
                    },
                    clearApplicationCache: function () {
                        var params = {
                            userId: userId
                        };
                        return $http.post('/resources/my/refresh', params)
                                .success(function (data, status) {
                                    if (data) {
                                        httpNotification.info('Cache clear in already in progress', status);
                                    } else {
                                        httpNotification.success('Cache cleared successfully', '');
                                    }
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error('Error clearing cache', status);
                        });
                    }
                };
                return service;
            }])

        /**
         * Security Services, these are the other services that need to interact with the security rest services
         */
        .factory('SecurityServices', ['$http', '$log', '$q', 'httpNotification', 'AcuityAclObjectEnum', 'Person',
            function ($http, log, $q, httpNotification, AcuityAclObjectEnum, Person) {
                /**
                 * User groups in the security db
                 */
                var trainedUserGroup = "TRAINED_USER_GROUP";
                var developmentTeamGroup = "DEVELOPMENT_GROUP";
                var service = {
                    /**
                     * Gets the datasetId lockdown info
                     *
                     * @returns dataset lockdown info user has access to
                     */
                    getLockdownAccess: function (acuityAclObjectEnum, datasetId) {
                        var promise = $http.get('/resources/logging/' + acuityAclObjectEnum.name + '/' + datasetId + '/lockdown')
                                .success(function (data, status, headers) {
                                    log.info("Get dataset info " + datasetId + ": " + data);
                                    return data;
                                }).error(function (data, status) {
                            if (status === 404) {
                                httpNotification.info("No lockdown information for this Dataset", "Dataset " + datasetId);
                            } else {
                                httpNotification.error("Error getting Lockdown Dataset information", status, "Dataset " + datasetId);
                            }
                        });
                        return promise;
                    },
                    /**
                     * Gets the drug programme info
                     *
                     * @returns Drug Programmes user has access to
                     */
                    getDrugProgramme: function (acuityAclObjectEnum, drugProgrammeId) {
                        var promise = $http.get('/resources/myanalytics/DrugProgramme/' + acuityAclObjectEnum.name + '/' + drugProgrammeId)
                                .success(function (data) {
                                    log.info("Get drug programme info " + drugProgrammeId + ": " + data);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Error getting Drug Programme information", status, "Drug Programme " + drugProgrammeId);
                        });
                        return promise;
                    },
                    /**
                     * Gets the clinical study info
                     *
                     * @returns Clinical study user has access to
                     */
                    getClinicalStudy: function (acuityAclObjectEnum, clinicalStudyId) {
                        var promise = $http.get('/resources/myanalytics/ClinicalStudy/' + acuityAclObjectEnum.name + '/' + clinicalStudyId)
                                .success(function (data) {
                                    log.info("Get clinical study info " + clinicalStudyId + ": " + data);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Error getting Clinical Study information", status, "Clinical Study " + clinicalStudyId);
                        });
                        return promise;
                    },
                    /**
                     * Gets the dataset info
                     *
                     * @returns dataset user has access to
                     */
                    getDataset: function (acuityAclObjectEnum, datasetId) {
                        var promise = $http.get('/resources/myanalytics/Dataset/' + acuityAclObjectEnum.name + '/' + datasetId)
                                .success(function (data, status, headers) {
                                    log.info("Get dataset info " + datasetId + ": " + data);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Error getting Dataset information", status, "Dataset " + datasetId);
                        });
                        return promise;
                    },
                    /**
                     * Adds/Swaps a permission to the AcuityObjectIdentityEnum.  Overwrites the current permission if there is
                     * one for this user
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {Person} person to swap permission
                     * @param {Integer} permissionMask mask of the newPermission
                     * @param {Boolean} granting/revoking a permission
                     */
                    swapUserPermission: function (acuityAclObjectEnum, acuityAclObjectId, person, permissionMask, granting) {
                        if (!Person.isActivePermission(person)) {
                            throw "Cant swap person who isnt active " + person;
                        }
                        var postData = {
                            acuitySidDetails: Person.toAcuitySidDetails(person),
                            //WRONG
                            permissionMask: permissionMask,
                            granting: granting
                        };
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/ace', postData)
                                .success(function (data, status) {
                                    log.info("adding/swapping user permission " + permissionMask + " granted? " + granting + " to " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to update user permissions for " + person.userId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Swaps all the users permissions to the AcuityObjectIdentityEnum.  Overwrites the current permission if there is
                     * one for this user
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {Person} personArray array of persons to swap
                     * @param {Integer} permissionMask mask of the newPermission
                     * @param {Boolean} granting/revoking a permission
                     * @param {Boolean} isSwap if isSwap, dont pass check to the server, if isnt isSwap, then its batch input, so check
                     */
                    swapAllUsersPermissions: function (acuityAclObjectEnum, acuityAclObjectId, personArray, permissionMask, granting, isSwap) {

                        var check = true; //batch input
                        if (isSwap) {
                            check = false; // swap
                        }
                        // remove user if they are scheduled to be added and not already added                   
                        var postData =
                                _.chain(personArray)
                                .filter(function (person) {
                                    return Person.isActivePermission(person);
                                })
                                .map(function (person) {
                                    return {
                                        acuitySidDetails: Person.toAcuitySidDetails(person),
                                        //WRONG
                                        permissionMask: permissionMask,
                                        granting: granting
                                    };
                                }).value();
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/aces?check=' + check, postData)
                                .success(function (data, status) {
                                    log.info("swapping all users permissions " + permissionMask + " granted? " + granting + " to " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status, headers, config, statusText) {
                            httpNotification.customError("Unable to set all users permissions", status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId, data);
                        });
                        return promise;
                    },
                    /**
                     * Adds a permission to the AcuityObjectIdentityEnum.  Overwrites the current permission if there is
                     * one for this user
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {Person} person with userId
                     * @param {Integer} permissionMask mask of the newPermission
                     */
                    addUserPermission: function (acuityAclObjectEnum, acuityAclObjectId, person, permissionMask) {
                        return this.swapUserPermission(acuityAclObjectEnum, acuityAclObjectId, person, permissionMask, true);
                    },
                    /**
                     * Gets all permisions for the current user
                     * @param {person} person to get all info about
                     * @returns list of user permisions
                     */
                    getAllUserPermisions: function (person) {

                        var promise = $http.get('/resources/acl/userdatasetpermissions/' + person.userId)
                                .success(function (data, status) {
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get a list of permisions for user" + person.userId, status);
                        });
                        return promise;
                    },
                    /**
                     * Removes all permissions to the acuityObjectType
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {Person} person to remove
                     * @returns {unresolved}
                     */
                    removeAllUserPermissions: function (acuityAclObjectEnum, acuityAclObjectId, person) {
                        // Post UserPermission object, with no permissionMask meaning delete all users aces
                        var postData = {
                            acuitySidDetails: Person.toAcuitySidDetails(person)
                        };
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/deleteace', postData)
                                .success(function (data, status) {
                                    log.info("removed all user permissions to " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to remove all permissions for " + person.userId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Replaces the data owner for the acuityObjectType
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {String} userId new data owner
                     * @returns {unresolved}
                     */
                    replaceDataOwner: function (acuityAclObjectEnum, acuityAclObjectId, userId) {
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/ace/dataOwner', userId)
                                .success(function (data, status) {
                                    log.info("replaced dataowner for " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to replace data owner for " + userId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the users in the user db that are enabled
                     *
                     * [{"userId":"User1","fullName":"","authorities":[],"enabled":true},{"userId":"User2","fullName":"","authorities":[],"enabled":true}]
                     *
                     * @returns list of enabled users
                     */
                    getAllEnabledUsers: function () {
                        return this.getAllUserForGroup(trainedUserGroup);
                    },
                    /**
                     * Gets all the users in the user db that are enabled
                     *
                     * [{"userId":"User1","fullName":"","authorities":[],"enabled":true},{"userId":"User2","fullName":"","authorities":[],"enabled":true}]
                     *
                     * @returns list of enabled users
                     */
                    getAllUserForGroup: function (group) {

                        var promise = $http.get('/resources/groups/' + group)
                                .success(function (data, status) {
                                    log.info("got all users from group " + group + " users with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get a list of all users for group " + group, status);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the users permission for the drug programme, study, vis
                     *
                     * [{"user":{"userId":"User1","fullName":"FN","authorities":[],"enabled":true},"permissionMask":256, granted: true},
                     * {"user":{"userId":"User2","fullName":"FN2","authorities":[],"enabled":true},"permissionMask":512, granted: false}]
                     *
                     * @returns list all the drug programme, study, vis permissions
                     */
                    getAllPermissions: function (acuityAclObjectEnum, acuityAclObjectId) {
                        var promise = $http.get('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/ace')
                                .success(function (data, status) {
                                    log.info("got all permissions for " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get a list of all permissions", status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the users permission (with scheduled) for the drug programme, study, vis
                     *
                     * [{"user":{"userId":"User1","fullName":"FN","authorities":[],"enabled":true},"permissionMask":256, granted: true, 
                     *    job:{start:1446598839104, end:1446670839104, permission :{mask:30752, pattern:".................LISP.....V....."}},
                     * {"user":{"userId":"User2","fullName":"FN2","authorities":[],"enabled":true},"permissionMask":512, granted: false}]
                     *
                     * @returns list all the drug programme, study, vis permissions
                     */
                    getAllWithScheduledPermissions: function (acuityAclObjectEnum, acuityAclObjectId) {
                        var promise = $http.get('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/acewithscheduled')
                                .success(function (data, status) {
                                    log.info("got all scheduled permissions for " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get a list of all scheduled permissions", status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the development tram users in the user db
                     *
                     * [{"userId":"User1","fullName":"","authorities":[],"enabled":true},{"userId":"User2","fullName":"","authorities":[],"enabled":true}]
                     *
                     * @returns list of enabled users
                     */
                    getAllDevelopmentTeamUsers: function () {
                        return this.getAllUserForGroup(developmentTeamGroup);
                    },
                    /**
                     * Gets all the users that arent a trained user in the user db
                     *
                     * [{"userId":"User1","fullName":"","authorities":[],"enabled":true},{"userId":"User2","fullName":"","authorities":[],"enabled":true}]
                     *
                     * @returns list of users with no trained user role
                     */
                    getAllNoneTrainedUsers: function () {

                        var promise = $http.get('/resources/groups/not/' + trainedUserGroup)
                                .success(function (data, status) {
                                    log.info("got all users not in the " + trainedUserGroup + " group with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get a list of users not in the " + trainedUserGroup + " group", status);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the matching users with a given surname in active directory.
                     * @param {type} surname
                     * @returns {securityServices_L116.service.getAllMatchingUsers.result}
                     */
                    getAllMatchingUsers: function (surname) {

                        var wildcard;
                        if (surname && surname.length > 3) {
                            wildcard = "END";
                        } else {
                            wildcard = "NONE";
                        }

                        var canceller = $q.defer();
                        var promise = $http.get('/resources/user/search/' + surname + "?wildcard=" + wildcard, {timeout: canceller.promise})
                                .success(function (data, status) {
                                    log.info("got all users matching surname " + surname + " users with status " + status + " wildcard was " + wildcard);
                                    return data;
                                }).error(function (data, status) {
                            log.error("Unable to get a list of all matching users for surname " + surname, status);
                        });
                        var result = {promise: promise, canceller: canceller};
                        return result;
                    },
                    /**
                     * Adds user to a group
                     *
                     * @param {string} userId prid
                     * @param {string} group group name
                     */
                    addUserToGroup: function (userId, group, suppressNotFound) {

                        if ('undefined' === typeof suppressNotFound) {
                            suppressNotFound = false;
                        }

                        var promise = $http.put('/resources/users/' + userId + '/groups', group)
                                .success(function (data, status) {
                                    log.info("added user " + userId + " to group" + group + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            if (!suppressNotFound) {
                                httpNotification.error("Unable add user " + userId + " to group " + group, status);
                            } else {
                                if (status !== 404) {
                                    httpNotification.error("Unable add user " + userId + " to group " + group, status);
                                }
                            }
                        });
                        return promise;
                    },
                    /**
                     * Adds a link between two users.
                     *
                     * @param {String} userIdFrom prid linking from
                     * @param {String} userIdTo prid linking from
                     */
                    linkUser: function (userIdFrom, userIdTo) {

                        var promise = $http.post('/resources/user/' + userIdFrom + '/link/' + userIdTo)
                                .success(function (data, status) {
                                    log.info("linked user " + userIdFrom + " to " + userIdTo + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable link user " + userIdFrom + " to " + userIdTo, status);
                        });
                        return promise;
                    },
                    /**
                     * Removes link for a user.
                     *
                     * @param {String} userId prid to unlinking
                     */
                    unLinkUser: function (userId) {

                        var promise = $http.post('/resources/user/' + userId + '/unlink')
                                .success(function (data, status) {
                                    log.info("unlinked user " + userId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to unlink user " + userId, status);
                        });
                        return promise;
                    },
                    /**
                     * Adds user to the dev team group
                     *
                     * @param {string} userId prid
                     */
                    addUserToDevTeamGroup: function (userId) {
                        return this.addUserToGroup(userId, developmentTeamGroup);
                    },
                    /**
                     * Adds user to the trained users group
                     *
                     * @param {string} userId prid
                     */
                    addExistingUserToTrainedUserGroup: function (userId) {
                        return this.addUserToGroup(userId, trainedUserGroup, true);
                    },
                    /**
                     * Creates a user and adds them to trained users group
                     *
                     * @param {string} userId prid
                     * @param {string} fullName of the user
                     */
                    addNewUserToTrainedUserGroup: function (userId, fullName) {
                        var promise = $http.put('/resources/users/' + userId + '/groups/' + trainedUserGroup + '/fullName/' + fullName)
                                .success(function (data, status) {
                                    log.info("added user " + userId + " with fullName " +
                                            fullName + " to group " + trainedUserGroup + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable add user " + userId + " with fullName " +
                                    fullName + " to group " + trainedUserGroup, status);
                        });
                        return promise;
                    },
                    /**
                     * Removes user from a group
                     *
                     * @param {string} userId prid
                     * @param {string} group group name
                     */
                    removeUserFromGroup: function (userId, group) {
                        var promise = $http.delete('/resources/users/' + userId + '/groups/' + group)
                                .success(function (data, status) {
                                    log.info("removed user " + userId + " from group" + group + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable remove user " + userId + " from group " + group, status);
                        });
                        return promise;
                    },
                    /**
                     * Removes user from a the trained user group
                     *
                     * @param {string} userId prid
                     * @param {boolean} deleteAces delete all user aces
                     */
                    removeUserFromTrainedUserGroupWithAces: function (userId, deleteAces) {
                        var promise = $http.delete('/resources/users/' + userId + '/groups/trainedUserGroup?deleteAces=' + deleteAces)
                                .success(function (data, status) {
                                    log.info("removed user " + userId + " from TRAINED_USER_GROUP and removed all aces? " + deleteAces + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable remove user " + userId + " from TRAINED_USER_GROUP", status);
                        });
                        return promise;
                    },
                    /**
                     * Adds user to the trained user group
                     *
                     * @param {string} userId prid
                     */
                    addUserToTrainedUserGroup: function (userId) {
                        return this.addUserToGroup(userId, trainedUserGroup);
                    },
                    /**
                     * Remove user from the trained user group
                     *
                     * @param {string} userId prid
                     * @param {boolean} deleteAces delete all user aces
                     */
                    removeUserFromTrainedUserGroup: function (userId, deleteAces) {
                        if (!deleteAces) {
                            deleteAces = false;
                        }
                        return this.removeUserFromTrainedUserGroupWithAces(userId, deleteAces);
                    },
                    /**
                     * Remove user from the dev team group
                     *
                     * @param {string} userId prid
                     */
                    removeUserFromDevTeamGroup: function (userId) {
                        return this.removeUserFromGroup(userId, developmentTeamGroup);
                    },
                    /**
                     * Adds a scheduled permission for user/group
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     * @param {Person} person or group
                     * @param {Integer} permissionMask mask of the newPermission
                     * @param {Date} start date/time of this permission
                     * @param {Date} end date/time of this permission
                     */
                    addScheduledSid: function (acuityAclObjectEnum, acuityAclObjectId, person, permissionMask, start, end) {
                        var postData = {
                            acuitySidDetails: Person.toAcuitySidDetails(person),
                            // WRONG
                            permissionMask: permissionMask,
                            granting: true,
                            start: start,
                            end: end
                        };
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/schedule/ace', postData)
                                .success(function (data, status) {
                                    log.info("added scheduled permission for " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to add scheduled permissions for " + person.userId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * List all the groups from the secuirty db
                     */
                    listAllGroups: function () {
                        var promise = $http.get('/resources/groups')
                                .success(function (data, status) {
                                    log.info("got list of all groups ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable get all groups", status);
                        });
                        return promise;
                    },
                    /**
                     * List all the groups from the secuirty db
                     */
                    listAllGroupsWithLockdown: function () {
                        var promise = $http.get('/resources/groups/withdatasetsandlockdown')
                                .success(function (data, status) {
                                    log.info("got list of all groups with lockdown ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable get all groups with lockdown", status);
                        });
                        return promise;
                    },
                    /**
                     * Create a group from the secuirty db
                     */
                    createGroup: function (groupName) {
                        var promise = $http.post('/resources/groups/' + groupName)
                                .success(function (data, status) {
                                    log.info("created group " + groupName + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to create group " + groupName, status);
                        });
                        return promise;
                    },
                    /**
                     * Delete agroup from the secuirty db
                     */
                    deleteGroup: function (groupName) {
                        var promise = $http.delete('/resources/groups/' + groupName)
                                .success(function (data, status) {
                                    log.info("deleted group " + groupName + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to delete group " + groupName, status);
                        });
                        return promise;
                    },
                    /**
                     * Gets all the users who have view permission for the acl (including from their parent drug propgramme and groups)
                     *
                     * @param {AcuityAclObjectEnum} acuityAclObjectEnum, either ClinicalStudy, Dataset or DrugProgramme
                     * @param {Integer} acuityAclObjectId id of the acuityObjectIdentityEnum, ie DrugProgramme 1
                     */
                    getGrantedUsersForAcl: function (acuityAclObjectEnum, acuityAclObjectId) {
                        var promise = $http.get('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/useraces')
                                .success(function (data, status) {
                                    log.info("got granted users for " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to get granted users for " + acuityAclObjectEnum.name + ' ' + acuityAclObjectId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Sets the lockdown status
                     */
                    setLockdown: function (acuityAclObjectEnum, acuityAclObjectId, lockdownStatus) {

                        var endpoint = "setlockdown";
                        if (!lockdownStatus) {
                            endpoint = "unsetlockdown";
                        }

                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/' + endpoint, lockdownStatus)
                                .success(function (data, status) {
                                    log.info("set lockdown of " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to set lockdown for " + acuityAclObjectEnum.name + ' ' + acuityAclObjectId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Enables the dataset by setting the inheritied permissions
                     */
                    setInheritPermission: function (acuityAclObjectEnum, acuityAclObjectId, inheritiedStatus) {

                        var endpoint = "setInheritPermission";
                        if (!inheritiedStatus) {
                            endpoint = "unsetInheritPermission";
                        }

                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/' + endpoint)
                                .success(function (data, status) {
                                    log.info("set inherit permission of " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to set inherit permission for " + acuityAclObjectEnum.name + ' ' + acuityAclObjectId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Sets the view package permissions, these would be the all the permission, including base and view vis
                     */
                    setViewPackagePermission: function (acuityAclObjectEnum, acuityAclObjectId, person) {

                        var postData = {
                            viewPermissionMasks: person.viewPackagePermissions,
                            acuitySidDetails: Person.toAcuitySidDetails(person)
                        };
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/viewpackageaces', postData)
                                .success(function (data, status) {
                                    log.info("set view package permissions " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to set view package permissions " + acuityAclObjectEnum.name + ' ' + acuityAclObjectId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    },
                    /**
                     * Sets the view extra package permissions, these would be the just the extra packeage permission, ir oncology and proact
                     */
                    setViewExtraPackagePermission: function (acuityAclObjectEnum, acuityAclObjectId, person) {

                        var postData = {
                            viewPermissionMasks: person.viewPackagePermissions,
                            acuitySidDetails: Person.toAcuitySidDetails(person)
                        };
                        var promise = $http.post('/resources/acl/' + acuityAclObjectEnum.name + '/' + acuityAclObjectId + '/viewextrapackagesaces', postData)
                                .success(function (data, status) {
                                    log.info("set view extra package permissions " + acuityAclObjectEnum.name + ": " + acuityAclObjectId + " ok with status " + status);
                                    return data;
                                }).error(function (data, status) {
                            httpNotification.error("Unable to set view extra package permissions " + acuityAclObjectEnum.name + ' ' + acuityAclObjectId, status, acuityAclObjectEnum.name + ' ' + acuityAclObjectId);
                        });
                        return promise;
                    }
                };
                return service;
            }])

        .factory('linkUsersDialog', ['ngDialog', function (ngDialog) {

                var dialog;
                /**
                 * Opens the modal dialog, ths user can only close by pressing accept.
                 *
                 * This only opens if the cookie isnt present that signifies that they have already accepted the terms of use
                 */
                var openModal = function ($scope) {

                    if (dialog) {
                        dialog.close();
                    }

                    dialog = ngDialog.open({
                        className: 'ngdialog-theme-default',
                        showClose: true,
                        closeByEscape: true,
                        closeByDocument: true,
                        template: '/views/myAnalytics/linkUsersDialog.html',
                        scope: $scope
                    });
                };
                /**
                 * Close dialog
                 */
                var closeModal = function () {

                    if (dialog) {
                        dialog.close(0);
                    }
                };
                return {
                    openModal: openModal,
                    closeModal: closeModal
                };
            }])
        /**
         * Security Auth, determines if a user has access to a ACUITY PERMISSION (ie VIEW VISUALISATIONS).
         *
         * Note this visual security, it disables or hides a button, the actual security is and should always
         * be enforced on the server.
         */
        .factory('SecurityAuth',['SecurityModel', function (SecurityModel) {
            var authService = {
                /*
                 * Gets all the roles above AUTHORISED_USER
                 */
                getRolesMasksExceptAuthorisedUser: function () {
                    return [522240, 450575, 442383, 32783];
                },
                /*
                 * Gets all the roles above AUTHORISED_USER
                 */
                getRoleFromMask: function (permissionMask) {
                    switch (permissionMask) {
                        case 522240:
                            return 'Global Admin';
                        case 450575:
                            return 'Drug Programme dataowner';
                        case 442383:
                            return 'Authroiser';
                        case 32783:
                            return 'Administrator';
                        default:
                            return 'Authorised user';
                    }
                },
                /**
                 * If permissionMask is one of getRolesMasksExceptAuthorisedUser, returns true
                 */
                isOneOfRolesMasksExceptAuthorisedUser: function (permissionMask) {
                    return authService.getRolesMasksExceptAuthorisedUser().indexOf(permissionMask) !== -1;
                },
                /**
                 * Decides whether the permission required is allowed for the acl.  The acl has rolePermissionMask and using
                 * bitmasking if can determine whether it has the required permission.
                 *
                 * Ie.  acl.rolePermissionMask = 64,  required permission is 32, then ((64 | 32) == 64) is true
                 *
                 * @param {String or Integer} permissionRequired permission mask of the required either 1 or VIEW_VISUALISATIONS
                 * @param {Acl} acl that user wants the permisson to access.
                 * {
                 *  "type": "com.acuity.acuity.security.acl.domain.DrugProgramme",
                 *  "id": 29,
                 *  "name": "STUDY0005",
                 *  "rolePermissionMask": 32,
                 *  "viewPermissionMask": 32,
                 *  "isOpen": true,
                 *  "drugProgramme": "STDY4321",
                 *  "moduleType": null
                 * },
                 *
                 * @returns {Boolean} true
                 */
                hasPermission: function (permissionRequired, acl) {
                    var isString = isNaN(parseInt(permissionRequired));
                    if (!isString) {
                        return authService.checkPermission(permissionRequired, acl.rolePermissionMask);
                    } else {
                        // permissionRequired is a string, so convert 'VIEW_VISUALISATIONS' into mask 1.
                        var acuityPermission = _.findLast(SecurityModel.getAcuityPermissions(), function (acuityPermission) {
                            return acuityPermission.name === permissionRequired;
                        });
                        if (acuityPermission === null || _.isUndefined(acuityPermission)) {
                            return false;
                        } else {
                            return authService.checkPermission(acuityPermission.mask, acl.rolePermissionMask);
                        }
                    }
                },
                /**
                 * method to do permission checking
                 * 
                 * ie does 64 have 32 permission
                 * 
                 * (64 | 32) == 64 is true so 64 does have 32 permission
                 */
                checkPermission: function (permissionRequired, permissionForAcl) {
                    return (permissionForAcl | permissionRequired) === permissionForAcl;
                },
                /**
                 * Currently there are 3 'permissions' in the permission model that arent directly
                 * related to an Acl but depends on whether a user has a permission higher than
                 * Administrator.
                 *
                 * Add and remove developer role -- Developer Only
                 * Access to admin UI home page  -- Administrator or higher (Authoriser, Data Owner, Developer)
                 * Access to study upload reports -- Administrator or higher (Authoriser, Data Owner, Developer)
                 *
                 * @param {List<Acl>} acls list of current acls for the user
                 */
                hasAnyPermissionAdministratorOrAbove: function (acls) {
                    return _.some(acls, function (acl) {
                        // DEV TEAM 522240 // Data Owner 450575 // Authoriser 442383 // Administrator 32783
                        return acl.rolePermissionMask === DEVELOPMENT_TEAM ||
                                acl.rolePermissionMask === DRUG_PROGRAMME_DATA_OWNER ||
                                acl.rolePermissionMask === AUTHROISERS ||
                                acl.rolePermissionMask === ADMINISTRATOR;
                    });
                },
                /**
                 * See hasAnyPermissionAdministratorOrAbove() documentation
                 *
                 * @param {List<Acl>} acls list of current acls for the user
                 */
                hasAnyPermissionDeveloper: function (acls) {
                    return _.some(acls, function (acl) {
                        // DEV TEAM 522240
                        return acl.rolePermissionMask === DEVELOPMENT_TEAM;
                    });
                },
                /**
                 * Gets the view package permissions for the user
                 *
                 * @param {Integer} permissionMask of the acl of current acls for the user
                 */
                getViewPackagePermissions: function (permissionMask) {
                    return _.filter(_.map(SecurityModel.getViewPackagePermissons(), 'mask'), function (mask) {
                        return authService.checkPermission(mask, permissionMask);
                    });
                }
            };
            return authService;
        }]);
