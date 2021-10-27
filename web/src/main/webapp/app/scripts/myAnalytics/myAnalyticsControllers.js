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

angular.module('acuityApp.myAnalyticsControllers', [])

/**
 * Controller for displaying the page in list view or in grid view
 *
 * Needs refactered and seperated into smaller controllers
 */
    .controller('MyAnalyticsController',
        ['$scope', '$log', '$filter', 'ParentModel', 'SecurityServices', "MyAnalyticsUtils", "MyAnalyticsDNDUtils",
            'SecurityAuth', 'Person', 'viewUsersInGroupDialog', 'addGroupDialog', 'AcuityAclObjectEnumService',
            function ($scope, $log, $filter, ParentModel, SecurityServices, MyAnalyticsUtils, MyAnalyticsDNDUtils,
                      SecurityAuth, Person, viewUsersInGroupDialog, addGroupDialog, AcuityAclObjectEnumService) {

                $scope.SecurityAuth = SecurityAuth;

                $scope.PARENT_MODEL = ParentModel;

                /**
                 * re initialises all the model after the dialog is closed or someone is added
                 */
                $scope.baseInit = function () {
                    $scope.PARENT_MODEL.addScheduledUserPermission.personToAdd = null;
                    $scope.PARENT_MODEL.addScheduledUserPermission.arrayTo = [];
                    $scope.PARENT_MODEL.addScheduledUserPermission.newPermissionMask = null;
                    $scope.PARENT_MODEL.addScheduledUserPermission.start = null;
                    $scope.PARENT_MODEL.addScheduledUserPermission.end = null;
                    $scope.PARENT_MODEL.addScheduledUserPermission.errroMessage = "";

                    $scope.PARENT_MODEL.groups.selectedGroup = "";
                    $scope.PARENT_MODEL.groups.arrayTo = null;
                    $scope.PARENT_MODEL.groups.allGroups = [];
                    $scope.PARENT_MODEL.groups.permissionMask = null;
                    $scope.PARENT_MODEL.groups.isScheduled = false;
                };

                //attach service to scope for direct use
                $scope.MyAnalyticsDNDUtils = MyAnalyticsDNDUtils;

                /**
                 * Watch the model object, searchTerm.name and on any changes to it, apply the same filter as in the html
                 * and count the results.
                 *
                 * This is not ideal as its filtered twice, but the size shouldnt be large.
                 * I tried ng-repeat="dp in filteredDP  = (drugProgrammes | filterOr:searchTerm)' and then use
                 * filteredDP in the other page but it didnt work, seems different scopes.
                 */
                var searchUserTermListener = $scope.$watch("PARENT_MODEL.searchTerm.name", function (text) {
                    $log.debug("search " + $scope.PARENT_MODEL.searchTerm.name);
                    $scope.PARENT_MODEL.filteredAclsCounts.drugProgrammes =
                        $filter("filterOr")($scope.PARENT_MODEL.allAcls.drugProgrammes, $scope.PARENT_MODEL.searchTerm).length;

                    $scope.PARENT_MODEL.filteredAclsCounts.studies =
                        $filter("filterOr")($scope.PARENT_MODEL.allAcls.clinicalStudies, $scope.PARENT_MODEL.searchTerm).length;

                    $scope.PARENT_MODEL.filteredAclsCounts.datasets =
                        $filter("filterOr")($scope.PARENT_MODEL.allAcls.datasets, $scope.PARENT_MODEL.searchTerm).length;
                });

                $scope.$on('$destroy', function () {
                    searchUserTermListener();
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
                    $log.info("Removed " + person.name + " from list");

                    // keep in history so we can redo last remove
                    $scope.PARENT_MODEL.dropRedo.lastPersonArray = array;
                    $scope.PARENT_MODEL.dropRedo.lastPerson = person;
                };

                /**
                 * Reruns the last removal from a list
                 */
                $scope.redoLastDrop = function () {
                    if ($scope.PARENT_MODEL.dropRedo.lastPerson !== null && $scope.PARENT_MODEL.dropRedo.lastPersonArray !== null) {

                        MyAnalyticsUtils.addPersonToList($scope.PARENT_MODEL.dropRedo.lastPerson, $scope.PARENT_MODEL.dropRedo.lastPersonArray);
                        $scope.PARENT_MODEL.dropRedo.lastPerson = null;
                        $scope.PARENT_MODEL.dropRedo.lastPersonArray = null;
                    }
                };

                /**
                 * A Person object has ben dropped onto a new list, this needs to be sent to the server to add the new permission
                 * to the database and then the person object added to the list
                 *
                 * @param {Person} person person added to be added to the array
                 * @param {Person} arrayTo of people in the list the person is going to
                 * @param {String} newPermissionMask new permission mask for the person
                 * @param {String} acuityAclObjectEnum ie AcuityAclObjectEnum.DrugProgramme
                 * @param {String} acuityAclObjectId ro add the newPermissionMask to
                 * @param {Callback} cb function once this has finished
                 * @returns {undefined}
                 */
                $scope.baseOnDrop = function (person, arrayTo, newPermissionMask, acuityAclObjectEnum, acuityAclObjectId, cb) {

                    if (newPermissionMask === '0') {
                        return SecurityServices.removeAllUserPermissions(acuityAclObjectEnum, acuityAclObjectId, person)
                            .then(function (response) {
                                MyAnalyticsUtils.onlyAddPersonToList(person, arrayTo);
                            }).finally(cb);
                    } else if (newPermissionMask === '450575') { // replacing data owner
                        return SecurityServices.replaceDataOwner(acuityAclObjectEnum, acuityAclObjectId, person.userId)
                            .then(function (response) {
                                MyAnalyticsUtils.addPersonToList(person, arrayTo);
                            }).finally(cb);
                    } else {
                        return SecurityServices.addUserPermission(acuityAclObjectEnum, acuityAclObjectId, person, newPermissionMask)
                            .then(function (response) {
                                MyAnalyticsUtils.addPersonToList(person, arrayTo);
                            }).finally(cb);
                    }
                };

                /**
                 * Add a list of users to the authorised user list for a particular acuityAclObject, then updates the
                 * authorisedUsers to reflect what was send back from the rest call
                 *
                 * @param {String} acuityAclObjectEnum ie AcuityAclObjectEnum.DrugProgramme
                 * @param {String} acuityAclObjectId ro add the newPermissionMask to
                 * @argument {Person} requestPersonArray Person array to add to acuityAclObject
                 * @argument {Integer} permissionMask to add the users permission as
                 * @argument {Person} authorisedUsers currently holding all the users
                 * @argument {callBack} cd after method has finished, this is used to stop the waiting spinner
                 */
                $scope.baseImportUsersPermissions = function (acuityAclObjectEnum, acuityAclObjectId, requestPersonArray, permissionMask, authorisedUsers, cb, trainedUsers) {

                    return SecurityServices.swapAllUsersPermissions(acuityAclObjectEnum, acuityAclObjectId, requestPersonArray, permissionMask, true, false)
                        .then(function (response) {

                            // response is all the current users for this permission mask (ones that have been added plus one that already there)
                            var responsePersonArray = _.map(response.data, function (userWithPermission) {
                                return new Person(userWithPermission.user.fullName, userWithPermission.user.userId, userWithPermission.granted);
                            });

                            MyAnalyticsUtils.removeFromUsers(requestPersonArray, trainedUsers);

                            // remove all and reset list
                            MyAnalyticsUtils.removeAllUsers(authorisedUsers);

                            _.forEach(responsePersonArray, function (person) {
                                MyAnalyticsUtils.addPersonToList(person, authorisedUsers);
                            });
                        }).finally(cb);
                };

                /**
                 * Generates a person array from a list of prids in \n format as a string
                 *
                 * @param {String} inputString a list of prids seperated by \n
                 * @returns {Person} array of Person objects
                 */
                $scope.generatePersonArray = function (inputString) {
                    var pridArray = inputString.split("\n");

                    return _.chain(pridArray).filter(function (prid) {
                        return prid ? true : false;
                    }).uniq().map(function (prid) {
                        return new Person('N/A', prid.trim(), true);
                    }).value();
                };

                $scope.isAcuityType = function (acuityObjectIdentity) {
                    return AcuityAclObjectEnumService.isAcuityType(acuityObjectIdentity);
                };

                $scope.listAndJoinNames = function (acuityObjectIdentities, joinOn) {
                    if (acuityObjectIdentities) {
                        return acuityObjectIdentities.map(function (item) {
                            return item.name;
                        }).join(joinOn);
                    } else {
                        return '';
                    }
                };

                $scope.listAndJoinDatasetNames = function (acuityObjectIdentities, joinOn) {
                    if (acuityObjectIdentities) {
                        return acuityObjectIdentities.map(function (item) {
                            return item.clinicalStudyName + ':' + item.name;
                        }).join(joinOn);
                    } else {
                        return '';
                    }
                };

                /////////////////////////////////////////////////////////
                ////////////////   Group methods ////////////////////////
                /////////////////////////////////////////////////////////

                /**
                 * User groups in the security db
                 */
                var trainedUserGroup = "TRAINED_USER_GROUP";
                var developmentTeamGroup = "DEVELOPMENT_GROUP";

                /**
                 * Remove a group user from the dataset.
                 */
                $scope.viewUsersInGroup = function (groupPerson) {

                    SecurityServices.getAllUserForGroup(groupPerson.userId).then(function (response) {

                        $scope.PARENT_MODEL.groups.usersForGroup = response.data;
                        $scope.PARENT_MODEL.groups.currentGroup = groupPerson.userId;

                        viewUsersInGroupDialog.openModal($scope);
                    });
                };

                /**
                 * Remove a group user from the dataset.
                 */
                $scope.viewUsersInGroupFromDialog = function (group) {

                    SecurityServices.getAllUserForGroup(group).then(function (response) {

                        $scope.PARENT_MODEL.groups.usersForGroup = response.data;
                        $scope.PARENT_MODEL.groups.currentGroup = group;

                        viewUsersInGroupDialog.openModal($scope);
                    });
                };

                /**
                 * Sets the selected group
                 */
                $scope.selectGroupHandler = function (groupItem) {
                    $scope.PARENT_MODEL.groups.selectedGroup = groupItem;
                    $log.debug("selected group is " + groupItem);
                };

                /**
                 * Checks if the list of avaliable groups are valid to enable the ok button
                 */
                $scope.isGroupListValid = function () {
                    return $scope.PARENT_MODEL.groups.allGroups.length > 0;
                };

                /**
                 * Checks if the group dialog box is valid
                 */
                $scope.isGroupDialogValid = function () {
                    return $scope.isGroupListValid();
                };

                /**
                 * Gets all the groups from the server and sets the local model
                 */
                $scope.getAllGroups = function () {
                    return SecurityServices.listAllGroups().then(function (response) {
                        $scope.PARENT_MODEL.groups.allGroups = response.data;
                        $scope.PARENT_MODEL.groups.selectedGroup = response.data[0];
                    });
                };

                /**
                 * Opens the add group permissions dialog, passes in the $scope
                 *
                 * @param {Person} arrayTo of Persons the user is been added to
                 * @param {Integer} permissionMask current person to link from
                 */
                $scope.baseOpenAddGroupDialog = function (arrayTo, permissionMask, parentScope) {
                    $scope.PARENT_MODEL.groups.permissionMask = permissionMask;
                    $scope.PARENT_MODEL.groups.arrayTo = arrayTo;

                    SecurityServices.listAllGroups().then(function (response) {

                        // remove Development and Training groups
                        $scope.PARENT_MODEL.groups.allGroups = _.filter(response.data, function (group) {
                            return !(group === trainedUserGroup || group === developmentTeamGroup);
                        });

                        // also remove ones that have already been added
                        var currentlyAddedGroups = _.chain(arrayTo)
                            .filter(function (person) {
                                return person.group;
                            }).map(function (person) {
                                return person.userId;
                            }).value();

                        $scope.PARENT_MODEL.groups.allGroups = _.difference($scope.PARENT_MODEL.groups.allGroups, currentlyAddedGroups);
                        if ($scope.PARENT_MODEL.groups.allGroups.length > 0) {
                            $scope.PARENT_MODEL.groups.selectedGroup = $scope.PARENT_MODEL.groups.allGroups[0];
                        } else {
                            $scope.PARENT_MODEL.groups.selectedGroup = 'N/A';
                        }

                        addGroupDialog.openModal(parentScope);
                    });
                };

                /**
                 * Exports all the users for a DrugProgramme or dataset
                 *
                 * @param {String} acuityAclObjectEnum ie AcuityAclObjectEnum.DrugProgramme
                 * @param {String} acuityAclObjectId to add the newPermissionMask to
                 * @param {String} aclName name of the Acl
                 */
                $scope.baseExportGrantedUsers = function (acuityAclObjectEnum, acuityAclObjectId, aclName) {

                    return SecurityServices.getGrantedUsersForAcl(acuityAclObjectEnum, acuityAclObjectId).then(function (response) {
                        var allGrantedUsers = _.chain(response.data).sortBy(function (acuitySid) {
                            return acuitySid.user.fullName;
                        }).map(function (acuitySid) {
                            return {
                                Name: acuitySid.user.fullName,
                                Prid: acuitySid.user.userId
                            };
                        }).value();

                        var name = aclName.replace(/ /g, "_");

                        alasql('SELECT * INTO XLS("' + name + '-Users.xls", {headers:true}) FROM ?', [allGrantedUsers]);
                    });
                };
            }
        ])

    /**
     * Model object for all the child Acl controllers ie MyAnalyticsDrugProgrammeAclController
     */
    .factory('ChildModel', ['Permissions', function (Permissions) {

        /**
         * New ChildModel object for MyAnalyticsDrugProgrammeAclController
         *
         * @returns {ChildModel}
         */
        function ChildModel () {
            // Display loading image
            this.loading = false;
            // is the page showing the info or edit or edit_views page
            this.pageType = 'info';
            // data of the acl drugProgramme
            this.aclInfo = null;
            // Edit user filter, default showBy is name
            this.searchUsersTerm = {name: "", showBy: "name"};
            // Users permissions on this object
            this.permissions = new Permissions();
            // Allows updating of all user view permissions so they dont acuity to model changes
            this.updatingUserViewPermission = false;
        }

        return ChildModel;
    }])
    /**
     * Model object for the MyAnalyticsController
     */
    .factory('ParentModel', ['SecurityModel', function (SecurityModel) {
        /**
         * New ParentModel object for MyAnalyticsController
         *
         * @returns {ParentModel}
         */
        var ParentModel = {
            // define the search term for the search input box for Drug Programmes, Studies and datasets
            searchTerm: {
                name: ""
            },
            // list of all Acls from the server
            allAcls: {
                drugProgrammes: SecurityModel.listDrugProgrammes(),
                clinicalStudies: SecurityModel.listClinicalStudies(),
                datasets: SecurityModel.listDatasets()
            },
            // currently selected Acl id for each Drug Programme, Study Vis, used to
            // highlight to row on the accordian
            selectedAcl: {
                drugProgrammeId: 0,
                clinicalStudyId: 0,
                datasetId: 0
            },
            //keep record of the last removed array
            dropRedo: {
                lastPersonArray: [],
                lastPerson: null
            },
            /**
             * Used in counting the number of filtered search acls per search term
             *
             * @type {{drugProgrammes: Number, studies: Number, datasets: Number}}
             */
            filteredAclsCounts: {
                drugProgrammes: SecurityModel.listDrugProgrammes().length,
                studies: SecurityModel.listClinicalStudies().length,
                datasets: SecurityModel.listDatasets().length
            },
            // dataset display by, either by drug programme or moduleType
            displayByDrugProgramme: true,
            /**
             * Toggles the selected analytics menu item
             *
             * Drug Program 0, Studies 1, Vis 2 row index for highlighting selected accordion
             */
            selectedRow: {id: 2}, // default vis
            //
            importUserPermissions: {
                permissionMask: 0,
                /* input of the list of csv items for input list.
                 * in the format of userid/prid, (optional, default true) granting
                 *
                 * ie
                 * ksnsdsd,true
                 * fksdhs34
                 * sd3545,false
                 */
                inputString: ''
            },
            // model for adding scheduled permissions
            addScheduledUserPermission: {
                // person to be added with permission
                personToAdd: null,
                // the person array they are been moving from
                arrayTo: [],
                // new permission for personToAdd
                newPermissionMask: null,
                //
                isOpen: false,
                // start date of new permission
                start: null,
                // endt date of new permission
                end: null,
                // errors with start and end dates (ie start < end)
                errroMessage: ""
            },
            groups: {
                // currently  selected group
                selectedGroup: "",
                // array that
                arrayTo: null,
                // list of all groups from server
                allGroups: [],
                // to be added for group
                permissionMask: null,
                // list of users in a group
                usersForGroup: [],
                // is the group dialog adding a normal or scheduled permission
                isScheduled: false,
                // for adding drug groups, disable showing scheduled section
                showScheduled: true
            },
            grantedUsers: {
                allGrantedUsers: []
            },
            allViewExtraPackagePermissions: _.map(SecurityModel.getViewExtraPackagePermissons(), 'mask')
        };

        return ParentModel;
    }])

    .controller('MyAnalyticsDrugProgrammeAclController',
        ['$scope', 'ChildModel', 'SecurityServices', 'AcuityAclObjectEnum', 'AcuityAclObjectEnumService', 'MyAnalyticsUtils',
            'importUserPermissionsDialog', 'Person', 'viewUsersInAclDialog', 'SecurityModel',
            function ($scope, ChildModel, SecurityServices, AcuityAclObjectEnum, AcuityAclObjectEnumService, MyAnalyticsUtils,
                      importUserPermissionsDialog, Person, viewUsersInAclDialog, SecurityModel) {

                //
                //  Acl DrugProgramme passed into scope by ngrepeat called drugProgramme
                //

                $scope.MODEL = new ChildModel();

                /**
                 * Uses the generic getEditUserInfo from parent controller.
                 */
                $scope.loadUserPermissions = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    MyAnalyticsUtils.populateAclPermissions($scope.MODEL.permissions,
                        AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, function () {
                        $scope.MODEL.loading = false;
                        $scope.MODEL.pageType = 'edit';
                    });
                };

                $scope.loadUserViewPermissions = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    MyAnalyticsUtils.populateAclPermissions($scope.MODEL.permissions,
                        AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id,
                        function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.isEditViewPage = true;
                            $scope.MODEL.pageType = 'edit_views';
                        });
                };

                /**
                 * Uses the generic onDrop from parent controller.
                 *
                 * See parent $scope.onDrop for parameters
                 */
                $scope.onDropAddUser = function ($data, array, newPermissionMask) {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    $scope.baseOnDrop($data, array, newPermissionMask,
                        AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id,
                        function () {
                            $scope.MODEL.loading = false;
                        });
                };

                /**
                 * Uses the generic onDrop from parent controller for data owners.  But also grabs the old dataOwner and removes them
                 * from the dataOwners and adds them to the trainedUsers
                 *
                 * See parent $scope.onDrop for parameters
                 */
                $scope.onDropReplaceDataOwner = function ($data, array, newPermissionMask) {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";
                    // keep reference to oldDataOwner and add back to trained users if all goes ok
                    var oldDataOwner = $scope.MODEL.permissions.dataOwners[0];

                    $scope.baseOnDrop($data, array, newPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, function () {

                        if (!oldDataOwner.isEmpty()) {
                            // remove oldDataOwner from dataOwners
                            $scope.dropSuccessHandler(oldDataOwner, $scope.MODEL.permissions.dataOwners);
                            // add oldDataOwner back to trained users
                            MyAnalyticsUtils.addPersonToList(oldDataOwner, $scope.MODEL.permissions.trainedUsers);
                        }

                        $scope.MODEL.loading = false;
                    });
                };

                /**
                 * Gets the drug programme info from the server using the scope variable drugProgramme
                 * passed in from the ng-repeat
                 */
                $scope.loadDrugProgramme = function () {
                    $scope.MODEL.loading = true;

                    //set parent scope selected Drug id
                    $scope.PARENT_MODEL.selectedAcl.drugProgrammeId = $scope.drugProgramme.id;
                    SecurityServices.getDrugProgramme(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id)
                        .then(function (response) {
                            $scope.MODEL.aclInfo = response.data;
                        }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.pageType = 'info';
                        }
                    );
                };

                /**
                 * A Person object requested to swap their granted permission
                 *
                 * @param {Person} person added to be added to the array
                 * @param {Integer} permissionMask new permission mask for the person
                 */
                $scope.swapUserGrantedPermission = function (person, permissionMask) {

                    $scope.MODEL.loading = true;

                    SecurityServices.swapUserPermission(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, person, permissionMask, !person.granted)
                        .then(function (response) {
                            person.granted = !person.granted;
                        }).finally(function () {
                        $scope.MODEL.loading = false;
                    });
                };

                /**
                 * Swaps all the users permission from the personArray to either granted or denied
                 *
                 * @param {Person} personArray of people in the list
                 * @param {String} permissionMask new permission mask for the person
                 * @param {Boolean} granted or deny the permission for all the users
                 */
                $scope.swapAllUsersPermissions = function (personArray, permissionMask, granted) {
                    $scope.MODEL.loading = true;

                    SecurityServices.swapAllUsersPermissions(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, personArray, permissionMask, granted, true)
                        .then(function (response) {
                            // set all as granted
                            _.forEach(personArray, function (person) {
                                person.granted = granted;
                            });
                        }).finally(function () {
                        $scope.MODEL.loading = false;
                    });
                };

                /**
                 * Calls the importUsersPermissions_ in the parent controller
                 */
                $scope.importUsersPermissions = function () {

                    $scope.MODEL.loading = true;

                    var requestPersonArray = $scope.generatePersonArray($scope.PARENT_MODEL.importUserPermissions.inputString);

                    return $scope.baseImportUsersPermissions(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, requestPersonArray,
                        $scope.PARENT_MODEL.importUserPermissions.permissionMask,
                        $scope.MODEL.permissions.authorisedUsers,
                        //remove added users from trained users list
                        function () {
                            $scope.PARENT_MODEL.importUserPermissions.inputString = '';
                            $scope.MODEL.loading = false;
                        },
                        $scope.MODEL.permissions.trainedUsers
                    )
                        .then(function () {
                            return true;
                        }, function () {
                            return false;
                        });
                };

                /**
                 * Opens the import user permissions dialog, passes in the $scope
                 *
                 * @param {Integer} permissionMask current person to link from
                 */
                $scope.openImportUsersPermissionsDialog = function (permissionMask) {
                    $scope.PARENT_MODEL.importUserPermissions.inputString = '';
                    $scope.PARENT_MODEL.importUserPermissions.permissionMask = permissionMask;

                    importUserPermissionsDialog.openModal($scope);
                };

                /////////////////////////////////////////
                // Add Group functions
                /////////////////////////////////////////

                /**
                 * Adds a group user to the drug programme.
                 *
                 * This is called from the dialog box
                 */
                $scope.addGroupPermission = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";
                    var requestArrayTo = $scope.PARENT_MODEL.groups.arrayTo;
                    var requestPermissionMask = $scope.PARENT_MODEL.groups.permissionMask;
                    var requestGroup = $scope.PARENT_MODEL.groups.selectedGroup;

                    var groupPerson = Person.groupPerson(requestGroup);

                    return $scope.baseOnDrop(groupPerson, requestArrayTo, requestPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, function () {
                        $scope.MODEL.loading = false;
                        return true;
                    }).then(function () {
                        $scope.baseInit();
                        return true;
                    });
                };

                /**
                 * Remove a group or user from the dataset permission list
                 */
                $scope.removePermission = function (person, arrayFrom) {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    $scope.baseOnDrop(person, $scope.MODEL.permissions.trainedUsers, '0', AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, function () {
                        $scope.MODEL.loading = false;
                        MyAnalyticsUtils.removeFromUsers([person], arrayFrom); // remove from list
                    });
                };

                $scope.openAddGroupDialog = function (arrayTo, permissionMask) {
                    $scope.PARENT_MODEL.groups.showScheduled = false;
                    $scope.baseOpenAddGroupDialog(arrayTo, permissionMask, $scope);
                };

                /**
                 * Shows all the users for the DrugProgramme
                 */
                $scope.viewGrantedUsers = function () {

                    $scope.MODEL.loading = true;

                    SecurityServices.getGrantedUsersForAcl(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id).then(function (response) {
                        $scope.PARENT_MODEL.grantedUsers.allGrantedUsers = response.data;
                        $scope.MODEL.loading = false;
                        $scope.acl = $scope.drugProgramme;
                        $scope.isDataset = false;

                    viewUsersInAclDialog.openModal($scope);
                    });
                };

                /**
                 * Exports all the users for the DrugProgramme
                 */
                $scope.exportGrantedUsers = function () {

                    $scope.MODEL.loading = true;

                    $scope.baseExportGrantedUsers(
                        AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type),
                        $scope.drugProgramme.id,
                        $scope.MODEL.aclInfo.drugProgramme
                    ).then(function () {
                        $scope.MODEL.loading = false;
                    });
                };

                /**
                 * Saves the current permissions to the server
                 */
                $scope.saveViewPackagePermissions = function (authorisedUser) {

                    if (!$scope.MODEL.updatingUserViewPermission) {

                        $scope.MODEL.loading = true;

                        SecurityServices.setViewPackagePermission(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, authorisedUser)
                            .then(function (response) {
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.updatingUserViewPermission = false;
                        });
                    }
                };

                /**
                 * Saves the current permissions to the server
                 */
                $scope.saveViewExtraPackagePermissions = function (authorisedUser) {

                    if (!$scope.MODEL.updatingUserViewPermission) {

                        $scope.MODEL.loading = true;

                        SecurityServices.setViewExtraPackagePermission(AcuityAclObjectEnumService.getFromClass($scope.drugProgramme.type), $scope.drugProgramme.id, authorisedUser)
                            .then(function (response) {
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.updatingUserViewPermission = false;
                        });
                    }
                };

                $scope.hasMachineInsights = function() {
                    return SecurityModel.getViewExtraPackagePermissons()
                        .filter((viewPackage) => viewPackage.name === 'VIEW_MACHINE_INSIGHTS_PACKAGE')
                        .length > 0
                }
            }
        ])

    .controller('MyAnalyticsClinicalStudyAclController',
        ['$scope', 'ChildModel', 'SecurityServices', 'AcuityAclObjectEnum', 'AcuityAclObjectEnumService', 'MyAnalyticsUtils',
            function ($scope, ChildModel, SecurityServices, AcuityAclObjectEnum, AcuityAclObjectEnumService, MyAnalyticsUtils) {
                //
                //  Acl Clinical Study passed into scope by ngrepeat called study
                //

                $scope.MODEL = new ChildModel();

                /**
                 * Uses the generic getEditUserInfo from parent controller.
                 */
                $scope.loadUserPermissions = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    MyAnalyticsUtils.populateAclPermissions($scope.MODEL.permissions, AcuityAclObjectEnumService.getFromClass($scope.study.type), $scope.study.id, function () {
                        $scope.MODEL.loading = false;
                        $scope.MODEL.pageType = 'edit';
                    });
                };

                /**
                 * Uses the generic onDrop from parent controller.
                 *
                 * See parent $scope.onDrop for parameters
                 */
                $scope.onDropAddUser = function ($data, array, newPermissionMask) {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    $scope.onDrop($data, array, newPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.study.type), $scope.study.id, function () {
                        $scope.MODEL.loading = false;
                    });
                };

                /**
                 * Gets the Clinical Study info from the server using the scope variable study
                 * passed in from the ng-repeat
                 */
                $scope.loadClinicalStudy = function () {
                    $scope.MODEL.loading = true;

                    //set parent scope selected clinicalStudyId
                    $scope.PARENT_MODEL.selectedAcl.clinicalStudyId = $scope.study.id;

                    SecurityServices.getClinicalStudy(AcuityAclObjectEnumService.getFromClass($scope.study.type), $scope.study.id)
                        .then(function (response) {
                            $scope.MODEL.aclInfo = response.data;
                        }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.pageType = 'info';
                        }
                    );
                };
            }
        ])

    .controller('MyAnalyticsDatasetAclController',
        ['$scope', 'ChildModel', 'SecurityServices', 'AcuityAclObjectEnum', 'AcuityAclObjectEnumService', 'MyAnalyticsUtils',
            'importUserPermissionsDialog', 'addScheduledUserDialog', 'Person', 'viewUsersInAclDialog', '$filter', 'ngDialog',
            '$q', 'SecurityModel',
            function ($scope, ChildModel, SecurityServices, AcuityAclObjectEnum, AcuityAclObjectEnumService, MyAnalyticsUtils,
                      importUserPermissionsDialog, addScheduledUserDialog, Person, viewUsersInAclDialog, $filter, ngDialog,
                      $q, SecurityModel) {
                //
                //  Acl Dataset passed into scope by ngrepeat called viz
                //

                $scope.MODEL = new ChildModel();

                /**
                 * Uses the generic getEditUserInfo from parent controller.
                 */
                $scope.loadUserPermissions = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    MyAnalyticsUtils.populateAclPermissions($scope.MODEL.permissions, AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                        $scope.MODEL.loading = false;
                        $scope.MODEL.pageType = 'edit';
                    });
                };

                $scope.loadUserViewPermissions = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    MyAnalyticsUtils.populateAclPermissions($scope.MODEL.permissions, AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                        $scope.MODEL.loading = false;
                        $scope.MODEL.isEditViewPage = true;
                        $scope.MODEL.pageType = 'edit_views';
                    });
                };

                /**
                 * Saves the current permissions to the server
                 */
                $scope.saveViewPackagePermissions = function (authorisedUser) {

                    if (!$scope.MODEL.updatingUserViewPermission) {

                        $scope.MODEL.loading = true;

                        SecurityServices.setViewPackagePermission(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, authorisedUser)
                            .then(function (response) {
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.updatingUserViewPermission = false;
                        });
                    }
                };

                /**
                 * Saves the current permissions to the server
                 */
                $scope.saveViewExtraPackagePermissions = function (authorisedUser) {

                    if (!$scope.MODEL.updatingUserViewPermission) {

                        $scope.MODEL.loading = true;

                        SecurityServices.setViewExtraPackagePermission(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, authorisedUser)
                            .then(function (response) {
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.updatingUserViewPermission = false;
                        });
                    }
                };


                $scope.hasMachineInsights = function() {
                    return SecurityModel.getViewExtraPackagePermissons()
                        .filter((viewPackage) => viewPackage.name === 'VIEW_MACHINE_INSIGHTS_PACKAGE')
                        .length > 0
                }

                /**
                 * Uses the generic onDrop from parent controller.
                 *
                 * See parent $scope.onDrop for parameters
                 */
                $scope.onDropAddUser = function ($data, array, newPermissionMask) {
                    if (newPermissionMask != '0') {
                        $scope.checkLockDownConfirm(true, true).then(function (success) {
                            $scope.MODEL.loading = true;
                            $scope.MODEL.searchUsersTerm.name = "";
                            $scope.baseOnDrop($data, array, newPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                                $scope.MODEL.loading = false;
                            });
                        }, function (cancel) {
                            $scope.cancel();
                        });
                    } else {
                        $scope.MODEL.loading = true;
                        $scope.MODEL.searchUsersTerm.name = "";
                        $scope.baseOnDrop($data, array, newPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                            $scope.MODEL.loading = false;
                        });
                    }

                };

                /**
                 * Gets the Dataset info from the server using the scope variable viz
                 * passed in from the ng-repeat
                 */
                $scope.loadDataset = function () {
                    $scope.MODEL.loading = true;

                    //set parent scope selected viz id
                    $scope.PARENT_MODEL.selectedAcl.datasetId = $scope.vis.id;
                    SecurityServices.getDataset(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id)
                        .then(function (response) {
                            $scope.MODEL.aclInfo = response.data;
                        })
                        .finally(function () {
                            $scope.MODEL.loading = false;
                            $scope.MODEL.pageType = 'info';
                        });
                };

                /**
                 * A Person object requested to swap their granted permission
                 *
                 * @param {Person} person added to be added to the array
                 * @param {Integer} permissionMask new permission mask for the person
                 */
                $scope.swapUserGrantedPermission = function (person, permissionMask) {

                    $scope.checkLockDownConfirm(true, true).then(function (success) {
                        if (person.isActivePermission()) {
                            $scope.MODEL.loading = true;

                            SecurityServices.swapUserPermission(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, person, permissionMask, !person.granted)
                                .then(function (response) {
                                    person.granted = !person.granted;
                                }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        }
                    });
                };

                /**
                 * Swaps all the users permission from the personArray to either granted or denied
                 *
                 * @param {Person} personArray of people in the list
                 * @param {String} permissionMask new permission mask for the person
                 * @param {Boolean} granted or deny the permission for all the users
                 */
                $scope.swapAllUsersPermissions = function (personArray, permissionMask, granted) {

                    $scope.checkLockDownConfirm(true, true).then(function (success) {
                        $scope.MODEL.loading = true;

                        // remove user if they are scheduled to be added and not already added
                        var removedScheduledArray = _.filter(personArray, function (person) {
                            return Person.isActivePermission(person);
                        });

                        SecurityServices.swapAllUsersPermissions(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, removedScheduledArray, permissionMask, granted, true)
                            .then(function (response) {
                                // set all as granted
                                _.forEach(personArray, function (person) {
                                    person.granted = granted;
                                });
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                        });
                    });
                };

                /**
                 * Calls the importUsersPermissions_ in the parent controller
                 */
                $scope.importUsersPermissions = function () {

                    $scope.checkLockDownConfirm(true, true).then(function (success) {
                        $scope.MODEL.loading = true;

                        var requestPersonArray = $scope.generatePersonArray($scope.PARENT_MODEL.importUserPermissions.inputString);

                        return $scope.baseImportUsersPermissions(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, requestPersonArray,
                            $scope.PARENT_MODEL.importUserPermissions.permissionMask,
                            $scope.MODEL.permissions.authorisedUsers,
                            function () {
                                $scope.PARENT_MODEL.importUserPermissions.inputString = '';
                                $scope.MODEL.loading = false;
                            },
                            $scope.MODEL.permissions.trainedUsers
                        )
                            .then(function (response) {
                                return true;
                            }, function (response) {
                                return false;
                            });
                    });
                };

                /**
                 * Opens the import user permissions dialog, passes in the $scope
                 *
                 * @param {Integer} permissionMask current person to link from
                 */
                $scope.openImportUsersPermissionsDialog = function (permissionMask) {
                    $scope.PARENT_MODEL.importUserPermissions.inputString = '';
                    $scope.PARENT_MODEL.importUserPermissions.permissionMask = permissionMask;

                    importUserPermissionsDialog.openModal($scope);
                };

                /////////////////////////////////////////
                // Add scheduled permission functions
                /////////////////////////////////////////

                /**
                 * The person is removed from one of the list once the person is dropped to be scheduled.  But the user
                 * might press cancel on the dialog to not scheduled the users. In this case the
                 * perons needed to be readded to the list they were removed from
                 *
                 * @returns {Boolean} is function worked
                 */
                $scope.cancel = function () {
                    $scope.redoLastDrop();
                    $scope.baseInit();

                    return true;
                };

                /**
                 * Validates the start and end dates of the scheduled permission
                 *
                 * @returns {Boolean} if valid dates
                 */
                $scope.isStartEndDateValid = function () {
                    var now = new Date();
                    var start = $scope.PARENT_MODEL.addScheduledUserPermission.start;
                    var end = $scope.PARENT_MODEL.addScheduledUserPermission.end;
                    $scope.PARENT_MODEL.addScheduledUserPermission.errorMessage = "";

                    if (start && start < now) {
                        $scope.PARENT_MODEL.addScheduledUserPermission.errorMessage = "Start date can't be in the past";
                        return false;
                    }

                    if (end && end < now) {
                        $scope.PARENT_MODEL.addScheduledUserPermission.errorMessage = "End date can't be in the past";
                        return false;
                    }

                    if (!end || !start) {
                        return false;
                    }
                    if (end < start) {
                        $scope.PARENT_MODEL.addScheduledUserPermission.errorMessage = "End date can't be before start date";
                        return false;
                    }

                    $scope.PARENT_MODEL.addScheduledUserPermission.errorMessage = "";
                    return true;
                };

                /**
                 * Sets the end date of the calendar when the user opens it, depending on what the user
                 * had already set for the start date.
                 *
                 * @param {Event} e event
                 */
                $scope.openEndCalendar = function (e) {
                    e.preventDefault();
                    e.stopPropagation();

                    var today = new Date();
                    var todayPlus1Days = new Date(today);
                    todayPlus1Days.setDate(today.getDate() + 1);
                    todayPlus1Days.setHours(0, 0, 0, 0);

                    var start = $scope.PARENT_MODEL.addScheduledUserPermission.start;
                    var end = $scope.PARENT_MODEL.addScheduledUserPermission.end;

                    if (end === null) {
                        if (start === null || start <= today) {
                            $scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus1Days;
                        } else {
                            var startPlus1Days = new Date(start);
                            startPlus1Days.setDate(startPlus1Days.getDate() + 1);
                            startPlus1Days.setHours(0, 0, 0, 0);

                            $scope.PARENT_MODEL.addScheduledUserPermission.end = startPlus1Days;
                        }
                    }

                    $scope.PARENT_MODEL.addScheduledUserPermission.isOpenEnd = true;
                };

                /**
                 * Sets the start date of the calendar when the user opens it.
                 *
                 * @param {Event} e event
                 */
                $scope.openStartCalendar = function (e) {
                    e.preventDefault();
                    e.stopPropagation();

                    if ($scope.PARENT_MODEL.addScheduledUserPermission.start === null) {
                        var today = new Date();
                        var todayPlus1Day = new Date(today);
                        todayPlus1Day.setDate(today.getDate() + 1);
                        todayPlus1Day.setHours(0, 0, 0, 0);

                        $scope.PARENT_MODEL.addScheduledUserPermission.start = todayPlus1Day;
                    }

                    $scope.PARENT_MODEL.addScheduledUserPermission.isOpenStart = true;
                };

                /**
                 * Uses the generic onDrop from parent controller.
                 *
                 * See parent $scope.onDrop for parameters
                 */
                $scope.openAddScheduledUserDialog = function ($data, arrayTo, newPermissionMask) {

                    $scope.PARENT_MODEL.addScheduledUserPermission.personToAdd = $data;
                    $scope.PARENT_MODEL.addScheduledUserPermission.arrayTo = arrayTo;
                    $scope.PARENT_MODEL.addScheduledUserPermission.newPermissionMask = newPermissionMask;

                    addScheduledUserDialog.openModal($scope);
                };

                /**
                 * Adds a scheduled user to the dataset.
                 *
                 * This is called from the dialog box
                 */
                $scope.addScheduledUser = function () {

                    $scope.checkLockDownConfirm(true, true).then(function (success) {
                        $scope.MODEL.loading = true;

                        var requestPersonToAdd = $scope.PARENT_MODEL.addScheduledUserPermission.personToAdd;
                        var requestArrayTo = $scope.PARENT_MODEL.addScheduledUserPermission.arrayTo;
                        var requestPermissionMask = $scope.PARENT_MODEL.addScheduledUserPermission.newPermissionMask;
                        var requestStart = $scope.PARENT_MODEL.addScheduledUserPermission.start;
                        var requestEnd = $scope.PARENT_MODEL.addScheduledUserPermission.end;

                        return $scope.addScheduledSid(requestPersonToAdd, requestPermissionMask, requestStart, requestEnd, requestArrayTo);
                    });
                };

                /**
                 * Adds a scheduled sid to the dataset.
                 *
                 * This is to add a person or group to the permisson list.
                 */
                $scope.addScheduledSid = function (sidPerson, permissionMask, start, end, arrayTo) {
                    return SecurityServices.addScheduledSid(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, sidPerson, permissionMask,
                        start, end)
                        .then(function (response) {

                            // need to add a person to the list
                            var jobDetails = response.data;
                            sidPerson.permissionJob = jobDetails;
                            MyAnalyticsUtils.addPersonToList(sidPerson, arrayTo);

                            $scope.baseInit();
                            $scope.MODEL.loading = false;

                            return true;
                        }, function (response) {
                            $scope.baseInit();
                            $scope.MODEL.loading = false;

                            return false;
                        });
                };

                /////////////////////////////////////////
                // Add Group functions
                /////////////////////////////////////////

                /**
                 * Overrides the base isGroupsValid if its if its scheduled then called isValid()
                 */
                $scope.isScheduledGroupsValid = function () {
                    if ($scope.PARENT_MODEL.groups.isScheduled) {
                        var scheduledValid = $scope.isStartEndDateValid();
                        if (scheduledValid) {
                            return $scope.isGroupListValid();
                        } else {
                            return false;
                        }
                    } else {
                        return $scope.isGroupListValid();
                    }
                };

                /**
                 * Validates the group permisson add button, if its scheduled then called isValid()
                 *
                 * @returns {Boolean} if valid
                 */
                $scope.isGroupDialogValid = function () {
                    if ($scope.PARENT_MODEL.groups.isScheduled) {
                        return $scope.isStartEndDateValid();
                    } else {
                        return $scope.isGroupListValid();
                    }
                };

                /**
                 * Adds a group user to the dataset.  Either scheduled or unscheduled
                 *
                 * This is called from the dialog box
                 */
                $scope.addGroupPermission = function () {
                    return $scope.checkLockDownConfirm(false, true).then(function (success) {
                        if ($scope.PARENT_MODEL.groups.isScheduled) {
                            return $scope.addScheduledGroupPermission();
                        } else {
                            return $scope.addUnscheduledGroupPermission();
                        }
                    });
                };

                /**
                 * Adds a scheduled group to the dataset.
                 */
                $scope.addScheduledGroupPermission = function () {
                    $scope.MODEL.loading = true;

                    var requestArrayTo = $scope.PARENT_MODEL.groups.arrayTo;
                    var requestPermissionMask = $scope.PARENT_MODEL.groups.permissionMask;
                    var requestGroup = $scope.PARENT_MODEL.groups.selectedGroup;
                    var requestStart = $scope.PARENT_MODEL.addScheduledUserPermission.start;
                    var requestEnd = $scope.PARENT_MODEL.addScheduledUserPermission.end;

                    var groupPerson = Person.groupPerson(requestGroup);

                    return $scope.addScheduledSid(groupPerson, requestPermissionMask, requestStart, requestEnd, requestArrayTo);
                };

                /**
                 * Adds a normal unscheduled group user to the dataset.
                 */
                $scope.addUnscheduledGroupPermission = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    var requestArrayTo = $scope.PARENT_MODEL.groups.arrayTo;
                    var requestPermissionMask = $scope.PARENT_MODEL.groups.permissionMask;
                    var requestGroup = $scope.PARENT_MODEL.groups.selectedGroup;

                    var groupPerson = Person.groupPerson(requestGroup);

                    return $scope.baseOnDrop(groupPerson, requestArrayTo, requestPermissionMask, AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                        $scope.MODEL.loading = false;
                        return true;
                    }).then(function () {
                        $scope.baseInit();
                        return true;
                    });
                };

                $scope.openAddGroupDialog = function (arrayTo, permissionMask) {
                    $scope.PARENT_MODEL.groups.showScheduled = true;
                    $scope.baseOpenAddGroupDialog(arrayTo, permissionMask, $scope);
                };

                /**
                 * Shows all the users for the Dataset
                 */
                $scope.viewGrantedUsers = function () {

                    $scope.MODEL.loading = true;

                    SecurityServices.getGrantedUsersForAcl(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id).then(function (response) {
                        $scope.PARENT_MODEL.grantedUsers.allGrantedUsers = response.data;
                        $scope.MODEL.loading = false;
                        $scope.acl = $scope.vis;
                        $scope.isDataset = true;

                        viewUsersInAclDialog.openModal($scope);
                    });
                };

                /**
                 * Exports all the users for the Dataset
                 */
                $scope.exportGrantedUsers = function () {

                    $scope.MODEL.loading = true;

                    var cleanedName = $filter("cleanModuleName")($scope.MODEL.aclInfo.name);

                    $scope.baseExportGrantedUsers(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, cleanedName)
                        .then(function () {
                            $scope.MODEL.loading = false;
                        });
                };

                /**
                 * Removes a user/group from the dataset.
                 */
                $scope.removePermission = function (person, arrayFrom) {

                    var isUser = person && !person.group;

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";

                    $scope.baseOnDrop(person, $scope.MODEL.permissions.trainedUsers, '0', AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, function () {
                        $scope.MODEL.loading = false;
                        MyAnalyticsUtils.removeFromUsers([person], arrayFrom); // remove from list
                    });
                };

                /**
                 * Sets lockdown for the dataset
                 */
                $scope.setLockdown = function () {

                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";
                    $scope.vis.lockdown = false;
                    
                    return $scope.unsetLockDownConfirm().then(function (success) {

                            SecurityServices.setLockdown(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, $scope.vis.lockdown)
                                .then(function (response) {
                                }).finally(function () {
                                $scope.MODEL.loading = false;
                            });
                        },
                        function (data) {
                            // Cancelled, so reverse the lockdown state
                            $scope.vis.lockdown = true;
                            //$scope.MODEL.loading = false;
                        });
                };

                /**
                 * Gets the lockdown access for the dataset
                 */
                $scope.viewLockdownUserAccess = function () {
                    $scope.MODEL.loading = true;

                    SecurityServices.getLockdownAccess(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id)
                        .then(function (response) {
                            /* jshint ignore:start */
                            var hiddenElement = document.createElement('a');

                            hiddenElement.href = 'data:attachment/csv,' + encodeURI(response.data);
                            hiddenElement.target = '_blank';
                            hiddenElement.download = 'lockdown.csv';
                            hiddenElement.click();
                            /* jshint ignore:end */
                        }).finally(function () {
                        $scope.MODEL.loading = false;
                    });

                };

                /**
                 * Presents a confirm message to the user when changing permission
                 */
                $scope.checkLockDownConfirm = function (isUser, granting) {

                    var messageforUser = isUser ? 'the user is' : 'all the users in the group are';
                    var meassgeForGranting = granting ? 'grant' : 'remove';

                    if ($scope.vis.lockdown) {
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
                            '           <div class="col-md-9">' +
                            'You are about to ' + meassgeForGranting + ' access to ' + $scope.vis.name + '. <br/><br/>' +
                            'Granting a user access who is not on the Insiders List could result in criminal charges being brought against the system owner.<br/><br/>' +
                            'Ensure you have confirmation from the Drug Programme Data Owner or Authoriser ' + messageforUser + ' on the Insiders List before proceeding. <br/><br/>' +
                            '<button ng-click="closeThisDialog()">Cancel</button> ' +
                            '<button ng-click="confirm()">Confirm</button> ' +
                            '       </div>' +
                            '       </div>' +
                            '   </div>' +
                            '</div>'
                        });
                    } else {
                        var deferred = $q.defer();
                        deferred.resolve('not in lockdown, so ok');
                        return deferred.promise;
                    }
                };

                /**
                 * Presents a confirm message to the user when un setting lockdown
                 */
                $scope.unsetLockDownConfirm = function () {

                    if (!$scope.vis.lockdown) {
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
                            '<b>Are you sure you want to remove lockdown?</b><br/><br/>' +
                            'You cannot apply lockdown to a dataset from VA Security. A dataset has to be loaded with the lockdown status.<br/><br/>' +
                            'Removing lockdown status stops the lockdown audit from being recorded and lockdown warning messages from being displayed.<br/><br/>' +
                            'Ensure you have confirmation from the Drug Programme Data Owner or Authoriser that ' + $scope.vis.name + ' is no longer in lockdown. <br/><br/>' +
                            '<button ng-click="closeThisDialog()">Cancel</button> ' +
                            '<button ng-click="confirm()">Confirm</button> ' +
                            '       </div>' +
                            '       </div>' +
                            '   </div>' +
                            '</div>'
                        });
                    } else {
                        var deferred = $q.defer();
                        deferred.resolve('not in lockdown, so ok');
                        return deferred.promise;
                    }
                };

                /**
                 * Sets inherited permission for the dataset
                 */
                $scope.setInheritPermissionConfirm = function () {
                    if ($scope.vis.lockdown) {
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
                            'You are about to inherit permissions. This will grant all Drug Programme users access to ' + $scope.vis.name + ', which is in lockdown.<br/><br/>' +
                            'Granting a user access who is not on the Insiders List could result in criminal charges being brought against the system owner.<br/><br/>' +
                            'Ensure you have confirmation from the Drug Programme Data Owner or Authoriser all Drug Programme users are on the Insiders List before proceeding. <br/><br/>' +
                            '<button ng-click="closeThisDialog()">Cancel</button> ' +
                            '<button ng-click="confirm()">Confirm</button> ' +
                            '       </div>' +
                            '       </div>' +
                            '   </div>' +
                            '</div>'
                        });
                    } else {
                        var deferred = $q.defer();
                        deferred.resolve('not in lockdown, so ok');
                        return deferred.promise;
                    }
                };

                /**
                 * Sets inherited permission for the dataset
                 */
                $scope.setInheritPermission = function () {
                    $scope.MODEL.loading = true;
                    $scope.MODEL.searchUsersTerm.name = "";
                    return $scope.setInheritPermissionConfirm().then(function (success) {
                        SecurityServices.setInheritPermission(AcuityAclObjectEnumService.getFromClass($scope.vis.type), $scope.vis.id, $scope.vis.inherited)
                            .then(function (response) {
                            }).finally(function () {
                            $scope.MODEL.loading = false;
                        });
                    }, function (error) {
                        $scope.vis.inherited = !$scope.vis.inherited;
                    });
                };
            }
        ]);
