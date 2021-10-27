
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

describe('GIVEN analyticsFilter', function () {

    'use strict';

    beforeEach(module('acuityApp.constants'));
    beforeEach(module('acuityApp.securityServices'));
    beforeEach(module('acuityApp.myAnalyticsServices'));
    beforeEach(module('acuityApp.myAnalyticsFilters'));

    var testDatasetsData = [
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 130, name: "ACUITY_Safety_STDY4321_Dummy_Instance",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321", moduleType: "Safety"},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 131, name: "ACUITY_Tolerability_STDY4321_Dummy_Instance",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321", moduleType: "Tolerability"},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 132, name: "ACUITY_Operational_STDY4321_Dummy_Instance",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321", moduleType: "Operational"},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 133, name: "ACUITY_Oncology_STDY4321_Dummy_Instance",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY5321", moduleType: "Oncology"},
        {supertype: "com.acuity.va.security.acl.domain.Dataset", type: "com.acuity.va.security.acl.domain.AcuityDataset", id: 134, name: "ACUITY_Oncology_STDY4321_Dummy_Instance2",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY5321", moduleType: "Oncology"}
    ];

    var testStudyData = [
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 130, name: "Dummy_Instance1",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321"},
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 131, name: "Dummy_Instance2",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321"},
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 132, name: "Dummy_Instance3",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY4321"},
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 133, name: "Instance1",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY5321"},
        {supertype: "com.acuity.va.security.acl.domain.ClinicalStudy", type: "com.acuity.va.security.acl.domain.ClinicalStudy", id: 134, name: "Instance2",
            permissionMask: 3, isOpen: false, drugProgramme: "STDY5321"}
    ];

    describe('WHEN filtering by drug programme', function () {

        it('THEN it should return a set of all the drug programmes', inject(function (filterByDrugProgrammeFilter) {
            expect(filterByDrugProgrammeFilter(testDatasetsData)).toEqual(['STDY4321', 'STDY5321']);
        }));

        it('THEN it should return a set of all the module types', inject(function (filterByModuleTypeFilter) {
            expect(filterByModuleTypeFilter(testDatasetsData)).toEqual(['Safety', 'Tolerability', 'Operational', 'Oncology']);
        }));
    });
    describe('WHEN filtering by Persons', function () {
        var testPersonData;

        beforeEach(inject(function (Person) {
            testPersonData = [
                new Person('Glen', 'ksn223'),
                new Person('Glen D', 'dd3545'),
                new Person('Bob', 'Bob232342'),
                new Person('Gerry', 'kssddd'),
                new Person('Simon', 'ksdn12')
            ];
        }));

        it('can get an instance of my factory', inject(function (Person) {
            expect(new Person('Glen', 'ksn223').name).toEqual('Glen');
            expect(Person).toBeDefined();
        }));

        it('THEN it should return a filtered set of persons from lower case name', inject(function (filterBySearchTermFilter) {
            expect(filterBySearchTermFilter(testPersonData, {name: 'g', showBy: 'name'}).length).toBe(3);
        }));

        it('THEN it should return a filtered set of persons from upper case name', inject(function (filterBySearchTermFilter) {
            expect(filterBySearchTermFilter(testPersonData, {name: 'G', showBy: 'name'}).length).toBe(3);
        }));

        it('THEN it should return a filtered set of persons from upper and lower case ksn223', inject(function (filterBySearchTermFilter) {
            expect(filterBySearchTermFilter(testPersonData, {name: 'Ks', showBy: 'nameAndId'}).length).toBe(3);
        }));
    });
    describe('WHEN displaying person name', function () {
        var testPerson;
        var groupPerson;

        beforeEach(inject(function (Person) {
            testPerson = new Person('Glen', 'ksn223');
            groupPerson = Person.groupPerson('Group');
        }));

        it('THEN it should display name', inject(function (personDisplayNameFilter) {
            expect(personDisplayNameFilter(testPerson, 'name')).toBe('Glen');
            expect(personDisplayNameFilter(groupPerson, 'name')).toBe('GROUP: Group');
        }));

        it('THEN it should display id', inject(function (personDisplayNameFilter) {
            expect(personDisplayNameFilter(testPerson, 'userId')).toBe('ksn223');
            expect(personDisplayNameFilter(groupPerson, 'name')).toBe('GROUP: Group');
        }));

        it('THEN it should display nameAndId', inject(function (personDisplayNameFilter) {
            expect(personDisplayNameFilter(testPerson, 'nameAndId')).toBe('Glen (ksn223)');
            expect(personDisplayNameFilter(groupPerson, 'name')).toBe('GROUP: Group');
        }));

        it('THEN it should display empty for null', inject(function (personDisplayNameFilter) {
            expect(personDisplayNameFilter(null, 'nameAndId')).toBe('');
        }));

        it('THEN it should display empty for undefined', inject(function (personDisplayNameFilter) {
            expect(personDisplayNameFilter(undefined, 'nameAndId')).toBe('');
        }));
    });
    describe('WHEN filtering with a filterOr', function () {

        it('THEN it should filter on Safety OR Oncology', inject(function (filterOrFilter, AcuityAclObjectEnum) {
            var searchByValue = {name: 'Safety or Oncology'};

            expect(filterOrFilter(testDatasetsData, searchByValue).length).toBe(3);
        }));

        it('THEN it should filter on just Safety', inject(function (filterOrFilter, AcuityAclObjectEnum) {
            var searchByValue = {name: 'Safety'};

            expect(filterOrFilter(testDatasetsData, searchByValue).length).toBe(1);
        }));

        it('THEN it should filter on just Safety or Oncology OR Tolera', inject(function (filterOrFilter, AcuityAclObjectEnum) {
            var searchByValue = {name: 'Safety or Oncology OR  Tolera'};

            expect(filterOrFilter(testDatasetsData, searchByValue).length).toBe(4);
        }));

        it('THEN it should filter on just clinical study drug programme: STDY4321', inject(function (filterOrFilter, AcuityAclObjectEnum) {
            var searchByValue = {name: 'STDY4321'};

            expect(filterOrFilter(testStudyData, searchByValue).length).toBe(3);
        }));

        it('THEN it should filter on just clinical study drug programme: STDY4321', inject(function (filterOrFilter, AcuityAclObjectEnum) {
            var searchByValue = {name: 'STDY'};

            expect(filterOrFilter(testStudyData, searchByValue).length).toBe(5);
        }));
    });
    describe('WHEN filtering with a filterPermissionMask', function () {

        var testPermissions =
                [
                    {user: {userId: "user001", fullName: "Bob B", authorities: [], "enabled": true}, permissionMask: 7, viewPackagePermissions: []},
                    {user: {userId: "User5", fullName: "User5 Fullname", authorities: [], "enabled": true}, permissionMask: 3, viewPackagePermissions: []},
                    {user: {userId: "User3", fullName: "User3 Fullname", authorities: [], "enabled": true}, permissionMask: 442383, viewPackagePermissions: []},
                    {user: {userId: "Group", group: true, fullName: "Group Fullname", authorities: [], "enabled": true}, permissionMask: 32783, viewPackagePermissions: []}
                ];

        it('THEN it should filter on PermissionMask 3', inject(function (filterPermissionMaskFilter, Person, SecurityAuth) {
            var expectedPerson = new Person(testPermissions[0].user.fullName, testPermissions[0].user.userId, testPermissions[0].user.granted,
                    undefined, undefined, false, 7);
            expectedPerson.viewPackagePermissions = [3, 4];
            
            expect(filterPermissionMaskFilter(testPermissions, 3).length).toBe(2);
            expect(filterPermissionMaskFilter(testPermissions, 3)[0]).toEqual(expectedPerson);
            expect(filterPermissionMaskFilter(testPermissions, 3)[1].viewPackagePermissions).toEqual([3]);
        }));

        it('THEN it should filter on PermissionMask 442383', inject(function (filterPermissionMaskFilter, Person) {
            var expectedPerson = new Person(testPermissions[2].user.fullName, testPermissions[2].user.userId, testPermissions[2].user.granted,
                    undefined, undefined, false, 442383);
            expectedPerson.viewPackagePermissions = [3, 4, 8];
            
            expect(filterPermissionMaskFilter(testPermissions, 442383).length).toBe(1);
            expect(filterPermissionMaskFilter(testPermissions, 442383)[0]).toEqual(expectedPerson);
        }));

        it('THEN it should filter on PermissionMask 32', inject(function (filterPermissionMaskFilter) {
            expect(filterPermissionMaskFilter(testPermissions, 32).length).toBe(0);
        }));

        it('THEN it should filter on PermissionMask 32783', inject(function (filterPermissionMaskFilter, Person) {
            var expectedPerson = new Person(testPermissions[3].user.fullName, testPermissions[3].user.userId, testPermissions[3].user.granted,
                    undefined, undefined, true, 32783);
            expectedPerson.viewPackagePermissions = [3, 4, 8];
            
            expect(filterPermissionMaskFilter(testPermissions, 32783).length).toBe(1);
            expect(filterPermissionMaskFilter(testPermissions, 32783)[0]).toEqual(expectedPerson);
        }));
    });

    describe('WHEN filtering with a dataUploadStatus', function () {

        it('THEN it should replace RED with Fail', inject(function (dataUploadStatusFilter) {
            expect(dataUploadStatusFilter('RED')).toEqual('Fail');
        }));

        it('THEN it should replace AMBER with Warning', inject(function (dataUploadStatusFilter) {
            expect(dataUploadStatusFilter('AMBER')).toEqual('Warning');
        }));

        it('THEN it should replace GREEN with Success', inject(function (dataUploadStatusFilter) {
            expect(dataUploadStatusFilter('GREEN')).toEqual('Success');
        }));

        it('THEN it should replace Random with Unknown', inject(function (dataUploadStatusFilter) {
            expect(dataUploadStatusFilter('Random')).toEqual('Unknown');
        }));
    });
    describe('WHEN filtering if present', function () {
        var testPersonData;
        var otherPersonData;

        beforeEach(inject(function (Person) {
            testPersonData = [
                new Person('Glen', 'ksn223'),
                new Person('Glen D', 'dd3545'),
                new Person('Bob', 'Bob232342'),
                new Person('Gerry', 'kssddd'),
                new Person('Simon', 'ksdn12')
            ];
        }));

        beforeEach(inject(function (Person) {
            otherPersonData = [
                new Person('Bob', 'Bob232342'),
                new Person('Gerry', 'kssddd')
            ];
        }));

        it('THEN it should return a filtered set of persons not contained in otherPersonData', inject(function (filterIfPresentFilter) {
            expect(filterIfPresentFilter(testPersonData, otherPersonData).length).toBe(3);
        }));

        it('THEN it should return an unfiltered set of persons from empty argument', inject(function (filterIfPresentFilter) {
            expect(filterIfPresentFilter(testPersonData, {}).length).toBe(5);
        }));
    });
});
