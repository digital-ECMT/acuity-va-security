
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

describe('GIVEN myAnalyticsServices', function () {

    'use strict';

    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.myAnalyticsServices'));

    describe('Running Person factory tests', function () {

        describe('WHEN creating person objects', function () {
            var testPerson;
            var linkedTestPerson;
            var scheduledActiveTestPerson;
            var scheduledInactiveTestPerson;
            var emptyPerson;
            var groupPerson;
            var personPerson;

            beforeEach(inject(function (Person) {
                testPerson = new Person('Glen', 'ksn223');
                var jobActiveDetails = {jobStateCode: 1}; //scheduled and added
                var jobInactiveDetails = {jobStateCode: 0}; //scheduled but not added
                linkedTestPerson = new Person('Linked', 'ksn111', true, testPerson);
                scheduledInactiveTestPerson = new Person('Scheduled', 'ksn111', true, null, jobInactiveDetails);
                scheduledActiveTestPerson = new Person('Scheduled', 'ksn111', true, null, jobActiveDetails);
                emptyPerson = Person.emptyPerson();
                groupPerson = Person.groupPerson("Group1");
                personPerson = Person.person("glen1");
            }));

            it('THEN it should display name', function () {
                expect(testPerson.name).toBe('Glen');
            });

            it('THEN test person should display name', function () {
                expect(testPerson.group).toBe(false);
            });

            it('THEN test person should display userId', function () {
                expect(testPerson.userId).toBe('ksn223');
            });

            it('THEN test person should display nameAndId', function () {
                expect(testPerson.nameAndId).toBe('Glen (ksn223)');
            });

            it('THEN test person should not be empty nameAndId', function () {
                expect(testPerson.isEmpty()).toBe(false);
            });

            it('THEN test person should not be linked', function () {
                expect(testPerson.isLinked()).toBe(false);
            });

            it('THEN test person toAcuitySidDetails', inject(function (Person) {
                expect(testPerson.toAcuitySidDetails()).toEqual({
                    sidAsString: testPerson.userId,
                    group: testPerson.group
                });
                expect(Person.toAcuitySidDetails(testPerson)).toEqual({
                    sidAsString: testPerson.userId,
                    group: testPerson.group
                });
            }));

            it('THEN empty person should be empty', function () {
                expect(emptyPerson.name).toBe('Empty');
                expect(emptyPerson.group).toBe(false);
                expect(emptyPerson.isEmpty()).toBe(true);
            });

            it('THEN linked person should be linked', function () {
                expect(linkedTestPerson.isLinked()).toBe(true);
                expect(linkedTestPerson.linkedPerson).toBe(testPerson);
                expect(linkedTestPerson.granted).toBe(true);
            });

            it('THEN linked person should not be linked when setting null or undefined', function () {
                linkedTestPerson.linkedPerson = null;
                expect(linkedTestPerson.isLinked()).toBe(false);

                linkedTestPerson.linkedPerson = undefined;
                expect(linkedTestPerson.isLinked()).toBe(false);
            });

            it('THEN should be isScheduledJob', function () {
                expect(scheduledInactiveTestPerson.isScheduledJob()).toBe(true);
                expect(scheduledActiveTestPerson.isScheduledJob()).toBe(true);
                expect(testPerson.isScheduledJob()).toBe(false);
            });

            it('THEN hould be jobStateCode', function () {
                expect(scheduledInactiveTestPerson.jobStateCode()).toBe(0);
                expect(scheduledActiveTestPerson.jobStateCode()).toBe(1);
                expect(testPerson.jobStateCode()).toBe(-1);
            });

            it('THEN should be isActivePermission', inject(function (Person) {
                expect(scheduledInactiveTestPerson.isActivePermission()).toBe(false);
                expect(scheduledActiveTestPerson.isActivePermission()).toBe(true);
                expect(testPerson.isActivePermission()).toBe(true);
                expect(Person.isActivePermission(testPerson)).toBe(true);
                expect(Person.isActivePermission(scheduledActiveTestPerson)).toBe(true);
                expect(Person.isActivePermission(scheduledInactiveTestPerson)).toBe(false);
                expect(Person.isActivePermission({})).toBe(true);
            }));

            it('THEN group person should be created', inject(function (Person) {
                expect(groupPerson.group).toBe(true);
                expect(groupPerson.userId).toBe("Group1");
                expect(groupPerson.name).toBe("Group1");
                expect(groupPerson.isActivePermission()).toBe(true);
                expect(groupPerson.isScheduledJob()).toBe(false);
                expect(groupPerson.isLinked()).toBe(false);
                expect(groupPerson.toAcuitySidDetails()).toEqual({
                    sidAsString: groupPerson.userId,
                    group: groupPerson.group
                });
                expect(Person.toAcuitySidDetails(groupPerson)).toEqual({
                    sidAsString: groupPerson.userId,
                    group: groupPerson.group
                });
            }));

            it('THEN person person should be created', inject(function (Person) {
                expect(personPerson.group).toBe(false);
                expect(personPerson.userId).toBe("glen1");
                expect(personPerson.name).toBe("glen1");
                expect(personPerson.isActivePermission()).toBe(true);
                expect(personPerson.isScheduledJob()).toBe(false);
                expect(personPerson.isLinked()).toBe(false);
                expect(personPerson.toAcuitySidDetails()).toEqual({
                    sidAsString: personPerson.userId,
                    group: personPerson.group
                });
                expect(Person.toAcuitySidDetails(personPerson)).toEqual({
                    sidAsString: personPerson.userId,
                    group: personPerson.group
                });
            }));
            it('THEN person person should be created', function () {
                expect(personPerson.group).toBe(false);
                expect(personPerson.userId).toBe("glen1");
                expect(personPerson.name).toBe("glen1");
                expect(personPerson.isActivePermission()).toBe(true);
                expect(personPerson.isScheduledJob()).toBe(false);
                expect(personPerson.isLinked()).toBe(false);
            });
        });
    });

    describe('Running MyAnalyticsUtils tests', function () {

        describe('WHEN using checkIfArrayEmpty method', function () {

            var testPersonArray;
            var testPersonEmptyArray = [];
            var testEmptyPerson;
            var MyAnalyticsUtils;

            beforeEach(inject(function (Person, _MyAnalyticsUtils_) {
                testPersonArray = [
                    new Person('Glen', 'ksn223'),
                    new Person('Glen D', 'dd3545'),
                    new Person('Bob', 'Bob232342'),
                    new Person('Gerry', 'kssddd'),
                    new Person('Simon', 'ksdn12')
                ];

                testEmptyPerson = Person.emptyPerson();
                MyAnalyticsUtils = _MyAnalyticsUtils_;
            }));

            it('THEN if not an empty array, should not add am empty person', function () {
                expect(testPersonArray.length).toBe(5);

                MyAnalyticsUtils.checkIfArrayEmpty(testPersonArray);

                expect(testPersonArray.length).toBe(5);
            });
            it('THEN if an empty array, should add an empty person', function () {
                expect(testPersonEmptyArray.length).toBe(0);

                MyAnalyticsUtils.checkIfArrayEmpty(testPersonEmptyArray);

                expect(testPersonEmptyArray.length).toBe(1);
                expect(testPersonEmptyArray[0].isEmpty()).toBe(true); // one added should be empty person
            });
            it('THEN if array contains empty and none empty persons, should remove the empty person', function () {
                expect(testPersonArray.length).toBe(5);
                testPersonArray.push(testEmptyPerson);
                expect(testPersonArray.length).toBe(6);

                MyAnalyticsUtils.checkIfArrayEmpty(testPersonArray);

                expect(testPersonArray.length).toBe(5);
                expect(_.pluck(testPersonArray, 'name')).not.toContain('Empty');
            });
        });
        describe('WHEN using removeFromUsers method', function () {

            var testAllUserPersonArray;
            var testRemoveFromPersonArray;
            var testEmptyPerson;
            var MyAnalyticsUtils;

            beforeEach(inject(function (Person, _MyAnalyticsUtils_) {
                testAllUserPersonArray = [
                    new Person('Glen', 'ksn223'),
                    new Person('Glen D', 'dd3545'),
                    new Person('Bob', 'Bob232342'),
                    new Person('Gerry', 'kssddd'),
                    new Person('Simon', 'ksdn12')
                ];
                testRemoveFromPersonArray = [
                    new Person('Glen', 'ksn223'),
                    new Person('Glen', 'dd3545')
                ];

                testEmptyPerson = Person.emptyPerson();
                MyAnalyticsUtils = _MyAnalyticsUtils_;
            }));

            it('THEN it removes all users from array', function () {
                expect(testAllUserPersonArray.length).toBe(5);

                MyAnalyticsUtils.removeFromUsers(testRemoveFromPersonArray, testAllUserPersonArray);

                expect(testAllUserPersonArray.length).toBe(3);
                expect(_.pluck(testAllUserPersonArray, 'name')).toEqual(['Bob', 'Gerry', 'Simon']);
                expect(_.pluck(testAllUserPersonArray, 'name')).not.toContain('Glen');
            });

            it('THEN shouldnt remove anything with empty array', function () {
                expect(testAllUserPersonArray.length).toBe(5);

                MyAnalyticsUtils.removeFromUsers([], testAllUserPersonArray);

                expect(testAllUserPersonArray.length).toBe(5);
            });

            it('THEN shouldnt remove empty from array', function () {
                expect(testAllUserPersonArray.length).toBe(5);

                MyAnalyticsUtils.removeFromUsers([testEmptyPerson], testAllUserPersonArray);

                expect(testAllUserPersonArray.length).toBe(5);
            });
        });
    });
    describe('Running MyAnalyticsDNDUtils tests', function () {

        describe('WHEN using getMyDroppableLocations method', function () {

            it('THEN if only allow droppable locations of "administratorChannel, authorisedUserChannel, usersChannel" ' +
                'for person with permission DRUG_PROGRAMME_DATA_OWNER = 450575 on item authoriser', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 450575};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('authoriserChannel', mockAcuityObject);

                    console.log("location " + myLocations);
                    expect(myLocations.length).toBe(3);
                    expect(myLocations[0]).toEqual("administratorChannel");
                }));

            it('THEN dont allow any droppable locations of "authoriser, administrator, authorisedUser, users" ' +
                'for person with permission DRUG_PROGRAMME_DATA_OWNER = 450575 on item dataOwner', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 450575};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('dataOwnerChannel', mockAcuityObject);

                    expect(myLocations.length).toBe(0);
                }));

            it('THEN if only allow all droppable locations for person with permission DEVELOPMENT_TEAM = 522240 ' +
                'on item users', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 522240};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('usersChannel', mockAcuityObject);

                    console.log("location " + myLocations);
                    expect(myLocations.length).toBe(4);
                    expect(myLocations[0]).toEqual("dataOwnerChannel");
                }));

            it('THEN if only allow droppable locations of "administrator, users" for person with ' +
                'permission AUTHORISERS = 442383 on item authorisedUser', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 442383};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('authorisedUserChannel', mockAcuityObject);

                    console.log("location " + myLocations);
                    expect(myLocations.length).toBe(2);
                    expect(myLocations[0]).toEqual("administratorChannel");
                }));

            it('THEN if only allow no droppable locations for person with ' +
                'permission ADMINISTRATOR = 32783 on item user', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 32783};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('usersChannel', mockAcuityObject);

                    console.log("location " + myLocations);
                    expect(myLocations.length).toBe(1);
                    expect(myLocations[0]).toEqual("authorisedUserChannel");
                }));

            it('THEN if only allow no droppable locations for person with permission AUTHORISED_USER = 3' +
                ' on item users', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 3};

                    var myLocations = MyAnalyticsDNDUtils.getMyDroppableLocations('usersChannel', mockAcuityObject);

                    console.log("location " + myLocations);
                    expect(myLocations.length).toBe(0);
                }));
        });

        describe('WHEN using isDraggable method', function () {

            var mockPerson;
            var mockEmptyPerson;

            beforeEach(inject(function (Person) {

                mockEmptyPerson = Person.emptyPerson();
                mockPerson = new Person("d", "b");
            }));

            it('THEN isDraggable true for person with permission DRUG_PROGRAMME_DATA_OWNER = 450575 ' +
                'on item authoriser with none empty list', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 450575};

                    var draggable = MyAnalyticsDNDUtils.isDraggable(mockPerson, 'authoriserChannel', mockAcuityObject);

                    expect(draggable).toBe(true);
                }));

            it('THEN isDraggable false for person with permission ADMINISTRATOR = 32783 ' +
                'on item authorisedUser with none empty list', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 32783};

                    var draggable = MyAnalyticsDNDUtils.isDraggable(mockPerson, 'authorisedUserChannel', mockAcuityObject);

                    expect(draggable).toBe(true);
                }));

            it('THEN isDraggable false for person with permission DRUG_PROGRAMME_DATA_OWNER = 450575 ' +
                'on item dataOwner with none empty list', inject(function (MyAnalyticsDNDUtils) {

                    var mockAcuityObject = {rolePermissionMask: 450575};

                    var draggable = MyAnalyticsDNDUtils.isDraggable(mockPerson, 'dataOwnerChannel', mockAcuityObject);

                    expect(draggable).toBe(false);
                }));

            it('THEN isDraggable false for empty person', inject(function (MyAnalyticsDNDUtils) {

                var mockAcuityObject = {rolePermissionMask: 229152};

                var draggable = MyAnalyticsDNDUtils.isDraggable(mockEmptyPerson, 'dataOwnerChannel', mockAcuityObject);

                expect(draggable).toBe(false);
            }));
        });
    });

    // TODO
    //populateAclPermissions
});
