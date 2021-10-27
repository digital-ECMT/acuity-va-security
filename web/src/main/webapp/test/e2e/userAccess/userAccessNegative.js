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

var fs = require('fs');
var testNumber = 1;
var ptor;

var takeScreenShot = function (number) {
    browser.takeScreenshot().then(function (png) {
        var stream = fs.createWriteStream("./screenshots/userAccessNegative" + number + ".png");
        stream.write(new Buffer(png, 'base64'));
        stream.end();
    });
};

var paws = function () {
    ptor.waitForAngular();
    ptor.sleep(2000);
};

var waitForElement = function (by) { //waits for footer to appear
    browser.wait(function () {
        return ptor.isElementVisible(by);
    }, 30000);
};

var waitPageToLoad = function () { //waits for footer to appear
    var expectedElement = by.partialLinkText('ACUITY');
    browser.wait(function () {
        return ptor.isElementPresent(expectedElement);
    }, 30000);
};


describe('Dataset modules page tests', function () {
    browser.driver.manage().window().setSize(1600, 800);
    ptor = protractor.getInstance();
    ptor.manage().deleteAllCookies();
    ptor.sleep(5000);
    ptor.ignoreSynchronization = true;
    //First page load requires delay to load everything
    var targetUrl;
    //SwappedUser=Jenkins
    if(ptor.baseUrl.indexOf('swappedUser')>-1){
        targetUrl=ptor.baseUrl + '&swappedRoles=ACL_ADMINISTRATOR,TRAINED_USER'
    } else {
        targetUrl=ptor.baseUrl + '?swappedUser=UnknownUser&swappedRoles=ACL_ADMINISTRATOR,TRAINED_USER'
    }


    ptor.get(targetUrl);
    waitPageToLoad();
    ptor.sleep(10000);
    ptor.findElement(by.partialLinkText('Analytics')).click();
    paws();

    beforeEach(function () {
    });

    afterEach(function () {
        takeScreenShot(testNumber);
        var passed = jasmine.getEnv().currentSpec.results().passed();
        if (passed) {
            console.log("UserAccessNegativeTest TEST " + testNumber + " PASSED");
        }
        else {
            console.log("UserAccessNegativeTest TEST " + testNumber + " FAILED <---");
        }
        testNumber++;
    });

    describe('WHEN unknown user logs to system and views dataset modules', function () {
        beforeEach(function () {
            if(ptor.getCurrentUrl().toString().indexOf('datasets')<0){
                element(by.partialLinkText('Dataset modules')).click();
                paws();
            }
        });


        it('THEN number of dataset modules is 0', function () {
            ptor.findElement(protractor.By.partialLinkText('Dataset modules')).getText().then(function (txt) {
                var numDatasets = parseInt(txt.substr(22, txt.length));//retrieving number of modules, displayed
                expect(numDatasets).toEqual(0);
            });
        });

        it('THEN number of displayed dataset modules icons is 0', function () {
            //Compares previously retrieved number to number of studies in repeater
            ptor.findElements(by.repeater('vis in filteredDatasetsDrug')).then(function (vis) {
                expect(vis.length).toEqual(0);
            });
        });

        it('THEN if icons are sorted by drug programme, number of sections in icon view is equal to 0', function () {
            ptor.findElement(protractor.By.id('gridDrugInput')).click();
            ptor.findElements(by.repeater('drug in PARENT_MODEL.allAcls.datasets')).then(function (drugs) {
                expect(drugs.length).toEqual(0);
            });
        });

        it('THEN if icons are sorted by modules type, number sections is 0', function () {
            ptor.findElement(protractor.By.id('gridTypeInput')).click();
            ptor.findElements(by.repeater('moduleType in PARENT_MODEL.allAcls.datasets')).then(function (moduleTypes) {
                expect(moduleTypes.length).toEqual(0);
            });
        });

        it('THEN number of lines in list view is 0', function () {
            //Compares previously retrieved number to number of studies in repeater
            ptor.findElement(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
            paws();
            ptor.findElements(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function (vis) {
                expect(vis.length).toEqual(0);
            });
        });
    });

    describe('WHEN Unknown user observes drug programmes', function () {

        beforeEach(function () {
            if(ptor.getCurrentUrl().toString().indexOf('drug-programmes')<0){
                element(by.partialLinkText('Drug programmes')).click();
                paws();
            }
        });

        var number;

        it('THEN displayed number of Drug programmes should be  0', function () {
            ptor.findElement(protractor.By.id('programmesCounter')).getText().then(function (txt) {
                number = parseInt(txt);
                expect(number).toEqual(0);
            });
        });

        it('THEN number of Drug programmes in list view is 0', function () {
            ptor.findElements(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).then(function (Drugs) {
                expect(Drugs.length).toEqual(0);
            });
        });
    });

    describe('WHEN Unknown user observes clinical studies', function () {

        beforeEach(function () {
            if(ptor.getCurrentUrl().toString().indexOf('studies')<0){
                element(by.partialLinkText('Clinical study datasets')).click();
                paws();
            }
        });

        var numStudies;

        it('THEN number of Clinical study datasets should be displayed and be 0', function () {
            //checks displayed number of studies in 'Clinical study datasets' button
            ptor.findElement(protractor.By.partialLinkText('Clinical study datasets')).getText().then(function (txt) {
                numStudies = parseInt(txt.substr(24, txt.length));//retrieving number of clinical study datasets, displayed
                expect(numStudies).toEqual(0);
            });
        });

        it('THEN number of lines in list view should be 0', function () {
            ptor.findElements(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function (studies) {
                expect(studies.length).toEqual(0);
            });
        });
    });
    ptor.manage().deleteAllCookies();
});
