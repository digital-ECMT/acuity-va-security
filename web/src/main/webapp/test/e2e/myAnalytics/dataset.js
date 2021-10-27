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

//Goes through the ACUITY portal from the Analitycs: Dataset modules till all links.
//open homepage
describe('Dataset modules page tests', function () {
    beforeEach(function () {
        portal.setTestName('Visualizations');
        portal.getMainPage();
        element(by.partialLinkText('Analytics')).click();
        portal.pause();
    });

    afterEach(function () {
        portal.logResult();
    });

    //Click 'Dataset modules' button and make sure that you moved to the
    //datasets' page
    describe('WHEN Datasets button clicked', function () {
        //Init
        beforeEach(function () {
            element(by.partialLinkText('Drug programmes')).click();
            portal.pause();
        });

        it('THEN user is redirected to datasets page', function () {
            element(by.partialLinkText('Dataset modules')).click();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/datasets');
        });
    });

    describe('WHEN datasets page is loaded', function () {
        var numDatasets = 0;
        it('THEN number of dataset modules should be displayed', function () {
            element(by.partialLinkText('Dataset modules')).getText().then(function (txt) {
                numDatasets = parseInt(txt.substr(22, txt.length));//retrieving number of modules, displayed
                expect(numDatasets).toBeGreaterThan(0);
            });
        });

        it('THEN number of displayed dataset modules should be equal to number of displayed icons', function () {
            //Compares previously retrieved number to number of studies in repeater
            element.all(by.repeater('vis in filteredDatasetsDrug')).then(function (vis) {
                expect(vis.length).toEqual(numDatasets);
            });
        });

        it('THEN number of Dataset Modules should be equal to number of lines in list view', function () {
            //Compares previously retrieved number to number of studies in repeater
            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
            portal.pause();
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function (vis) {
                expect(vis.length).toEqual(numDatasets);
            });
        });

        it('THEN if icons are sorted by drug programme, number of sections in icon view is equal to number of drug programmes', function () {
            element(by.id('gridDrugInput')).click();
            var numDrugsSections;
            element.all(by.repeater('drug in PARENT_MODEL.allAcls.datasets')).then(function (drugs) {
                numDrugsSections = drugs.length;
            });
            element(by.partialLinkText('Drug programmes')).getText().then(function (txt) {
                numProgrammes = parseInt(txt.substr(16, txt.length));//retrieving number of modules, displayed
                expect(numDrugsSections).not.toBeGreaterThan(numProgrammes);//some drug programmes can have no modules so these values can be not equal
                expect(numDrugsSections).toBeGreaterThan(numProgrammes/4);//but at least 1/4 of programmes is not empty
            });
        });

        it('THEN if icons are sorted by modules type, number sections is 6', function () {
            element(by.id('gridTypeInput')).click();
            element.all(by.repeater('moduleType in PARENT_MODEL.allAcls.datasets')).then(function (moduleTypes) {
                expect(moduleTypes.length).toEqual(6);
            });
        });


    });

    //Search works correctly
    describe('WHEN Dataset modules name is entered to Search field', function () {
        it('THEN the number of icons is reduced', function () {
            var expectedVisName = 'Safety_STDY4321_D';//ummy instance
            element(by.model('PARENT_MODEL.searchTerm.name')).sendKeys(expectedVisName);
            portal.pause();
            element.all(by.repeater('vis in filteredDatasetsDrug')).then(function(vis){
                expect(vis.length).toBe(1);
            });
        });

        it('THEN the number of lines in list view is reduced', function () {
            //switch to list view
            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
            var expectedVisName = 'Safety_STDY4321_D';//ummy instance
            element(by.model('PARENT_MODEL.searchTerm.name')).sendKeys(expectedVisName);
            portal.pause();
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function(vis){
                expect(vis.length).toBe(1);
            });
        });

        it('THEN after pressing Clear button Search field is clear again', function () {
            element(by.model('PARENT_MODEL.searchTerm.name')).sendKeys('some search');
            element(by.id('clearButton')).click();
            portal.pause();
            expect(element(by.model('PARENT_MODEL.searchTerm.name')).getText()).toBe('');
        });
    });

    describe('WHEN I expand Dataset module panel', function(){
        //Switching to list view
        beforeEach(function () {
            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
            portal.pause();
        });

        it('Setup button leads to dataset module setup',function(){
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function(allElements){
                //This will be used to verify, that there is no duplicates.
                var previousHtmls = [];

                //We will check 5% of elements randomly
                var randomIndexesToBeChecked = [];
                for (n=0; n<Math.floor(allElements.length/20); n++){
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
                    //expect(outerHtml).toContain(ptor.baseUrl); //disabled until complete integration
                    expect(outerHtml).toContain('/instance-setup?instanceId=');

                    //Veryfiing that there is no duplicate links
                    expect(previousHtmls.indexOf(outerHtml)).toEqual(-1);
                    previousHtmls[previousHtmls.length] = outerHtml;

                    //panel collapsed
                    element(by.linkText(elementName)).click();
                    portal.pause();
                }
            });
            //5% of modules got randomly checked
        });

        xit('View in spotfire link leads to spotfire wrapper',function(){
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function(allElements){
                //This will be used to verify, that there is no duplicates.
                var previousHtmls = [];

                //We will check ~4% of elements randomly
                var randomIndexesToBeChecked = [];
                for (n=0; n<Math.floor(allElements.length/25); n++){
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
                    var outerHtml = element(by.partialLinkText('View in Spotfire')).getOuterHtml();
                    //expect(outerHtml).toContain(ptor.baseUrl); //disabled until complete integration
                    expect(outerHtml).toContain('#/spotfireProxy/');

                    //Veryfiing that there is no duplicate links
                    expect(previousHtmls.indexOf(outerHtml)).toEqual(-1);
                    previousHtmls[previousHtmls.length] = outerHtml;

                    //panel collapsed
                    element(by.linkText(elementName)).click();
                    portal.pause();
                }
            });
            //5% of modules got randomly checked
        });

        xit('Inner panel layout contains all the required elements',function(){
            element.all(by.repeater('vis in PARENT_MODEL.allAcls.datasets')).then(function(allElements){
                //We will check 5% of elements randomly
                var randomIndexesToBeChecked = [];
                for (n=0; n<Math.floor(allElements.length/20); n++){
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

                    //Checking the layout
                    expect(element(by.xpath("//*[contains(text(),'Info')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Drug Programme:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Drug Components:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Date added to ACUITY:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Added by:')]")).isDisplayed()).toBe(true);

                    expect(element(by.xpath("//*[contains(text(),'Data content')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Number of studies within ACUITY:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Total number of dosed subjects in ACUITY:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Number of subjects:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Study data')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Subject groups')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'AE groups')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Lab groups')]")).isDisplayed()).toBe(true);

                    expect(element(by.xpath("//*[contains(text(),'Users')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Administrators:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Users:')]")).isDisplayed()).toBe(true);

                    expect(element(by.xpath("//*[contains(text(),'Visual content')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'Module type:')]")).isDisplayed()).toBe(true);
                    expect(element(by.xpath("//*[contains(text(),'ACUITY Views')]")).isDisplayed()).toBe(true);


                    //panel collapsed
                    element(by.linkText(elementName)).click();
                    portal.pause();
                }
            });
            //5% of modules got randomly checked
        });

    });
});
