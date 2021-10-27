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

var portal = require('../portal.js');

//Tests for check searching
describe('Tests for check searching', function () {

//Logging into console
    afterEach(function () {
        portal.logResult();
    });

    beforeEach(function () {
        portal.setTestName('Search');
        portal.getMainPage();
        element(by.partialLinkText('Analytics')).click();
        portal.pause();
        element(by.partialLinkText('Drug programmes')).click();
        portal.pause();
    });

//Page is loaded and we click drug programmes button
//We expect browser to stay on the same page
    describe('WHEN my analytics page is loaded and drug programmes button clicked', function () {
        it('THEN user is redirected to drug programmes page', function () {
            expect(portal.getCurrentUrl()).toContain('/drug-programmes');
        })
    });

    // Input 1234 in Search my analytics field and verify that list of drug programmes contains only STDY4321
    describe('WHEN D1234 entered to Search field', function () {
        it('THEN the list of drug programmes contains only 1 STDY4321', function () {
            var expectedDrugProg = 'D1234';
            element(by.id('searchInput')).sendKeys(expectedDrugProg);
            portal.pause();
            expect(element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).count()).toEqual(1);
        });

        it('THEN after pressing Clear button Search field is clear again', function () {
            element(by.id('searchInput')).sendKeys('Test Search');
            expect(element(by.id('searchInput')).getAttribute('value')).toBe('Test Search');
            element(by.id('clearButton')).click();
            expect(element(by.id('searchInput')).getAttribute('value')).toBe('');
        })

    });

// Input STDY1 in Search my analytics field and verify that list of drug programmes contains 2 row
    describe('WHEN STDY is entered to Search field', function () {
        it('THEN the list of drug programmes contains multiple rows', function () {
            var expectedDrugProg = 'STDY';
            element(by.id('searchInput')).sendKeys(expectedDrugProg);
            expect(element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes | filterOr:PARENT_MODEL.searchTerm')).count()).toBeGreaterThan(1);
        });
    });

    // Input '1234 or 2014' in Search my analytics field and verify that list of drug programmes contains 2 row
    describe('WHEN "D1234 or 2014" is entered to Search field', function () {
        it('THEN the list of drug programmes contains only 2 row', function () {
            var expectedDrugProg = 'D1234 or 2014';
            element(by.id('searchInput')).sendKeys(expectedDrugProg);
            expect(element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes | filterOr:PARENT_MODEL.searchTerm')).count()).toEqual(2);
        });
    });

// Input Dummy in Search my analytics field and verify that list of drug programmes contains only STDY4321
    describe('WHEN Dummy is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
        });

        it('THEN the list of Clinical studies contains only 1 Dummy study', function () {
            var expectedClinSt = 'Dummy';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            portal.pause();
            expect(element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).count()).toEqual(1);
        });
    });


// Input test in Search my analytics field and verify that list of drug programmes contains 'Greater Than 0 row'
    describe('WHEN test is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Clinical study datasets')).click();
        });

        it('THEN the list of Clinical studies contains greater than 1 row', function () {
            var expectedClinSt = 'test';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            expect(element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies | filterOr:PARENT_MODEL.searchTerm')).count()).toBeGreaterThan(1);
        });
    });

// Input 'ab or test' in Search my analytics field and verify that list of drug programmes contains 'Greater Than 0 row'
    xdescribe('WHEN ab or test is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Dataset modules')).click();
            portal.pause();
        });

        it('THEN the list of Clinical studies contains contains greater than 3 row', function () {  //todo 1
            var expectedClinSt = 'ab or test';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            expect(element.all(by.repeater('study in PARENT_MODEL.allAcls.datasets')).count()).toBeGreaterThan(3);
        });
    });

//Click 'Dataset modules' button
    describe('WHEN  Datasets modules button is clicked', function () {
        beforeEach(function () {
            element(by.partialLinkText('Dataset modules')).click();
            portal.pause();
        });

        it('THEN user is redirected to datasets page', function () {
            var visualButton = element(by.css('a.list-group-item:nth-child(3)'));
            visualButton.click();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/datasets');
        })
    });

// Input Dummy in Search my analytics field and verify that list of drug programmes contains only STDY4321
    describe('WHEN Dummy is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Dataset modules')).click();
        });

        it('THEN the list of Datasets modules contains only 5 datasets', function () {
            var expectedClinSt = 'Dummy';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            expect(element.all(by.repeater('vis in filteredDatasetsDrug')).count()).toBeGreaterThan(0);
            expect(element.all(by.repeater('vis in filteredDatasetsDrug')).count()).not.toBeGreaterThan(6);
        });
    });


// Input safety in Search my analytics field and verify that list of drug programmes contains 4 row
    describe('WHEN safety is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Dataset modules')).click();
        });

        it('THEN the list of Datasets modules contains greater than 10 row', function () {
            var expectedClinSt = 'safety';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            expect(element.all(by.repeater('vis in filteredDatasetsDrug')).count()).toBeGreaterThan(10);
            expect(element.all(by.repeater('drug in PARENT_MODEL.allAcls.datasets')).count()).toBeGreaterThan(10);
        });
    });

// Input 'safety or 1234' in Search my analytics field and verify that list of drug programmes contains 'Greater Than 4 row'
    xdescribe('WHEN ab or test is entered to Search field', function () {
        beforeEach(function () {
            element(by.partialLinkText('Dataset modules')).click();
        });

        it('THEN the list of Datasets modules contains greater than 4 row', function () { //todo 2
            var expectedClinSt = 'safety or 1234';
            element(by.id('searchInput')).sendKeys(expectedClinSt);
            expect(element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets | filterOr:PARENT_MODEL.searchTerm')).count()).toBeGreaterThan(4);
        });
    });
});
