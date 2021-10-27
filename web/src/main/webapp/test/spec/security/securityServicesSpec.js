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

describe('Service: securityService', function () {

    'use strict';

    var $httpBackend, service;

    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.securityServices'));
    beforeEach(module('underscore'));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('who am i SecurityModel', function () {

        var userResponse =
            {
                userId: "user001", fullName: "Glen d", authorities: [
                    {authority: "ROLE_USER_SINGLE"},
                    {authority: "ROLE_USER"},
                    {authority: "ROLE_ADMIN"}
                ], enabled: true, name: "user001", principal: "user001", details: null
            };

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('GET', '/resources/my/user/whoami').respond(userResponse);

                service = injector.get('SecurityModel');
            });
        });

        it('should get current user from rest service', function () {
            var userId;
            var fullName;

            service.initWhoami().then(function (response) {
                userId = response.data.userId;
                fullName = response.data.fullName;
            });
            $httpBackend.flush();

            console.log("Current user " + userId);
            expect(userId).toBe(userResponse.userId);
            expect(fullName).toBe(userResponse.fullName);
            expect(service.getUserId()).toEqual(userResponse.userId);
            expect(service.getFullName()).toEqual(userResponse.fullName);
        });
    });

    describe('list acls SecurityModel', function () {

        var aclResponse = [
            {type: "com.acuity.va.security.acl.domain.DrugProgramme", supertype: "com.acuity.va.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.va.security.acl.domain.DrugProgramme", supertype: "com.acuity.va.security.acl.domain.DrugProgramme", id: 2, name: "Drug A", rolePermissionMask: 96},
            {type: "com.acuity.va.security.acl.domain.AcuityDataset", supertype: "com.acuity.va.security.acl.domain.Dataset", id: 3, name: "Dataset Z", rolePermissionMask: 3},
            {type: "com.acuity.va.security.acl.domain.AcuityDataset", supertype: "com.acuity.va.security.acl.domain.Dataset", id: 4, name: "Dataset F", rolePermissionMask: 3},
            {type: "com.acuity.va.security.acl.domain.AcuityDataset", supertype: "com.acuity.va.security.acl.domain.Dataset", id: 5, name: "SDataset A", rolePermissionMask: 3},
            {type: "com.acuity.va.security.acl.domain.ClinicalStudy", supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 6, name: "Clinical Study A", rolePermissionMask: 96},
            {type: "com.acuity.va.security.acl.domain.ClinicalStudy", supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3},
            {type: "com.acuity.va.security.acl.domain.ClinicalStudy", supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 8, name: "Clinical Study B", rolePermissionMask: 96}
        ];

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.expectGET('/resources/my/aclswithoutauthorisedusers').respond(aclResponse);

                service = injector.get('SecurityModel');
            });
        });

        it('should get list of acls for current user from rest service', function () {
            var acls;

            service.initAcls().then(function (response) {
                acls = response.data;
            });
            $httpBackend.flush();

            console.log("Acls for user " + angular.toJson(acls));
            expect(acls).toEqual(aclResponse);

            _.each(acls, function (acl) {
                console.log(acl.id);
            });

            expect(service.getAcls()).toEqual(aclResponse);
        });

        it('should list only drug programmes from full list', function () {
            // invoke getting all acls
            service.initAcls();
            $httpBackend.flush();

            // list drug programmes
            var drugProgrammes = service.listDrugProgrammes();

            expect(drugProgrammes[0].name).toEqual("Drug A");
            expect(drugProgrammes[1].name).toEqual("Drug B");
        });

        it('should list only clinical studies from full list', function () {
            // invoke getting all acls
            service.initAcls();
            $httpBackend.flush();

            var clinicalStudies = service.listClinicalStudies();

            expect(clinicalStudies[0].name).toEqual("Clinical Study A");
            expect(clinicalStudies[1].name).toEqual("Clinical Study B");
            expect(clinicalStudies[2].name).toEqual("Clinical Study C");
        });

        it('should list only datasets from full list', function () {
            // invoke getting all acls

            service.initAcls();
            $httpBackend.flush();

            var datasets = service.listDatasets();

            expect(datasets[0].name).toEqual("Dataset F");
            expect(datasets[1].name).toEqual("Dataset Z");
            expect(datasets[2].name).toEqual("SDataset A");
        });
    });

    // TODO test all the services methods
    describe('get DrugProgramme/ClinicalStudy/Dataset SecurityServices', function () {

        var response = {
            id: 0,
            addedDate: 1401277294564,
            compound: "compound",
            dataOwner: "user001",
            datasetModules: [
                {moduleType: "Safety", count: 10}
            ],
            numberOfStudies: 0,
            numberOfDosedSubjects: 0,
            userInfo: {administrators: 0, users: 0, authorisors: 0, dataOwners: 4}
        };

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                $httpBackend = $injector.get('$httpBackend');

                service = injector.get('SecurityServices');
            });
        });

        it('should get drug programme from rest service', inject(function (AcuityAclObjectEnum) {
            var data;
            $httpBackend.expectGET('/resources/myanalytics/DrugProgramme/DrugProgramme/1').respond(response);
            service.getDrugProgramme(AcuityAclObjectEnum.DrugProgramme, 1).then(function (response) {
                data = response.data;
            });
            $httpBackend.flush();

            console.log("Current drug programme info " + angular.toJson(data));
            expect(data).toEqual(response);
        }));

        it('should get clinical study from rest service', inject(function (AcuityAclObjectEnum) {
            var data;
            $httpBackend.expectGET('/resources/myanalytics/ClinicalStudy/ClinicalStudy/10').respond(response);
            service.getClinicalStudy(AcuityAclObjectEnum.ClinicalStudy, 10).then(function (response) {
                data = response.data;
            });
            $httpBackend.flush();

            console.log("Current clinical study info " + angular.toJson(data));
            expect(data).toEqual(response);
        }));

        it('should get dataset from rest service', inject(function (AcuityAclObjectEnum) {
            var data;
            $httpBackend.expectGET('/resources/myanalytics/Dataset/AcuityDataset/100').respond(response);
            service.getDataset(AcuityAclObjectEnum.AcuityDataset, 100).then(function (response) {
                data = response.data;
            });
            $httpBackend.flush();

            console.log("Current dataset info " + angular.toJson(data));
            expect(data).toEqual(response);
        }));
    });

    describe('get Other SecurityServices methods', function () {

        var response = {
            id: 0,
            addedDate: 1401277294564,
            addedBy: "user001",
            compound: "compound",
            dataOwner: "user001",
            datasetModules: [
                {moduleType: "Safety", count: 10}
            ],
            numberOfStudies: 0,
            numberOfDosedSubjects: 0,
            userInfo: {administrators: 0, users: 0, authorisors: 0, dataOwners: 4}
        };

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                $httpBackend = $injector.get('$httpBackend');
                // $httpBackend.when('POST', '/resources/acl/DrugProgramme/1/ace').respond("");

                service = injector.get('SecurityServices');
            });
        });

        it('should swap all user permission from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var personActive = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active
            var personNotActive = new Person('glenna', 'id2', false, null, {jobStateCode: 0}); // set not active
            var personArray = [personActive, personNotActive];
            var postData = [
                {
                    acuitySidDetails: Person.toAcuitySidDetails(personActive),
                    permissionMask: 23,
                    granting: true
                }
            ];
            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/aces?check=false', postData).respond(201, {});

            service.swapAllUsersPermissions(AcuityAclObjectEnum.DrugProgramme, 1, personArray, 23, true, true).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));
        
        it('should batch input all user permission from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var personActive = new Person('glena', 'id1', false, null, {jobStateCode: 1}); // set active
            var personNotActive = new Person('glenna', 'id2', false, null, {jobStateCode: 0}); // set not active
            var personArray = [personActive, personNotActive];
            var postData = [
                {
                    acuitySidDetails: Person.toAcuitySidDetails(personActive),
                    permissionMask: 23,
                    granting: true
                }
            ];
            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/aces?check=true', postData).respond(201, {});

            service.swapAllUsersPermissions(AcuityAclObjectEnum.DrugProgramme, 1, personArray, 23, true, false).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('should add user permission from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var person = new Person('glen', 'id', true, null, {jobStateCode: 1}); // set active
            var postData = {
                acuitySidDetails: Person.toAcuitySidDetails(person),
                permissionMask: 23,
                granting: true
            };
            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/ace', postData).respond(201, {});

            service.addUserPermission(AcuityAclObjectEnum.DrugProgramme, 1, person, 23, postData).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('shouldnt add user schduled permission from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var person = new Person('glen', 'id', true, null, {jobStateCode: 0}); // set not active
            var postData = {
                acuitySidDetails: Person.toAcuitySidDetails(person),
                permissionMask: 23,
                granting: true
            };
            try {
                service.addUserPermission(AcuityAclObjectEnum.DrugProgramme, 1, person, 23, postData);
            } catch (err) {
                expect(err).toContain("Cant swap person who isnt active");
            }
        }));

        it('should swap user permission from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var person = new Person('glen', 'id', true, null, {jobStateCode: 1}); // set active            
            var postData = {
                acuitySidDetails: Person.toAcuitySidDetails(person),
                permissionMask: 23,
                granting: false
            };
            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/ace', postData).respond(201, {});

            service.swapUserPermission(AcuityAclObjectEnum.DrugProgramme, 1, person, 23, false).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('shouldnt swap schduled user permission from rest service', inject(function (AcuityAclObjectEnum, Person) {

            var person = new Person('glen', 'id', true, null, {jobStateCode: 0}); // not active

            try {
                service.swapUserPermission(AcuityAclObjectEnum.DrugProgramme, 1, person, 23, false);
            } catch (err) {
                expect(err).toContain("Cant swap person who isnt active");
            }
        }));

        it('should remove all users permissions from rest service', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var person = new Person('glen', 'id', true, null, {jobStateCode: 1}); // active

            var postData = {
                acuitySidDetails: Person.toAcuitySidDetails(person),
            };

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/deleteace', postData).respond(201, {});

            service.removeAllUserPermissions(AcuityAclObjectEnum.DrugProgramme, 1, person).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('should get all users scheduled permissions from rest service', inject(function (AcuityAclObjectEnum) {
            var status;

            $httpBackend.expectGET('/resources/acl/DrugProgramme/1/acewithscheduled').respond(201, {});

            service.getAllWithScheduledPermissions(AcuityAclObjectEnum.DrugProgramme, 1).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('should replace data owner from rest service', inject(function (AcuityAclObjectEnum) {
            var status;
            var userId = "glen";

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/ace/dataOwner', userId).respond(201, {});

            service.replaceDataOwner(AcuityAclObjectEnum.DrugProgramme, 1, userId).then(function (response) {
                status = response.status;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
        }));

        it('should get all enabled users from rest service', function () {
            var status;
            var data;

            var trainedUserGroup = "TRAINED_USER_GROUP";
            var response = {};

            $httpBackend.expectGET('/resources/groups/' + trainedUserGroup).respond(201, response);

            service.getAllEnabledUsers().then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should get all permissions from rest service', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var response = {};

            $httpBackend.expectGET('/resources/acl/DrugProgramme/1/ace').respond(201, response);

            service.getAllPermissions(AcuityAclObjectEnum.DrugProgramme, 1).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        }));

        it('should get all development team users from rest service', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var developmentTeamGroup = "DEVELOPMENT_GROUP";
            var response = {};

            $httpBackend.expectGET('/resources/groups/' + developmentTeamGroup).respond(201, response);

            service.getAllDevelopmentTeamUsers(AcuityAclObjectEnum.DrugProgramme, 1).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        }));

        it('should get all none trained users from rest service', function () {
            var status;
            var data;

            var trainedUserGroup = "TRAINED_USER_GROUP";
            var response = {};

            $httpBackend.expectGET('/resources/groups/not/' + trainedUserGroup).respond(201, response);

            service.getAllNoneTrainedUsers().then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should add user to group from rest service', function () {
            var status;
            var data;

            var group = "group";
            var userId = "userId";
            var response = {};

            $httpBackend.expectPUT('/resources/users/' + userId + '/groups', group).respond(201, response);

            service.addUserToGroup(userId, group).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should add user to trained user group from rest service', function () {
            var status;
            var data;

            var trainedUserGroup = "TRAINED_USER_GROUP";
            var userId = "userId";
            var response = {};

            $httpBackend.expectPUT('/resources/users/' + userId + '/groups', trainedUserGroup).respond(201, response);

            service.addUserToTrainedUserGroup(userId).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should remove user to group from rest service', function () {
            var status;
            var data;

            var group = "group";
            var userId = "userId";
            var response = {};

            $httpBackend.expectDELETE('/resources/users/' + userId + '/groups/' + group).respond(201, response);

            service.removeUserFromGroup(userId, group).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should create user and add user to trained user group from rest service', function () {
            var status;
            var data;

            var group = "TRAINED_USER_GROUP";
            var userId = "userId";
            var userName = "user";
            var response = {};

            $httpBackend.expectPUT('/resources/users/' + userId + '/groups/' + group + '/fullName/' + userName).respond(201, response);

            service.addNewUserToTrainedUserGroup(userId, userName).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should add existing user to trained user group from rest service', function () {
            var status;
            var data;

            var group = "TRAINED_USER_GROUP";
            var userId = "userId";
            var response = {};

            $httpBackend.expectPUT('/resources/users/' + userId + '/groups').respond(201, response);

            service.addExistingUserToTrainedUserGroup(userId).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(201);
            expect(data).toEqual(response);
        });

        it('should linkUser users together', function () {
            var status;
            var data;

            var userIdFrom = "userIdFrom";
            var userIdTo = "userIdTo";
            var response = {};

            $httpBackend.expectPOST('/resources/user/' + userIdFrom + '/link/' + userIdTo).respond(204, response);

            service.linkUser(userIdFrom, userIdTo).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });

        it('should unlinkUser a user', function () {
            var status;
            var data;

            var userIdFrom = "userIdFrom";
            var response = {};

            $httpBackend.expectPOST('/resources/user/' + userIdFrom + '/unlink').respond(204, response);

            service.unLinkUser(userIdFrom).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        it('should getAllUserForGroup', function () {
            var status;
            var data;

            var group = "group1";
            var response = {};

            $httpBackend.expectGET('/resources/groups/' + group).respond(204, response);

            service.getAllUserForGroup(group).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        it('should getAllMatchingUsers for surname more than 3 letters', function () {
            var status;
            var data;

            var surname = "1234";
            var response = {};

            $httpBackend.expectGET('/resources/user/search/' + surname + "?wildcard=END").respond(204, response);

            service.getAllMatchingUsers(surname).promise.then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        it('should getAllMatchingUsers for surname less than 3 letters', function () {
            var status;
            var data;

            var surname = "123";
            var response = {};

            $httpBackend.expectGET('/resources/user/search/' + surname + "?wildcard=NONE").respond(204, response);

            service.getAllMatchingUsers(surname).promise.then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        it('should addScheduledUser ', inject(function (AcuityAclObjectEnum, Person) {
            var status;
            var data;

            var date = new Date();
            var person = Person.person("glen");
            var postData = {
                acuitySidDetails: Person.toAcuitySidDetails(person),
                permissionMask: 32,
                granting: true,
                start: date,
                end: date
            };
            var response = {};

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/schedule/ace', postData).respond(204, response);

            service.addScheduledSid(AcuityAclObjectEnum.DrugProgramme, 1, person, 32, date, date).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));
        
        it('should listAllGroups', function () {
            var status;
            var data;
           
            var response = {};

            $httpBackend.expectGET('/resources/groups').respond(204, response);

            service.listAllGroups().then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        
        it('should create group', function () {
            var status;
            var data;

            var group = "group";
            var response = {};

            $httpBackend.expectPOST('/resources/groups/' + group).respond(204, response);

            service.createGroup(group).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        
        it('should delete group', function () {
            var status;
            var data;

            var group = "group";
            var response = {};

            $httpBackend.expectDELETE('/resources/groups/' + group).respond(204, response);

            service.deleteGroup(group).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        });
        
        it('should get acls user permissions', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var group = "group";
            var response = {};

            $httpBackend.expectGET('/resources/acl/DrugProgramme/1/useraces').respond(204, response);

            service.getGrantedUsersForAcl(AcuityAclObjectEnum.DrugProgramme, 1).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));
        
        it('should set lockdown', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var response = {};

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/setlockdown').respond(204, response);

            service.setLockdown(AcuityAclObjectEnum.DrugProgramme, 1, true).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));
        
        it('should unset lockdown', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var response = {};

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/unsetlockdown').respond(204, response);

            service.setLockdown(AcuityAclObjectEnum.DrugProgramme, 1, false).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));
        
        it('should set Inherit Permission', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var response = {};

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/setInheritPermission').respond(204, response);

            service.setInheritPermission(AcuityAclObjectEnum.DrugProgramme, 1, true).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));
        
        it('should unset Inherit Permission', inject(function (AcuityAclObjectEnum) {
            var status;
            var data;

            var response = {};

            $httpBackend.expectPOST('/resources/acl/DrugProgramme/1/unsetInheritPermission').respond(204, response);

            service.setInheritPermission(AcuityAclObjectEnum.DrugProgramme, 1, false).then(function (response) {
                status = response.status;
                data = response.data;
            });
            $httpBackend.flush();

            expect(status).toEqual(204);
        }));

    });

    describe('hasPermission SecurityAuth', function () {

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                service = injector.get('SecurityAuth');
            });
        });

        var ace = {
            type: "com.acuity.visualisations.security.acl.domain.DrugProgramme",
            id: 29,
            name: "STUDY0005",
            rolePermissionMask: 2051,
            isOpen: true,
            drugProgramme: "STDY4321",
            moduleType: null
        };        

        it('should give permission for EDIT_TRAINED_USERS = 2048', function () {

            var hasPermission = service.hasPermission('EDIT_TRAINED_USERS', ace);

            expect(hasPermission).toBe(true);
        });

        it('should give permission for 3', function () {

            var hasPermission = service.hasPermission('3', ace);

            expect(hasPermission).toBe(true);
        });

        it('should give permission for VIEW_VISUALISATIONS = 3', function () {

            var hasPermission = service.hasPermission('VIEW_VISUALISATIONS', ace);

            expect(hasPermission).toBe(true);
        });

        it('should NOT give permission for 16', function () {

            var hasPermission = service.hasPermission('16', ace);

            expect(hasPermission).toBe(false);
        });

        it('should NOT give permission for EDIT_DATA_OWNERS = 4096', function () {

            var hasPermission = service.hasPermission('EDIT_DATA_OWNERS', ace);

            expect(hasPermission).toBe(false);
        });

        it('should NOT give permission for 128 (EDIT_DATA_OWNERS)', function () {

            var hasPermission = service.hasPermission('4096', ace);

            expect(hasPermission).toBe(false);
        });

        it('should NOT give permission unknown permission', function () {

            var hasPermission = service.hasPermission('ASDD_EDdfdIT_ROLES', ace);

            expect(hasPermission).toBe(false);
        });
    });

    describe('hasAnyPermissionAdministratorOrAbove SecurityAuth', function () {

        var aceResponseAsAdmin = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 32783},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aceResponseNoneAdmin = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aceResponseNoneDrugOwner = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 522240}
        ];

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                service = injector.get('SecurityAuth');
            });
        });

        it('should not give permission for lower than admninistrator', function () {

            var hasPermission = service.hasAnyPermissionAdministratorOrAbove(aceResponseNoneAdmin);

            expect(hasPermission).toBe(false);
        });

        it('should give permission for admninistrator', function () {

            var hasPermission = service.hasAnyPermissionAdministratorOrAbove(aceResponseAsAdmin);

            expect(hasPermission).toBe(true);
        });

        it('should give permission for data owner', function () {

            var hasPermission = service.hasAnyPermissionAdministratorOrAbove(aceResponseNoneDrugOwner);

            expect(hasPermission).toBe(true);
        });

        it('should not give permission for empty list', function () {

            var hasPermission = service.hasAnyPermissionAdministratorOrAbove([]);

            expect(hasPermission).toBe(false);
        });
    });

    describe('hasAnyPermissionDeveloper SecurityAuth', function () {

        var aceResponseAsDataOwner = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 450575},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aceResponseNoneAdmin = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aceResponseDeveloper = [
            {type: "com.acuity.visualisations.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.visualisations.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 522240}
        ];

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;
                service = injector.get('SecurityAuth');
            });
        });

        it('should not give permission for data owner', function () {

            var hasPermission = service.hasAnyPermissionDeveloper(aceResponseAsDataOwner);

            expect(hasPermission).toBe(false);
        });

        it('should not give permission for none admin', function () {

            var hasPermission = service.hasAnyPermissionDeveloper(aceResponseNoneAdmin);

            expect(hasPermission).toBe(false);
        });

        it('should give permission for developer', function () {

            var hasPermission = service.hasAnyPermissionDeveloper(aceResponseDeveloper);

            expect(hasPermission).toBe(true);
        });

        it('should not give permission for empty list', function () {

            var hasPermission = service.hasAnyPermissionDeveloper([]);

            expect(hasPermission).toBe(false);
        });
    });

    describe('linkUsersDialog', function () {
        var linkUsersDialog;
        var scope;
        var mockNgDialog = {
            open: function () {
            },
            close: function (param) {
            }
        };

        beforeEach(function () {

            module(function ($provide) {
                $provide.factory('ngDialog', function () {
                    return mockNgDialog;
                });
            });
            inject(function ($rootScope, _linkUsersDialog_, $q) {
                linkUsersDialog = _linkUsersDialog_;
                scope = $rootScope.$new();
                spyOn(mockNgDialog, 'open').andCallFake(function () {
                    var deferred = $q.defer();
                    deferred.resolve('Remote call result');
                    return deferred.promise;
                });
                spyOn(mockNgDialog, 'close').andCallFake(function () {
                    var deferred = $q.defer();
                    deferred.resolve('Remote call result');
                    return deferred.promise;
                });
            });
        });

        it('should call show link Users Dialog dialog', function () {
            linkUsersDialog.openModal(scope);

            expect(mockNgDialog.open).toHaveBeenCalled();
        });
    });
});


