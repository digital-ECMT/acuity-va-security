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

//Goes through the ACUITY portal from the Analitycs: Drug Program till all links.
//open homepage
describe('Drug programmes page tests', function () {
    beforeEach(function () {
        portal.setTestName('DrugProgrammes');
    });

    afterEach(function () {
        portal.logResult();
    });

//Click 'Drug programmes' button and make sure that you moved to the Drug programmes
    describe('WHEN my analytics page is loaded and Drug programmes button clicked', function () {
        it('THEN user is redirected to Drug programmes page', function () {
            portal.getMainPage();
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/myAnalytics');
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/drug-programmes');
        })
    });

//Make sure that number of Drug programmes is displayed
    describe('WHEN Drug programmes page is loaded', function () {
        var number;

        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
        });

        it('THEN number of Drug programmes should be displayed and be greater than 0', function () {
            element(by.id('programmesCounter')).getText().then(function (txt) {
                number = parseInt(txt);
                expect(number).toBeGreaterThan(0);
            });
        });

        it('THEN number of Drug programmes should be equal to number of displayed icons', function () {
            //Compares previously retrieved number to number of drugs in repeater
            element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).then(function (Drugs) {
                expect(Drugs.length).toEqual(number);
            });
        });

        it('THEN number of Drug programmes should be equal to number of lines in list view', function () {
            //Compares previously retrieved number to number of Drugs in repeater
//            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
//            portal.pause();
            element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).then(function (Drugs) {
                expect(Drugs.length).toEqual(number);
            });
        });

    });

    //Make sure that "boxes view"  switch works between list and grid
    //excluded from test run since icon view is disabled for Programmes projection
    xdescribe('WHEN "list view" switch button is pressed', function () {

        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
        });


        xit('THEN view is changed from list to boxes and Drug programmes names are displayed', function () {
            element(by.xpath("//*[contains(@class,'glyphicon-th-large')]")).click();
            portal.pause();
            var dProgram = element(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes').row(0)).getText();
            expect(dProgram).toContain('Drug');

            //redirect back to list view
            element(by.css('span.glyphicon.glyphicon-list')).click();
            portal.pause();
        })
    });



//we click on the 1234 drug programm
    describe('WHEN STDY4321 the panel is opened', function () {
        var selectedProgram, openedPanel, setupBtn, show1Btn, show2Btn, editBtn, vis, oncol1234;
        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
//            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
//            portal.pause();
            element(by.partialLinkText('D1234')).then(function (link) {
                link.click();
                portal.pause();
            });
        });


        //click Show button and verify that user is redirected to studies page and search field is filled
        //todo: PhantomJS Specific failure, cant be reproduced in any other browser excluded until completely investigated
        xit('THEN click Show button ', function () {
            openedPanel = element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).get(7);
            takeScreenShot(testNumber+'debug1');

            openedPanel.all(by.partialLinkText('Show')).get(0).click(); //<-todo fails here
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/studies');
            expect(element(by.id('searchInput')).getAttribute('value')).toContain('Dummy');
            element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function (studies) {
                expect(studies.length).toBeGreaterThan('3');
                element(by.id('clearButton')).click();
            })

        });

        //click Show button and verify that user is redirected to visualization page and search field is filled
        //todo: PhantomJS Specific failure, cant be reproduced in any other browser excluded until completely investigated
        xit('THEN click Show button and THEN user is redirected to visualization page and search field is filled', function () {
            openedPanel = element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).get(7);
            takeScreenShot(testNumber+'debug1');
            openedPanel.all(by.partialLinkText('Show')).get(1).click(); // todo: fails here
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/datasets');
            expect(element(by.id('searchInput')).getAttribute('value')).toContain('STDY4321');
            takeScreenShot(testNumber);
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function (vis) {
                expect(vis.length).toBeGreaterThan('3');
            });
            element(by.id('clearButton')).click();

        });
//click Edit button and verify that panel appears
        it('THEN click Edit button', function () {
            openedPanel = element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).get(7); //STDY4321 Panel
            openedPanel.all(by.className('glyphicon-user')).get(0).click();
            portal.pause();
            portal.pause();
            portal.pause();
            var expectedUser = 'Shadrin';
            element(by.id('searchUsersInput')).sendKeys(expectedUser);
            portal.pause();
            expect(element.all(by.repeater('MODEL.permissions.trainedUsers | filterBySearchTerm:MODEL.searchUsersTerm')).count()).toBe(1);
            portal.pause();

        })
    });


//Make sure that Search works correctly
    describe('WHEN Drug programmes is entered to Search field', function () {

        beforeEach(function () {
            portal.getMainPage();
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
            portal.pause();
            //Commented - list view is now the default.
//            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
//            portal.pause();
        });


        it('THEN the list of Drug programmes is reduced', function () {
            var expectedDrugProg = 'STDY4321';
            element(by.model('PARENT_MODEL.searchTerm.name')).sendKeys(expectedDrugProg);
            expect(element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes | filterOr:PARENT_MODEL.searchTerm')).count()).toBe(1);
            element(by.id('clearButton')).click();
        });

        it('THEN after pressing Clear button Search field is clear again', function () {
            element(by.model('PARENT_MODEL.searchTerm.name')).sendKeys('there is no such program');
            portal.pause();
            element(by.id('clearButton')).click();
            portal.pause();
            expect(element(by.model('PARENT_MODEL.searchTerm.name')).getText()).toBe('');
        })
    });

    describe('WHEN I expand Drug Programme panel', function(){
        //Opening Programmes projection
        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
            portal.pause();
        });

        it('Setup button leads to drug programme setup',function(){
            element.all(by.repeater('drugProgramme in PARENT_MODEL.allAcls.drugProgrammes')).then(function(allElements){
                //This will be used to verify, that there is no duplicates.
                var previousHtmls = [];

                //We will check 10% of elements randomly
                var randomIndexesToBeChecked = [];
                for (n=0; n<Math.floor(allElements.length/10); n++){
                    var candidate = Math.floor((Math.random() * allElements.length) + 1);
                    if(randomIndexesToBeChecked.indexOf(candidate)==-1){
                        randomIndexesToBeChecked[randomIndexesToBeChecked.length] = candidate;
                    }
                }
                randomIndexesToBeChecked.sort(function(a, b){return a-b});
                console.log('Random indexes: '+ randomIndexesToBeChecked);

                for(i=0; i<randomIndexesToBeChecked.length; i++){
                    var elementName = allElements[randomIndexesToBeChecked[i]].getText();
                    element(by.linkText(elementName)).click();
                    portal.pause();
                    //Panel got expanded

                    //Checking the setup link
                    var outerHtml = element(by.partialLinkText('Setup')).getOuterHtml();
                    //expect(outerHtml).toContain(portal.ptor.baseUrl); //disabled until complete integration
                    expect(outerHtml).toContain('/programme-setup?projectId=');

                    //Veryfiing that there is no duplicate links
                    expect(previousHtmls.indexOf(outerHtml)).toEqual(-1);
                    previousHtmls[previousHtmls.length] = outerHtml;

                    //panel collapsed
                    element(by.linkText(elementName)).click();
                    portal.pause();
                }
            });
            //10% of programmes got randomly checked
        });
    });

});
