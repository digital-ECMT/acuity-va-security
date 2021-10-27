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

angular.module('acuityApp.myAnalyticsServices', [])

    .factory('addScheduledUserDialog', ['ngDialog', function (ngDialog) {

            var dialog;
            /**
             * Opens the modal dialog, ths user can only close by pressing accept.
             */
            var openModal = function ($scope) {

                if (dialog) {
                    dialog.close();
                }

                dialog = ngDialog.open({
                    className: 'ngdialog-theme-default custom-medium-width',
                    showClose: false,
                    closeByEscape: false,
                    closeByDocument: false,
                    template: '/views/myAnalytics/addScheduledUserDialog.html',
                    scope: $scope
                });

                return dialog;
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

    .factory('importUserPermissionsDialog', ['ngDialog', function (ngDialog) {

            var dialog;
            /**
             * Opens the modal dialog, ths user can only close by pressing accept.
             */
            var openModal = function ($scope) {

                if (dialog) {
                    dialog.close();
                }

                dialog = ngDialog.open({
                    className: 'ngdialog-theme-default custom-width',
                    showClose: true,
                    closeByEscape: true,
                    closeByDocument: true,
                    template: '/views/myAnalytics/importUserPermissionsDialog.html',
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
    
    .factory('viewUsersInAclDialog', ['ngDialog', function (ngDialog) {

            var dialog;
            /**
             * Opens the modal dialog, ths user can only close by pressing accept.
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
                    template: '/views/myAnalytics/viewUsersInAclDialog.html',
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

    .factory('addGroupDialog', ['ngDialog', function (ngDialog) {

            var dialog;
            /**
             * Opens the modal dialog, ths user can only close by pressing accept.
             */
            var openModal = function ($scope) {

                if (dialog) {
                    dialog.close();
                }

                dialog = ngDialog.open({
                    className: 'ngdialog-theme-default custom-medium-width',
                    showClose: true,
                    closeByEscape: true,
                    closeByDocument: true,
                    template: '/views/myAnalytics/addGroupDialog.html',
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
    
    .factory('viewUsersInGroupDialog', ['ngDialog', function (ngDialog) {

            var dialog;
            /**
             * Opens the modal dialog, ths user can only close by pressing accept.
             */
            var openModal = function ($scope) {

                if (dialog) {
                    dialog.close();
                }

                dialog = ngDialog.open({
                    className: 'ngdialog-theme-default custom-medium-width',
                    showClose: true,
                    closeByEscape: true,
                    closeByDocument: true,
                    template: '/views/myAnalytics/viewUsersInGroupDialog.html',
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

    .factory('MetadataService', [function () {

            return {
                /**
                 * Returns the object metadata object from the headers section of the response
                 *
                 * @param {headers} headers section of the http response containing string version of metadata object
                 */
                getMetadata: function (headers) {
                    if (headers && angular.fromJson(headers().metadata)) {
                        return angular.fromJson(headers().metadata);
                    } else {
                        return null;
                    }
                }
            };
        }])
    /**
     * Access Control Entry.  This is a class that represents an ace in the security database.
     * I.e.
     *  {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 29, name: "STUDY0005", permissionMask: 32, compound: "STDY4321", moduleType: null},
     *  {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 101, name: "Dummy study 2", permissionMask: 64, compound: "STDY5321", moduleType: null}
     *  {type: "com.acuity.acuity.security.acl.domain.AcuityDataset", id: 130, name: "ACUITY_Safety_STDY4321_Dummy_Instance", permissionMask: 32, compound: "STDY4321", moduleType: "Safety"},
     *
     * @returns {Ace}
     */
    .factory('Ace', [function () {

            /**
             * New Permissions object
             *
             * @returns {Permissions}
             */
            function Ace() {
                this.type = "";
                this.id = 0;
                this.name = "";
                this.permissionMask = 0;
                this.compound = "";
                this.moduleType = "";
            }

            // Return constructor - this is what defines the actual
            // injectable in the DI framework.
            return Ace;
        }])
    /**
     * Permissions object used to store user permission arrays for each type of permission
     *
     * @returns {_L10.Permissions}
     */
    .factory('Permissions', [function () {

            /**
             * New Permissions object
             *
             * @returns {Permissions}
             */
            function Permissions() {
                this.trainedUsers = [];
                this.dataOwners = [];
                this.authorisers = [];
                this.administrators = [];
                this.authorisedUsers = [];
            }

            // Return constructor - this is what defines the actual
            // injectable in the DI framework.
            return Permissions;
        }])

    /**
     * Person object used when editing user permissions on acuityAclObjects
     *
     * @returns {_L10.Person}
     */
    .factory('Person', [function () {

            /**
             * New Person object with job.
             * 
             * This has been extended to group (so not really a person now, but a sid)
             *
             * @param {String} name - full name of the person, ie Glen Drinkwater
             * @param {String} id - userId, ie user001
             * @param {Boolean} granted - whether granting is given or revoked
             * @param {Person} linkedPerson - linked person
             * @param {AddRemovePermissionJob} permissionJob - job that added this permission in
             * @param {String} group - is this a group type and not a user
             * @param {Int} permissionMask - role and view permission mask of the person that is stored in the db
             * @returns {Person}
             */
            function Person(name, id, granted, linkedPerson, permissionJob, group, permissionMask) {
                this.name = name;
                this.userId = id;
                this.granted = granted;
                this.nameAndId = name + ((id === "") ? "" : " (" + id + ")");
                this.linkedPerson = linkedPerson;
                this.permissionJob = permissionJob;
                this.group = group || false;
                this.permissionMask = permissionMask;
                this.viewPackagePermissions = [];
            }

            // Define the "instance" methods using the prototype
            // and standard prototypal inheritance.
            Person.prototype = {
                isEmpty: function () {
                    return this.name === 'Empty';
                },
                isLinked: function () {
                    return this.linkedPerson !== undefined && this.linkedPerson !== null;
                },
                isScheduledJob: function () {
                    return this.permissionJob !== undefined && this.permissionJob !== null;
                },
                jobStateCode: function () {
                    if (this.isScheduledJob()) {
                        return this.permissionJob.jobStateCode;
                    } else {
                        return -1;
                    }
                },
                jobState: function () {
                    if (this.isScheduledJob()) {
                        return this.permissionJob.jobState;
                    } else {
                        return undefined;
                    }
                },
                isActivePermission: function () {
                    return this.jobStateCode() === -1 || this.jobStateCode() === 1;
                },
                toAcuitySidDetails: function () {
                    return Person.toAcuitySidDetails(this);
                }
            };

            // "class" / "static" methods
            Person.emptyPerson = function () {
                return new Person("Empty", "");
            };

            Person.person = function (userId) {
                return new Person(userId, userId, true);
            };

            Person.groupPerson = function (groupName) {
                var groupPerson = new Person(groupName, groupName, true);
                groupPerson.group = true;
                return groupPerson;
            };

            /**
             * Sometimes a person is just represented as a Object {userId} and has no method, so create static method
             * 
             * @param {json object} personObject
             */
            Person.toAcuitySidDetails = function (personObject) {
                return {
                    sidAsString: personObject.userId,
                    group: personObject.group
                };
            };
            /**
             * If the user is scheduled, then they arent active, otherwise they are active
             */
            Person.isActivePermission = function (personObject) {
                if (_.isFunction(personObject.isActivePermission)) {
                    return personObject.isActivePermission();
                } else {
                    return true;
                }
            };

            // Return constructor - this is what defines the actual
            // injectable in the DI framework.
            return Person;
        }])

    .factory('MyAnalyticsUtils', ['Person', '$log', '$filter', 'SecurityServices', function (Person, $log, $filter, SecurityServices) {
            var emptyPerson = Person.emptyPerson();

            var service = {
                /**
                 * If array is empty, adds an empty person, and if it isnt, it removes the empty person if it has more than one none empty person
                 *
                 * @param {Person} array of Persons
                 */
                checkIfArrayEmpty: function (array) {
                    if (array.length > 1) {
                        var index = _.findIndex(array, function (person) {
                            return person.name === "Empty";
                        });
                        if (index !== -1) {
                            array.splice(index, 1);
                        }
                    } else if ((array.length === 0)) {
                        array.push(emptyPerson);
                    }
                },
                /**
                 * Adds a $data object to a list of Persons
                 *
                 * @param {Person object (not always a person person object) {name:'ss', userId:'prid'} $data object to add user to in the form of
                 * @param {Person} array array to add person to
                 */
                addPersonToList: function ($data, array) {
                    var granted = _.isUndefined($data.granted) ? true : $data.granted;
                    
                    var person = new Person($data.name, $data.userId, granted, null, $data.permissionJob);
                    person.group = _.isUndefined($data.group) ? false : $data.group;
                    
                    array.push(person);
                    
                    this.checkIfArrayEmpty(array);
                    $log.info("Added " + $data.name + " to the list");
                },
                /**
                 * only add a $data object to a list of Persons if its not a group
                 *
                 * @param {Person object (not always a person person object) {name:'ss', userId:'prid'} $data object to add user to in the form of
                 * @param {Person} array array to add person to
                 */
                onlyAddPersonToList: function ($data, array) {
                    if (!$data.group) {
                        this.addPersonToList($data, array);
                    }
                },
                /**
                 * Similar to lodash pull method, but doesnt use ===, it checks users userId.
                 *
                 * Ie removes all the objects in allUsers that appear in array (by matching userId)
                 *
                 * @param {Person} array of Persons to remove from allUsers array
                 * @param {Person} allUsers of Persons
                 */
                removeFromUsers: function (array, allUsers) {
                    _.forEach(array, function (acuityUser) {
                        var index = _.findIndex(allUsers, function (person) {
                            return person.userId === acuityUser.userId;
                        });

                        if (index !== -1) {
                            allUsers.splice(index, 1);
                        }
                    });
                },
                /**
                 * Remove all users from a given array.
                 * @param {type} array
                 * @returns {undefined}
                 */
                removeAllUsers: function (array) {
                    while (array.length > 0) {
                        array.pop();
                    }

                    array.push(emptyPerson);
                },
                /**
                 * Gets all the user permissions for the particular acuityAclObject:acuityAclObjectId
                 * ie DrugProgramme:1, Dataset:2
                 * and transforms this into the individual permissions list and also gets a list of
                 * all the users in the system.
                 *
                 * @param {Permissions} permissions of all the users for this acuityAclObjectEnumAsString:acuityAclObjectId
                 * @param {String} acuityAclObjectEnum ie AcuityAclObjectEnum.DrugProgramme
                 * @param {Long} acuityAclObjectId id in db
                 * @param {Callback} cb function once this has finished
                 * @returns {undefined}
                 */
                populateAclPermissions: function (permissions, acuityAclObjectEnum, acuityAclObjectId, cb) {

                    var self = this;

                    SecurityServices.getAllEnabledUsers()
                        .then(function (response) {
                            permissions.trainedUsers = _.map(response.data, function (user) {
                                return new Person(user.fullName, user.userId);
                            });

                            /**
                             * Gets all the permissions for this acl, and iterate over then to pull out all dataOwners,
                             * authorisedUsers etc and once done than, remove them from the users list
                             */
                            SecurityServices.getAllPermissions(acuityAclObjectEnum, acuityAclObjectId)
                                .then(function (response) {
                                    permissions.dataOwners = $filter('filterPermissionMask')(response.data, 450575);
                                    self.checkIfArrayEmpty(permissions.dataOwners);
                                    self.removeFromUsers(permissions.dataOwners, permissions.trainedUsers);

                                    permissions.authorisers = $filter('filterPermissionMask')(response.data, 442383);
                                    self.checkIfArrayEmpty(permissions.authorisers);
                                    self.removeFromUsers(permissions.authorisers, permissions.trainedUsers);

                                    permissions.administrators = $filter('filterPermissionMask')(response.data, 32783);
                                    self.checkIfArrayEmpty(permissions.administrators);
                                    self.removeFromUsers(permissions.administrators, permissions.trainedUsers);

                                    permissions.authorisedUsers = $filter('filterPermissionMask')(response.data, 3);
                                    self.checkIfArrayEmpty(permissions.authorisedUsers);
                                    self.removeFromUsers(permissions.authorisedUsers, permissions.trainedUsers);

                                    // finally check if this is empty as thing could be been removed
                                    self.checkIfArrayEmpty(permissions.trainedUsers);
                                });
                        }).finally(cb);
                }
            };

            return service;

        }])

    .factory('MyAnalyticsDNDUtils', ['Person', 'SecurityAuth', function (Person, SecurityAuth) {

            // list of drop locations in order of highest permission
            var definedDropLocations =
                ['dataOwnerChannel', 'authoriserChannel', 'administratorChannel', 'authorisedUserChannel', 'usersChannel'];

            /**
             * gets a list of all the droppable locations for a users permission mask.
             *
             * It gets the mask from the acuityAclObject, and depending on the mask, it determines where it is in the
             * definedDropLocations and uses that posiition to return a list.
             *
             * Ie if a user permission was dataOwner => authoriserChannel, administratorChannel, authorisedUserChannel, usersChannel
             *                            authoriser => administratorChannel, authorisedUserChannel, usersChannel
             *                         administrator => authorisedUserChannel, usersChannel
             *                              anything else return nothing / []
             *
             * @param {Acl} acuityAclObject ie DrugProgramme 1's acl
             */
            var getAllDroppableLocations = function (acuityAclObject) {
                // index in definedDropLocations where user has access to
                var definedDropLocations = [];

                if (SecurityAuth.hasPermission('EDIT_DATA_OWNERS', acuityAclObject)) {
                    definedDropLocations.push('dataOwnerChannel');
                }
                if (SecurityAuth.hasPermission('EDIT_AUTHORISERS', acuityAclObject)) {
                    definedDropLocations.push('authoriserChannel');
                }
                if (SecurityAuth.hasPermission('EDIT_ADMINISTRATORS', acuityAclObject)) {
                    definedDropLocations.push('administratorChannel');
                }
                if (SecurityAuth.hasPermission('EDIT_AUTHORISED_USERS', acuityAclObject)) {
                    definedDropLocations.push('authorisedUserChannel');
                }
                if (definedDropLocations.length !== 0) {
                    definedDropLocations.push('usersChannel');
                }
                return definedDropLocations;
            };

            var service = {
                /**
                 * Gets a list of all of the possible drop locations for a user with a permission mask on an acl object.
                 *
                 * Ie. for drug programme 1, user has dataOwner permission mask.
                 * If current location (mySelfLocation) was they wanted to drag from authoriserChannel
                 * Then the droppable locations would be:  administratorChannel, authorisedUserChannel, usersChannel
                 *
                 * Ie. for clinical study 1, user has administrator permission mask.
                 * If current location (mySelfLocation) was they wanted to drag from authorisedUserChannel
                 * Then the droppable locations would be:  usersChannel
                 *
                 * @param {String} mySelfLocation location of self, ie one of definedDropLocations
                 * @param {Acl} acuityAclObject ie DrugProgramme 1's acl
                 * @returns {List<String>} list of all possible drop locations for current use from mySelfLocation
                 */
                getMyDroppableLocations: function (mySelfLocation, acuityAclObject) {
                    var allDroppableLocations = getAllDroppableLocations(acuityAclObject);

                    // check if the drop location (mySelfLocation) is contained in allDroppableLocations, if not return ""
                    if (_.contains(allDroppableLocations, mySelfLocation)) {
                        return _.pull(allDroppableLocations, mySelfLocation);
                    } else {
                        return "";
                    }
                },
                /**
                 * A person is right clicked and dragged, this method determines if this is a draggable object.
                 * If person is empty, then the list is empty and therefore not draggable
                 * If mySelfLocation is in the list all droppedable locations then its allowed to be dragged.
                 *
                 * Ie. for drug programme 1, user has dataOwner permission mask.
                 * If current location (mySelfLocation) was they wanted to drag from authoriserChannel
                 * Then the all available droppable locations would be:  authoriserChannel, administratorChannel, authorisedUserChannel, usersChannel
                 * And mySelfLocation (authoriserChannel) is part of that so is draggable.
                 *
                 * Ie. for drug programme 1, user has authorisedUser permission mask.
                 * If current location (mySelfLocation) was they wanted to drag from authoriserChannel
                 * Then the all available droppable locations would be:  authorisedUserChannel, usersChannel
                 * And mySelfLocation (authoriserChannel) isnt part of that so isnt draggable.
                 *
                 * @param {Person}  person who is going to be dragged
                 * @param {String} mySelfLocation location of self, ie one of definedDropLocations
                 * @param {Acl} acuityAclObject ie DrugProgramme 1's acl
                 * @returns {Boolean} is this item in the list of mySelfLocation allowed to be dragged
                 */
                isDraggable: function (person, mySelfLocation, acuityAclObject) {
                    if (person.isEmpty()) {
                        return false;
                    } else {
                        var allDroppableLocations = getAllDroppableLocations(acuityAclObject);
                        return _.contains(allDroppableLocations, mySelfLocation);
                    }
                }
            };
            return service;
        }]);
