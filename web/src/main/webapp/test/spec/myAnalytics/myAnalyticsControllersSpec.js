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

/* jshint ignore:start */
// add dummy global alasql
var global = (function () {
    return this;
})();

alasql = function (a, b) {
    console.log("Exporting to excel");
};
/* jshint ignore:end */

describe('Controller: myAnalyticsControllers', function () {

    'use strict';

    // load the controller's module
    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.constants'));
    beforeEach(module('acuityApp.utilServices'));
    beforeEach(module('acuityApp.securityServices'));

    var spyedSecurityModel, rootScope, spyedSecurityServices, scope, deferredSuccess;

    var mockDialog = {
        openModal: function () {
        }
    };

    var mockNgDialog = {
        openConfirm: function () {
        }
    };

    // Initialize the controller and a mock scope
    beforeEach(function () {
        module(function ($provide) {
            $provide.factory('viewUsersInAclDialog', function () {
                return mockDialog;
            });
            $provide.factory('viewUsersInGroupDialog', function () {
                return mockDialog;
            });
            $provide.factory('addGroupDialog', function () {
                return mockDialog;
            });
            $provide.factory('ngDialog', function () {
                return mockNgDialog;
            });
        });
        inject(function ($rootScope, $q, SecurityModel, SecurityServices) {

            scope = $rootScope.$new();
            rootScope = $rootScope;
            spyedSecurityModel = SecurityModel;
            spyedSecurityServices = SecurityServices;

            spyOn(SecurityModel, 'listDrugProgrammes').andReturn(mockDrugProgrammes);
            spyOn(SecurityModel, 'listClinicalStudies').andReturn(mockClinicalStudies);
            spyOn(SecurityModel, 'listDatasets').andReturn(mockDatasets);

            deferredSuccess = $q.defer();
            spyOn(SecurityServices, 'getDrugProgramme').andReturn(deferredSuccess.promise);
            spyOn(SecurityServices, 'getDataset').andReturn(deferredSuccess.promise);
            spyOn(SecurityServices, 'getClinicalStudy').andReturn(deferredSuccess.promise);
            spyOn(SecurityServices, 'swapUserPermission').andReturn(deferredSuccess.promise);
            spyOn(SecurityServices, 'swapAllUsersPermissions').andReturn(deferredSuccess.promise);
            spyOn(SecurityServices, 'addScheduledSid').andReturn(deferredSuccess.promise);

            spyOn(mockNgDialog, 'openConfirm').andReturn(function () {
                var deferred = $q.defer();
                deferred.resolve('ok');
                return deferred.promise;
            });
        });
    });

    var mockDrugProgrammes = [
        {supertype: "com.acuity.va.security.acl.domain.DrugProgramme", type: "com.acuity.va.security.acl.domain.DrugProgramme", id: 29, name: "STUDY0005", permissionMask: 32, compound: "STDY4321", moduleType: null},
        {supertype: "com.acuity.va.security.acl.domain.DrugProgramme", type: "com.acuity.va.security.acl.domain.DrugProgramme", id: 30, name: "STDY0519", permissionMask: 32, compound: "STDY5321", moduleType: null},
        {supertype: "com.acuity.va.security.acl.domain.DrugProgramme", type: "com.acuity.va.security.acl.domain.DrugProgramme", id: 32, name: "STDY1929", permissionMask: 32, compound: "STDY4321", moduleType: null},
        {supertype: "com.acuity.va.security.acl.domain.DrugProgramme", type: "com.acuity.va.security.acl.domain.DrugProgramme", id: 45, name: "STDY4321", permissionMask: 32, compound: "STDY6321", moduleType: null}
    ];
    var mockClinicalStudies = [
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 100, name: "Dummy study", drugProgramme: "STDY4321", permissionMask: 32, compound: "STDY4321", moduleType: null},
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 101, name: "Dummy study 2", drugProgramme: "STDY4321", permissionMask: 64, compound: "STDY5321", moduleType: null}
    ];
    var mockDatasets = [
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 130, name: "ACUITY_Safety_STDY4321_Dummy_Instance",
            permissionMask: 32, isOpen: false, compound: "STDY4321", drugProgramme: "STDY4321", moduleType: "Safety", lockdown: true},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 131, name: "ACUITY_Tolerability_STDY4321_Dummy_Instance",
            permissionMask: 32, isOpen: false, compound: "STDY4321", drugProgramme: "STDY4321", moduleType: "Tolerability", lockdown: false},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 132, name: "ACUITY_Operational_STDY4321_Dummy_Instance",
            permissionMask: 32, isOpen: false, compound: "STDY4321", drugProgramme: "STDY4321", moduleType: "Operational", lockdown: false},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 133, name: "ACUITY_Oncology_STDY4321_Dummy_Instance",
            permissionMask: 32, isOpen: false, compound: "STDY5321", drugProgramme: "STDY4321", moduleType: "Oncology", lockdown: false},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 134, name: "ACUITY_Oncology_STDY4321_Dummy_Instance2",
            permissionMask: 32, isOpen: false, compound: "STDY5321", drugProgramme: "STDY4321", moduleType: "Oncology", lockdown: false}
    ];

    describe('MyAnalyticsControllers ..', function () {

        describe('MyAnalyticsController init', function () {
            var myAnalyticsController;

            beforeEach(inject(function ($controller, _ParentModel_, _Person_, _MyAnalyticsUtils_, _MyAnalyticsDNDUtils_) {

                myAnalyticsController = $controller('MyAnalyticsController', {
                    $scope: scope, ParentModel: _ParentModel_, SecurityModel: spyedSecurityModel, SecurityServices: spyedSecurityServices,
                    MyAnalyticsUtils: _MyAnalyticsUtils_, MyAnalyticsDNDUtils: _MyAnalyticsDNDUtils_, Person: _Person_
                });
            }));

            it('THEN the global services should be attached to the controller', function (_MyAnalyticsDNDUtils_) {

                expect(scope.MyAnalyticsDNDUtils).toBeDefined();
            });

            it('THEN the PARENT_MODEL initialisations should be set in the controller', inject(function (ParentModel) {

                expect(scope.PARENT_MODEL.searchTerm.name).toEqual("");

                expect(scope.PARENT_MODEL.allAcls.drugProgrammes).toBe(mockDrugProgrammes);
                expect(scope.PARENT_MODEL.allAcls.clinicalStudies).toBe(mockClinicalStudies);
                expect(scope.PARENT_MODEL.allAcls.datasets).toBe(mockDatasets);

                expect(scope.PARENT_MODEL.filteredAclsCounts.drugProgrammes).toBe(mockDrugProgrammes.length);
                expect(scope.PARENT_MODEL.filteredAclsCounts.studies).toBe(mockClinicalStudies.length);
                expect(scope.PARENT_MODEL.filteredAclsCounts.datasets).toBe(mockDatasets.length);

                expect(scope.PARENT_MODEL.selectedAcl.drugProgrammeId).toBe(0);
                expect(scope.PARENT_MODEL.selectedAcl.clinicalStudyId).toBe(0);
                expect(scope.PARENT_MODEL.selectedAcl.datasetId).toBe(0);

                expect(scope.PARENT_MODEL.displayByDrugProgramme).toBe(true);

                expect(scope.PARENT_MODEL.selectedRow.id).toBe(2);

                //expect(scope.PARENT_MODEL).toEqual(new ParentModel());
            }));
        });

        describe('MyAnalyticsController filtering acls', function () {
            var myAnalyticsController;

            beforeEach(inject(function ($filter, $controller, _Person_, _MyAnalyticsUtils_, _MyAnalyticsDNDUtils_) {

                myAnalyticsController = $controller('MyAnalyticsController', {
                    $scope: scope, filter: $filter, SecurityModel: spyedSecurityModel, SecurityServices: spyedSecurityServices,
                    MyAnalyticsUtils: _MyAnalyticsUtils_, MyAnalyticsDNDUtils: _MyAnalyticsDNDUtils_, Person: _Person_
                });
            }));

            it('THEN the number of acls should be correct and added to scope ', function () {
                expect(scope.PARENT_MODEL.filteredAclsCounts.drugProgrammes).toEqual(mockDrugProgrammes.length);

                scope.PARENT_MODEL.searchTerm.name = "STDY4321";
                scope.$apply();

                expect(scope.PARENT_MODEL.filteredAclsCounts.drugProgrammes).toBe(1);
                expect(scope.PARENT_MODEL.filteredAclsCounts.studies).toBe(2);
                expect(scope.PARENT_MODEL.filteredAclsCounts.datasets).toBe(5);
            });
        });


        //TODO
        //dropSuccessHandler
        //onDrop
        //getEditUserInfo
    });

    describe('MyAnalyticsDrugProgrammeAclController', function () {

        var myAnalyticsController;

        beforeEach(inject(function ($controller, _ChildModel_, _AcuityAclObjectEnum_) {

            myAnalyticsController = $controller('MyAnalyticsDrugProgrammeAclController', {
                $scope: scope, ChildModel: _ChildModel_, SecurityServices: spyedSecurityServices,
                AcuityAclObjectEnum: _AcuityAclObjectEnum_
            });
        }));

        describe('MyAnalyticsDrugProgrammeAclController init', function () {

            it('THEN the MODEL initialisations should be set in the controller', inject(function (ChildModel) {

                expect(scope.MODEL).toEqual(new ChildModel());

                expect(scope.MODEL.loading).toBe(false);
                expect(scope.MODEL.pageType).toBe('info');
                expect(scope.MODEL.aclInfo).toBe(null);
                expect(scope.MODEL.searchUsersTerm.name).toEqual("");
                expect(scope.MODEL.searchUsersTerm.showBy).toEqual("name");
                expect(scope.MODEL.permissions).toBeDefined();
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController loadDrugProgramme', function () {

            it('THEN it should set the aclInfo WHEN loadDrugProgramme is called', inject(function (ParentModel) {
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.drugProgramme = mockDrugProgrammes[0]; // mimic ng repeat for this controller that injects the ace drugProgramme

                expect(scope.MODEL.aclInfo).toBe(null);

                scope.loadDrugProgramme();

                expect(scope.MODEL.aclInfo).toBeDefined();
                expect(spyedSecurityServices.getDrugProgramme.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController loadUserPermissions', function () {
            var myAnalyticsController, httpBackend;

            var testMockAllUsers =
                    [
                        {userId: "User1", fullName: "user1 d", authorities: [], enabled: true},
                        {userId: "User2", fullName: "user2 d", authorities: [], enabled: true},
                        {userId: "User3", fullName: "user3 d", authorities: [], enabled: true},
                        {userId: "User4", fullName: "user4 d", authorities: [], enabled: true},
                        {userId: "User5", fullName: "user5 d", authorities: [], enabled: true},
                        {userId: "User6", fullName: "user6 d", authorities: [], enabled: true}
                    ];
            var testMockAcePermissions =
                    [
                        {user: {userId: "User1", fullName: "user1 d", authorities: [], enabled: true}, permissionMask: 32},
                        {user: {userId: "User2", fullName: "user2 d", authorities: [], enabled: true}, permissionMask: 229152},
                        {user: {userId: "User3", fullName: "user3 d", authorities: [], enabled: true}, permissionMask: 2147188257},
                        {user: {userId: "User4", fullName: "user4 d", authorities: [], enabled: true}, permissionMask: 30752}
                    ];

            // Initialize the controller and a mock scope
            beforeEach(inject(function (_$httpBackend_, $controller, _ChildModel_, $filter, _Person_, _MyAnalyticsUtils_, _MyAnalyticsDNDUtils_) {

                httpBackend = _$httpBackend_;

                httpBackend.when('GET', '/resources/groups/TRAINED_USER_GROUP').respond(testMockAllUsers);
                httpBackend.when('GET', '/resources/acl/DrugProgramme/DrugProgramme/29/ace').respond(testMockAcePermissions);
                httpBackend.when('GET', '/resources/acl/ClinicalStudy/ClinicalStudy/100/ace').respond(testMockAcePermissions);

                myAnalyticsController = $controller('MyAnalyticsController', {
                    $scope: scope, $filter: $filter, ChildModel: _ChildModel_, SecurityModel: spyedSecurityModel, SecurityServices: spyedSecurityServices,
                    MyAnalyticsUtils: _MyAnalyticsUtils_, MyAnalyticsDNDUtils: _MyAnalyticsDNDUtils_, Person: _Person_
                });
            }));

//            it('THEN it should get user info for Drug Programme', inject(function(ParentModel) {
//                scope.editUserLoading = true;
//                scope.PARENT_MODEL = new ParentModel(); //mimic parent scope
//                scope.drugProgramme = mockDrugProgrammes[0]; // mimic ng repeat for this controller that injects the ace drugProgramme
//
//                scope.loadUserPermissions();
//
//                httpBackend.flush();
//
//                expect(scope.allUsers.length).toBe(2);
//                expect(scope.searchUsersTerm.name).toBe("");
//                expect(_.pluck(scope.allUsers, 'userId')).toEqual(['User5', 'User6']);
//            }));

//            it('THEN it should get user info for Clinical Study', inject(function(AcuityAclObjectEnum) {
//                scope.editUserLoading = true;
//
//                scope.getEditUserInfo(AcuityAclObjectEnum.ClinicalStudy.name, 100);
//
//                httpBackend.flush();
//
//                expect(scope.allUsers.length).toBe(2);
//                expect(scope.searchUsersTerm.name).toBe("");
//                expect(_.pluck(scope.allUsers, 'userId')).toEqual(['User5', 'User6']);
//            }));
//
//            it('THEN it should get user info for Drug Programme for refreshDrugProgrammeContent', inject(function(AcuityAclObjectEnum) {
//                scope.editUserLoading = true;
//
//                scope.setDrugProgrammeEditPage(29, true); // set to edit
//
//                scope.refreshDrugProgrammeContent(29);
//
//                httpBackend.flush();
//
//                expect(scope.allUsers.length).toBe(2);
//                expect(scope.searchUsersTerm.name).toBe("");
//                expect(_.pluck(scope.allUsers, 'userId')).toEqual(['User5', 'User6']);
//            }));
//
//            it('THEN it should get user info for Clinical Study for refreshClinicalStudyContent', inject(function(AcuityAclObjectEnum) {
//                scope.editUserLoading = true;
//
//                scope.setStudyEditPage(100, true); // set to edit
//
//                scope.refreshClinicalStudyContent(100);
//
//                httpBackend.flush();
//
//                expect(scope.allUsers.length).toBe(2);
//                expect(scope.searchUsersTerm.name).toBe("");
//                expect(_.pluck(scope.allUsers, 'userId')).toEqual(['User5', 'User6']);
//            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController swapUserGrantedPermission', function () {

            it('THEN it should called swap with active user WHEN swapUserGrantedPermission is called', inject(function (Person) {
                scope.drugProgramme = mockDrugProgrammes[0]; // mimic ng repeat for this controller that injects the ace Dataset
                var personActive = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active

                scope.swapUserGrantedPermission(personActive, 32);

                expect(spyedSecurityServices.swapUserPermission.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController swapAllUsersPermissions', function () {

            it('THEN it should called swap with active user WHEN swapAllUsersPermissions is called', inject(function (Person) {

                scope.drugProgramme = mockDrugProgrammes[0]; // mimic ng repeat for this controller that injects the ace Dataset
                var person1 = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active
                var person2 = new Person('glena', 'id1', false, null, {jobStateCode: 0}); // set not active
                var personArray = [person1, person2];

                scope.swapAllUsersPermissions(personArray, 32, true);

                expect(spyedSecurityServices.swapAllUsersPermissions.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController addGroupPermission', function () {

            it('THEN it should call base methods addGroupPermission', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup = "Group1";

                scope.addGroupPermission();

                expect(scope.baseOnDrop.callCount).toBe(1);
                // expect(scope.baseInit.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController removePermission', function () {

            it('THEN it should call base methods removePermission', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup =
                        {
                            groupName: "Group1",
                            inLockdownDataset: false
                        };

                scope.removePermission();

                expect(scope.baseOnDrop.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController viewGrantedUsers', function () {

            var mockUser = [{user: 'glen'}];

            beforeEach(inject(function ($q, SecurityServices) {

                var usersDeferredSuccess = $q.defer();
                usersDeferredSuccess.resolve({
                    data: mockUser
                });

                spyOn(SecurityServices, 'getGrantedUsersForAcl').andReturn(usersDeferredSuccess.promise);
                spyOn(mockDialog, 'openModal');
            }));

            it('THEN it should call security services and open dialog', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.viewGrantedUsers();
                rootScope.$digest();

                expect(mockDialog.openModal.callCount).toBe(1);
                expect(spyedSecurityServices.getGrantedUsersForAcl.callCount).toBe(1);
                expect(scope.PARENT_MODEL.grantedUsers.allGrantedUsers).toEqual(mockUser);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController addGroupPermission', function () {

            it('THEN it should call base methods addGroupPermission', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup =
                        {
                            groupName: "Group1",
                            inLockdownDataset: false
                        };

                scope.addGroupPermission();

                expect(scope.baseOnDrop.callCount).toBe(1);
                // expect(scope.baseInit.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController removePermission', function () {

            it('THEN it should call base methods removePermission', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup =
                        {
                            groupName: "Group1",
                            inLockdownDataset: false
                        };

                scope.removePermission();

                expect(scope.baseOnDrop.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDrugProgrammeAclController viewGrantedUsers', function () {

            var mockUsers = [{user: 'glen'}];

            beforeEach(inject(function ($q, SecurityServices) {

                var usersDeferredSuccess = $q.defer();
                usersDeferredSuccess.resolve({
                    data: mockUsers
                });

                spyOn(SecurityServices, 'getGrantedUsersForAcl').andReturn(usersDeferredSuccess.promise);
                spyOn(mockDialog, 'openModal');
            }));

            it('THEN it should call security services and open dialog', inject(function (ParentModel, $q) {

                scope.drugProgramme = mockDrugProgrammes[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.viewGrantedUsers();
                rootScope.$digest();

                expect(mockDialog.openModal.callCount).toBe(1);
                expect(spyedSecurityServices.getGrantedUsersForAcl.callCount).toBe(1);
                expect(scope.PARENT_MODEL.grantedUsers.allGrantedUsers).toEqual(mockUsers);
            }));
        });

        //TODO
        //onDrop
        //getEditUserInfo
    });

    describe('MyAnalyticsVisualsationAclController', function () {

        var myAnalyticsController;

        var today = new Date();
        var todayPlus1Days = new Date(today);
        todayPlus1Days.setDate(today.getDate() + 1);
        todayPlus1Days.setHours(0, 0, 0, 0);

        var todayPlus2Days = new Date(today);
        todayPlus2Days.setDate(today.getDate() + 2);
        todayPlus2Days.setHours(0, 0, 0, 0);

        var todayMinus2Days = new Date(today);
        todayMinus2Days.setDate(today.getDate() - 2);
        todayMinus2Days.setHours(0, 0, 0, 0);

        var todayMinus1Days = new Date(today);
        todayMinus1Days.setDate(today.getDate() - 1);
        todayMinus1Days.setHours(0, 0, 0, 0);

        var noop = function () {
        };
        var mockEvent = {
            preventDefault: noop,
            stopPropagation: noop
        };



        beforeEach(function () {

            inject(function ($controller, _ChildModel_, _AcuityAclObjectEnum_, _Person_, $q) {

                myAnalyticsController = $controller('MyAnalyticsDatasetAclController', {
                    $scope: scope, ChildModel: _ChildModel_, SecurityServices: spyedSecurityServices,
                    AcuityAclObjectEnum: _AcuityAclObjectEnum_, Person: _Person_, ngDialog: mockNgDialog
                });
            });
        });

        describe('MyAnalyticsDatasetAclController init', function () {

            it('THEN the MODEL initialisations should be set in the controller', inject(function (ChildModel) {

                expect(scope.MODEL).toEqual(new ChildModel());

                expect(scope.MODEL.loading).toBe(false);
                expect(scope.MODEL.pageType).toBe('info');
                expect(scope.MODEL.aclInfo).toBe(null);
                expect(scope.MODEL.searchUsersTerm.name).toEqual("");
                expect(scope.MODEL.searchUsersTerm.showBy).toEqual("name");
                expect(scope.MODEL.permissions).toBeDefined();
            }));
        });

        describe('MyAnalyticsDatasetAclController loadDataset', function () {

            it('THEN it should set the aclInfo WHEN loadDataset is called', inject(function (ParentModel) {
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.vis = mockDatasets[0]; // mimic ng repeat for this controller that injects the ace Dataset

                expect(scope.MODEL.aclInfo).toBe(null);

                scope.loadDataset();

                expect(scope.MODEL.aclInfo).toBeDefined();
                expect(spyedSecurityServices.getDataset.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDatasetAclController swapUserGrantedPermission', function () {

            it('THEN it should called swap with active user WHEN swapUserGrantedPermission is called', inject(function ($rootScope, Person, $q) {
                scope.vis = mockDatasets[0]; // mimic ng repeat for this controller that injects the ace Dataset
                var personActive = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active

                var deferredSuccess = $q.defer();
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });
                scope.swapUserGrantedPermission(personActive, 32);

                $rootScope.$apply();
                expect(spyedSecurityServices.swapUserPermission.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDatasetAclController swapUserGrantedPermission', function () {

            it('THEN it shouldnt called swap with inactive user WHEN swapUserGrantedPermission is called', inject(function (Person, $rootScope, $q) {
                scope.vis = mockDatasets[0]; // mimic ng repeat for this controller that injects the ace Dataset
                var personActive = new Person('glena', 'id1', false, null, {jobStateCode: 0}); // set not active

                var deferredSuccess = $q.defer();
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });

                scope.swapUserGrantedPermission(personActive, 32);

                $rootScope.$apply();
                expect(spyedSecurityServices.swapUserPermission.callCount).toBe(0);
            }));
        });

        describe('MyAnalyticsDatasetAclController swapAllUsersPermissions', function () {

            it('THEN it should called swap with active user WHEN swapAllUsersPermissions is called', inject(function (Person, $rootScope, $q) {

                scope.vis = mockDatasets[0]; // mimic ng repeat for this controller that injects the ace Dataset
                var person1 = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active
                var person2 = new Person('glena', 'id1', false, null, {jobStateCode: 0}); // set not active
                var personArray = [person1, person2];

                var deferredSuccess = $q.defer();
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });

                scope.swapAllUsersPermissions(personArray, 32, true);

                $rootScope.$apply();
                expect(spyedSecurityServices.swapAllUsersPermissions.callCount).toBe(1);
            }));
        });


        describe('MyAnalyticsDatasetAclController isValid', function () {

            it('THEN it should be valid WHEN start is before end', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayPlus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus2Days;

                var isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(true);
            }));

            it('THEN it shouldnt be valid WHEN start is after end', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayPlus2Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus1Days;

                var isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(false);
            }));

            it('THEN it shouldnt be valid WHEN start and end is before now', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayMinus2Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayMinus1Days;

                var isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(false);
            }));

            it('THEN it shouldnt be valid WHEN start and end are null', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = null;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                var isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(false);

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayPlus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(false);

                scope.PARENT_MODEL.addScheduledUserPermission.start = null;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus1Days;

                isValid = scope.isStartEndDateValid();

                expect(isValid).toBe(false);
            }));
        });

        describe('MyAnalyticsDatasetAclController openEndCalendar', function () {

            it('THEN it should set correct end date  WHEN start is null and null', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = null;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.end).toEqual(todayPlus1Days);
            }));

            it('THEN it should set correct end date WHEN start is before today and end is null', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayMinus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.end).toEqual(todayPlus1Days);
            }));

            it('THEN it should set correct end date WHEN start is tomorrow and end is null', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayPlus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.end).toEqual(todayPlus2Days);
            }));

            it('THEN it should keep end WHEN start is tomorrow and end is set', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayMinus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus2Days;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.end).toEqual(todayPlus2Days);
            }));
        });

        describe('MyAnalyticsDatasetAclController openStartCalendar', function () {

            it('THEN it should set correct end date  WHEN start is null and null', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = null;
                scope.PARENT_MODEL.addScheduledUserPermission.end = null;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.end).toEqual(todayPlus1Days);
            }));

            it('THEN it should keep end WHEN start is set', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.addScheduledUserPermission.start = todayMinus1Days;
                scope.PARENT_MODEL.addScheduledUserPermission.end = todayPlus2Days;

                scope.openEndCalendar(mockEvent);

                expect(scope.PARENT_MODEL.addScheduledUserPermission.start).toEqual(todayMinus1Days);
            }));
        });

        describe('MyAnalyticsDatasetAclController addGroupPermission', function () {

            it('THEN it should call addScheduledGroupPermission when scheduled', inject(function (ParentModel, $q, $rootScope) {

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = true;

                var deferredSuccess = $q.defer();
                spyOn(scope, 'addScheduledGroupPermission').andReturn(deferredSuccess.promise);
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });

                scope.addGroupPermission();

                $rootScope.$apply();

                expect(scope.addScheduledGroupPermission.callCount).toBe(1);
            }));

            it('THEN it should call addScheduledGroupPermission when it isnt scheduled', inject(function (ParentModel, $q, $rootScope) {

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = false;

                var deferredSuccess = $q.defer();
                spyOn(scope, 'addUnscheduledGroupPermission').andReturn(deferredSuccess.promise);
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });

                scope.addGroupPermission();

                $rootScope.$apply();

                expect(scope.addUnscheduledGroupPermission.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDatasetAclController addUnscheduledGroupPermission', function () {

            it('THEN it should call base methods addUnscheduledGroupPermission', inject(function (ParentModel, $q) {

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup = "Group1";

                scope.addUnscheduledGroupPermission();

                expect(scope.baseOnDrop.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDatasetAclController addScheduledGroupPermission', function () {

            it('THEN it should call base methods addScheduledGroupPermission', inject(function (AcuityAclObjectEnum, ParentModel, $q, Person) {

                var person = Person.groupPerson("g1");
                var date = new Date();

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.PARENT_MODEL.groups.arrayTo = [];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup = "g1";
                scope.PARENT_MODEL.addScheduledUserPermission.start = date;
                scope.PARENT_MODEL.addScheduledUserPermission.end = date;

                scope.addScheduledGroupPermission();

                expect(spyedSecurityServices.addScheduledSid.callCount).toBe(1);
                expect(spyedSecurityServices.addScheduledSid).toHaveBeenCalledWith(AcuityAclObjectEnum.AcuityDataset, scope.vis.id, person, 32, date, date);
            }));
        });

        describe('MyAnalyticsDatasetAclController removePermission', function () {

            it('THEN it should call base methods removePermission', inject(function (ParentModel, $q, $rootScope) {

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.baseOnDrop = function () {
                };
                scope.baseInit = function () {
                };
                var deferredSuccess = $q.defer();
                spyOn(scope, 'baseOnDrop').andReturn(deferredSuccess.promise);
                spyOn(scope, 'baseInit').andReturn(deferredSuccess.promise);
                spyOn(scope, "checkLockDownConfirm").andCallFake(function () {
                    deferredSuccess.resolve('ok');
                    return deferredSuccess.promise;
                });

                scope.PARENT_MODEL.groups.arrayFrom = ["test"];
                scope.PARENT_MODEL.groups.permissionMask = 32;
                scope.PARENT_MODEL.groups.selectedGroup = "Group1";

                scope.removePermission();

                $rootScope.$apply();
                expect(scope.baseOnDrop.callCount).toBe(1);
            }));
        });

        describe('MyAnalyticsDatasetAclController viewGrantedUsers', function () {

            var mockUsers = [{user: 'glen'}];

            beforeEach(inject(function ($q, SecurityServices) {

                var usersDeferredSuccess = $q.defer();
                usersDeferredSuccess.resolve({
                    data: mockUsers
                });

                spyOn(SecurityServices, 'getGrantedUsersForAcl').andReturn(usersDeferredSuccess.promise);
                spyOn(mockDialog, 'openModal');
            }));

            it('THEN it should call security services and open dialog', inject(function (ParentModel, $q) {

                scope.vis = mockDatasets[0];
                scope.PARENT_MODEL = ParentModel; //mimic parent scope

                scope.viewGrantedUsers();
                rootScope.$digest();

                expect(mockDialog.openModal.callCount).toBe(1);
                expect(spyedSecurityServices.getGrantedUsersForAcl.callCount).toBe(1);
                expect(scope.PARENT_MODEL.grantedUsers.allGrantedUsers).toEqual(mockUsers);
            }));
        });

        describe('MyAnalyticsDatasetAclController isScheduledGroupsValid and isGroupDialogValid', function () {

            beforeEach(function () {

                scope.isGroupListValid = function () {
                };
            });

            it('THEN it should return true if scheduled and start end date ok for isScheduledGroupsValid', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = true;

                spyOn(scope, 'isStartEndDateValid').andReturn(true);
                spyOn(scope, 'isGroupListValid').andReturn(true);

                var isValid = scope.isScheduledGroupsValid();

                expect(isValid).toBe(true);
            }));

            it('THEN it should return true if scheduled and start end date isnt ok for isScheduledGroupsValid', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = true;

                spyOn(scope, 'isStartEndDateValid').andReturn(false);
                spyOn(scope, 'isGroupListValid').andReturn(true);

                var isValid = scope.isScheduledGroupsValid();

                expect(isValid).toBe(false);
            }));

            it('THEN it should return false if not scheduled and start end date isnt ok for isScheduledGroupsValid', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = false;

                spyOn(scope, 'isGroupListValid').andReturn(false);

                var isValid = scope.isScheduledGroupsValid();

                expect(isValid).toBe(false);
            }));

            it('THEN it should call isStartEndDateValid for scheduled for isGroupDialogValid', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = true;

                spyOn(scope, 'isStartEndDateValid').andReturn(true);

                var isValid = scope.isGroupDialogValid();

                expect(isValid).toBe(true);
            }));

            it('THEN it should call isGroupListValid for not scheduled for isGroupDialogValid', inject(function (ParentModel) {

                scope.PARENT_MODEL = ParentModel; //mimic parent scope
                scope.PARENT_MODEL.groups.isScheduled = false;

                spyOn(scope, 'isGroupListValid').andReturn(true);

                var isValid = scope.isGroupDialogValid();

                expect(isValid).toBe(true);
            }));
        });
    });

    describe('MyAnalyticsBaseAclController', function () {

        var myAnalyticsBaseController;
        var trainedUserGroup = "TRAINED_USER_GROUP";
        var developmentTeamGroup = "DEVELOPMENT_GROUP";
        var allGroups = [trainedUserGroup, developmentTeamGroup, "GROUP1"];

        var mockUsers = [
            {user: {userId: 'zksn', fullName: "ZGlen D"}},
            {user: {userId: 'ksn', fullName: "Glen D"}}
        ];

        beforeEach(inject(function ($controller, $q, $filter, _ParentModel_, _AcuityAclObjectEnum_, _SecurityServices_, _Person_, _MyAnalyticsUtils_) {

            myAnalyticsBaseController = $controller('MyAnalyticsController', {
                $scope: scope, ParentModel: _ParentModel_, SecurityServices: spyedSecurityServices, Person: _Person_
            });

            var groupsDeferredSuccess = $q.defer();
            groupsDeferredSuccess.resolve({
                data: allGroups
            });
            var userInGroupDeferredSuccess = $q.defer();
            userInGroupDeferredSuccess.resolve({
                data: mockUsers
            });
            var usersDeferredSuccess = $q.defer();
            usersDeferredSuccess.resolve({
                data: mockUsers
            });

            spyOn(_SecurityServices_, 'getGrantedUsersForAcl').andReturn(usersDeferredSuccess.promise);
            spyOn(_SecurityServices_, 'listAllGroups').andReturn(groupsDeferredSuccess.promise);
            spyOn(_SecurityServices_, 'getAllUserForGroup').andReturn(userInGroupDeferredSuccess.promise);
            spyOn(global, 'alasql');
        }));

        describe('MyAnalyticsBaseAclController baseExportGrantedUsers', function () {

            it('SHOULD sort the data and call alasql', inject(function (AcuityAclObjectEnum) {

                scope.baseExportGrantedUsers(AcuityAclObjectEnum.DrugProgramme, 1, 'DPName');
                rootScope.$digest();

                expect(spyedSecurityServices.getGrantedUsersForAcl.callCount).toBe(1);
                var expectedSortedResults = [{Name: 'Glen D', Prid: 'ksn'}, {Name: 'ZGlen D', Prid: 'zksn'}];

                expect(global.alasql).toHaveBeenCalledWith(jasmine.any(String), [expectedSortedResults]);
            }));
        });

        describe('MyAnalyticsBaseAclController baseOpenAddGroupDialog', function () {

            it('SHOULD set model correctly and remove dev and training groups', function () {

                var testScope = scope.$new();
                var arrayTo = [];
                var permissionMask = 32;

                scope.baseOpenAddGroupDialog(arrayTo, permissionMask, testScope);
                rootScope.$digest();

                expect(scope.PARENT_MODEL.groups.allGroups).toEqual(["GROUP1"]);
            });
        });

        describe('MyAnalyticsBaseAclController getAllGroups', function () {

            it('SHOULD sets model once got all groups', function () {

                scope.getAllGroups();
                rootScope.$digest();

                expect(scope.PARENT_MODEL.groups.allGroups).toEqual(allGroups);
                expect(scope.PARENT_MODEL.groups.selectedGroup).toEqual(allGroups[0]);
            });
        });

        describe('MyAnalyticsBaseAclController viewUsersInGroup', function () {

            it('SHOULD sets model once got all user in a groups', inject(function (Person) {

                var group = Person.groupPerson("g1");
                scope.viewUsersInGroup(group);
                rootScope.$digest();

                expect(scope.PARENT_MODEL.groups.usersForGroup).toEqual(mockUsers);
                expect(scope.PARENT_MODEL.groups.currentGroup).toEqual(group.userId);
            }));
        });
    });
});
