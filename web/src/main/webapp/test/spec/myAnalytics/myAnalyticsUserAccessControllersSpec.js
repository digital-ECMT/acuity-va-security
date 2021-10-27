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

describe('Controller: myAnalyticsUserAccessControllers', function () {

    'use strict';

    // load the controller's module
    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.constants'));
    beforeEach(module('acuityApp.utilServices'));
    beforeEach(module('acuityApp.securityServices'));

    var spyedSecurityModel, spyedSecurityServices, scope;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($rootScope, $q, SecurityModel, SecurityServices) {

        scope = $rootScope.$new();
        spyedSecurityModel = SecurityModel;
        spyedSecurityServices = SecurityServices;

    }));

    describe('UserAccessController ...', function () {

        describe('isCurrentUser', function () {
            var userAccessController;

            beforeEach(inject(function ($filter, $controller) {

                userAccessController = $controller('UserAccessController', {
                    $scope: scope, SecurityModel: spyedSecurityModel
                });
            }));

            it('THEN should be true for current user ', inject(function (Person) {
                spyOn(spyedSecurityModel, 'getUserId').andReturn("user001");

                var user001 = new Person('Glen', 'user001');

                expect(scope.isCurrentUser(user001)).toBe(true);
            }));

            it('THEN should be false not the for current user ', inject(function (Person) {
                spyOn(spyedSecurityModel, 'getUserId').andReturn("user001");

                var random = new Person('Glen', 'random');

                expect(scope.isCurrentUser(random)).toBe(false);
            }));
        });
    });

    describe('NoneTrainedUsersController ...', function () {

        describe('WHEN initailiing NoneTrainedUsersController', function () {
            var noneTrainedUsersController;

            beforeEach(inject(function ($q, $filter, $controller, MyAnalyticsUtils, Person) {

                var personArray = [{ userId: "knsfi", fullName: "Glen" }];
                var response = {data: personArray};

                spyOn(spyedSecurityServices, "getAllNoneTrainedUsers").andCallFake(function () {
                    var deferred = $q.defer();
                    deferred.resolve(response);
                    return deferred.promise;
                });
                spyOn(spyedSecurityServices, "addUserToTrainedUserGroup").andCallFake(function () {
                    var deferred = $q.defer();
                    deferred.resolve("201 Ok");
                    return deferred.promise;
                });

                noneTrainedUsersController = $controller('NoneTrainedUsersController', {
                    $scope: scope, SecurityServices: spyedSecurityServices, Person: Person, MyAnalyticsUtils: MyAnalyticsUtils
                });
            }));

            it('THEN getAllNoneTrainedUsers should have been called once and everything setup in controller', inject(function (Person) {

                //scope.getNotTrainedUserAccessInfo(); already called in init()
                scope.$apply();

                expect(spyedSecurityServices.getAllNoneTrainedUsers.callCount).toBe(1);
                expect(scope.MODEL.filteredCounts.notTrainedUsers).toBe(1);
                expect(scope.MODEL.users.notTrainedUsers.length).toBe(1);
            }));

            it('THEN calling addAsTrainedUser should call the services and add emtpy to', inject(function (Person) {

                scope.addAsTrainedUser(new Person("Glen", "knsfi"));
                scope.$apply();

                expect(spyedSecurityServices.addUserToTrainedUserGroup.callCount).toBe(1);
                expect(scope.MODEL.filteredCounts.notTrainedUsers).toBe(1);
                expect(scope.MODEL.users.notTrainedUsers.length).toBe(1);
                expect(scope.MODEL.users.notTrainedUsers[0].name).toBe("Empty");
            }));
        });
    });
});
