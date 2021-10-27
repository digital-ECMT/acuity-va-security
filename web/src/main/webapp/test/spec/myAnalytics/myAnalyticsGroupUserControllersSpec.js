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

describe('Controller: myAnalyticsGroupUserController', function () {

    'use strict';

    // load the controller's module
    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.securityServices'));
    beforeEach(module('acuityApp.myAnalyticsUserAccessControllers'));

    var groupUserAccessController, spyedSecurityModel, spyedSecurityServices, scope, rootScope, mockLog, $filter;

    // Mock data
    var mockInitGroups, testMockAllUsers;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($rootScope, $q, SecurityServices, SecurityModel) {

        scope = $rootScope.$new();
        rootScope = $rootScope;
        spyedSecurityModel = SecurityModel;
        spyedSecurityServices = SecurityServices;

        // Mock data
        mockInitGroups = [
            {
                groupName: "DEVELOPMENT_GROUP",
                inLockdownDataset: false
            },
            {
                groupName: "Group1",
                inLockdownDataset: false
            }
        ];

        testMockAllUsers =
                [
                    {userId: "User1", fullName: "user1 d", authorities: [], enabled: true},
                    {userId: "User2", fullName: "user2 d", authorities: [], enabled: true},
                    {userId: "User3", fullName: "user3 d", authorities: [], enabled: true},
                    {userId: "User4", fullName: "user4 d", authorities: [], enabled: true},
                    {userId: "User5", fullName: "user5 d", authorities: [], enabled: true},
                    {userId: "User6", fullName: "user6 d", authorities: [], enabled: true}
                ];

        //  Mock out calls        
        var listAllGroupsDeferredSuccess = $q.defer();
        listAllGroupsDeferredSuccess.resolve({
            data: mockInitGroups
        });

        var createGroupDeferredSuccess = $q.defer();
        createGroupDeferredSuccess.resolve();

        var removeUserFromGroupDeferredSuccess = $q.defer();
        removeUserFromGroupDeferredSuccess.resolve();

        var addUserToGroupDeferredSuccess = $q.defer();
        addUserToGroupDeferredSuccess.resolve();

        var deleteGroupDeferredSuccess = $q.defer();
        deleteGroupDeferredSuccess.resolve();

        var getAllEnabledUsersDeferredSuccess = $q.defer();
        getAllEnabledUsersDeferredSuccess.resolve({
            data: testMockAllUsers
        });

        var getAllUserForGroupDeferredSuccess = $q.defer();
        getAllUserForGroupDeferredSuccess.resolve({
            data: [testMockAllUsers[0]]
        });

        spyOn(spyedSecurityServices, 'removeUserFromGroup').andReturn(removeUserFromGroupDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'addUserToGroup').andReturn(addUserToGroupDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'deleteGroup').andReturn(deleteGroupDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'createGroup').andReturn(createGroupDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'listAllGroupsWithLockdown').andReturn(listAllGroupsDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'getAllEnabledUsers').andReturn(getAllEnabledUsersDeferredSuccess.promise);
        spyOn(spyedSecurityServices, 'getAllUserForGroup').andReturn(getAllUserForGroupDeferredSuccess.promise);

        mockLog = {
            error: function () {},
            log: function () {},
            debug: function () {}
        };
    }));

    describe('WHEN the controller is init', function () {

        var MODEL;

        beforeEach(inject(function ($controller, $filter, _Person_, _MyAnalyticsUtils_) {

            groupUserAccessController = $controller('GroupUserAccessController', {
                $scope: scope, $log: mockLog, $filter: $filter, SecurityModel: spyedSecurityModel, SecurityServices: spyedSecurityServices,
                MyAnalyticsUtils: _MyAnalyticsUtils_, Person: _Person_
            });

            MODEL = groupUserAccessController.MODEL;

            rootScope.$digest();
        }));

        it('SHOULD controller MODEL should be in correct state', function () {

            expect(spyedSecurityServices.listAllGroupsWithLockdown).toHaveBeenCalled();
            expect(spyedSecurityServices.getAllEnabledUsers).toHaveBeenCalled();
            expect(spyedSecurityServices.getAllUserForGroup).toHaveBeenCalled();

            expect(MODEL.groups.allGroups).toEqual(mockInitGroups);
            expect(MODEL.groups.selectedGroup).toEqual(mockInitGroups[0]);

            expect(MODEL.users.allTrainedUsers.length).toEqual(6);
            expect(MODEL.users.groupUsers.length).toEqual(1);
            expect(MODEL.users.trainedUsers.length).toEqual(5);
        });

        it('SHOULD current group should be deletable', function () {

            var deletable = groupUserAccessController.isCurrentGroupDeletable();

            expect(deletable).toBe(false);
        });

        it('SHOULD setting current group as not development team mean state is updated', function () {

            groupUserAccessController.selectGroupHandler(mockInitGroups[1]);

            expect(MODEL.groups.selectedGroup).toEqual(mockInitGroups[1]);
            expect(spyedSecurityServices.getAllUserForGroup).toHaveBeenCalledWith(mockInitGroups[1].groupName);
        });

        it('SHOULD adding group should call service and add to array list', function () {

            var newGroup = "test";
            var newGroupObject =
                    {
                        groupName: newGroup,
                        inLockdownDataset: false
                    };

            groupUserAccessController.addGroup(newGroup);

            rootScope.$digest();

            expect(MODEL.groups.allGroups).toContain(newGroupObject);
            expect(MODEL.groups.selectedGroup).toEqual(newGroupObject);
            expect(spyedSecurityServices.getAllUserForGroup).toHaveBeenCalledWith(newGroup);
            expect(spyedSecurityServices.createGroup).toHaveBeenCalledWith(newGroup);
        });

        it('SHOULDNT add same group twice', function () {

            var sameGroup = "Group1";
            var sameGroupObject =
                    {
                        groupName: sameGroup,
                        inLockdownDataset: false
                    };
                    
            groupUserAccessController.addGroup(sameGroup);

            rootScope.$digest();

            expect(MODEL.groups.allGroups).toContain(sameGroupObject);
            expect(MODEL.groups.selectedGroup).not.toEqual(sameGroupObject);
            expect(spyedSecurityServices.getAllUserForGroup).not.toHaveBeenCalledWith(sameGroup);
            expect(spyedSecurityServices.createGroup).not.toHaveBeenCalledWith(sameGroup);
        });

        it('SHOULDNT add empty group', function () {

            var emptyGroup = "";

            groupUserAccessController.addGroup(emptyGroup);

            rootScope.$digest();

            expect(MODEL.groups.allGroups).not.toContain(emptyGroup);
            expect(MODEL.groups.selectedGroup).not.toEqual(emptyGroup);
            expect(spyedSecurityServices.getAllUserForGroup).not.toHaveBeenCalledWith(emptyGroup);
            expect(spyedSecurityServices.createGroup).not.toHaveBeenCalledWith(emptyGroup);
        });

//        it('SHOULD delete current group should remove it from array', function () {
//
//            var currentGroup = mockInitGroups[1];
//
//            groupUserAccessController.selectGroupHandler(currentGroup); // set as Group1
//            rootScope.$digest();
//            groupUserAccessController.deleteCurrentGroup();
//            rootScope.$digest();
//
//            expect(MODEL.groups.selectedGroup).toEqual(mockInitGroups[0]);
//            expect(MODEL.groups.allGroups.length).toBe(1);
//            //expect(spyedSecurityServices.deleteGroup).toHaveBeenCalledWith(currentGroup);
//        });

        it('SHOULD set as isEmpty', function () {

            var currentGroup = mockInitGroups[1];

            groupUserAccessController.selectGroupHandler(currentGroup); // set as Group1
            rootScope.$digest();

            expect(MODEL.groups.isEmpty).toBe(false);
        });

        it('SHOULD set as isEmpty is false', function () {

            groupUserAccessController.selectGroupHandler('');
            rootScope.$digest();

            expect(MODEL.groups.isEmpty).toBe(true);
            expect(MODEL.groups.selectedGroup).toBe('');
        });

        it('SHOULD set as isEmpty is false again', function () {

            groupUserAccessController.selectGroupHandler(null);
            rootScope.$digest();

            expect(MODEL.groups.isEmpty).toBe(true);
            expect(MODEL.groups.selectedGroup).toBe('');
        });

        it('SHOULD remove user from selected group', function () {

            var currentGroup = mockInitGroups[1];

            groupUserAccessController.selectGroupHandler(currentGroup); // set as Group1
            rootScope.$digest();

            groupUserAccessController.onClickRemoveUserFromGroup(MODEL.users.groupUsers[0], MODEL.users.trainedUsers, MODEL.users.groupUsers);

            rootScope.$digest();

            expect(MODEL.users.groupUsers.length).toBe(0);
            expect(MODEL.users.trainedUsers.length).toBe(6);
            expect(spyedSecurityServices.removeUserFromGroup).toHaveBeenCalled();
        });

        it('SHOULD add user to selected group', function () {

            var currentGroup = mockInitGroups[1];

            groupUserAccessController.selectGroupHandler(currentGroup); // set as Group1
            rootScope.$digest();

            groupUserAccessController.onDropAddUserToSelectedGroup(MODEL.users.trainedUsers[1], MODEL.users.groupUsers, MODEL.users.trainedUsers);

            rootScope.$digest();

            expect(MODEL.users.groupUsers.length).toBe(2);
            expect(MODEL.users.trainedUsers.length).toBe(4);
            expect(spyedSecurityServices.addUserToGroup).toHaveBeenCalled();
        });
    });
});
