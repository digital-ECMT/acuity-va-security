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

//Goes through the ACUITY portal from the Analitycs: Clinical study datasets till all links.
//open homepage
describe('Clinical study datasets page tests', function () {

    beforeEach(function () {
        portal.setTestName('ClinicalStudies');
    });

    afterEach(function () {
        portal.logResult();
    });

    //Click 'Clinical study dataset' button and make sure that you moved to the
    describe('WHEN my analytics page is loaded and Clinical study datasets button clicked', function () {
        it('THEN user is redirected to Clinical study datasets page', function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/myAnalytics');
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
            expect(portal.getCurrentUrl()).toContain('/studies');
        })
    });

    //Make sure that number of Clinical study datasets is displayed
    describe('WHEN Clinical study dataset page is loaded', function () {
        var numStudies;

        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
        });

        it('THEN number of Clinical study datasets should be displayed and be greater than 0', function () {
            //checks displayed number of studies in 'Clinical study datasets' button
            element(by.partialLinkText('Clinical study datasets')).getText().then(function (txt) {
                numStudies = parseInt(txt.substr(24, txt.length));//retrieving number of clinical study datasets, displayed
                expect(numStudies).toBeGreaterThan(0);
            });
        });

        // Excluded, since icons view is currently disabled
        xit('THEN number of Clinical study datasets should be equal to number of displayed icons', function () {
            //Compares previously retrieved number to number of studies in repeater
            element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function (studies) {
                expect(studies.length).toEqual(numStudies);
            });
        });

        it('THEN number of Clinical study datasets should be equal to number of lines in list view', function () {
            //Compares previously retrieved number to number of studies in repeater
            //These lines are commented, since list view is opened by default already.
//            element(by.xpath("//*[contains(@class,'glyphicon-list')]")).click();
//            portal.pause();
            element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function (studies) {
                expect(studies.length).toEqual(numStudies);
            });
        });

    });

    //Make sure that "boxes view"  switch works between list and grid
    //Excluded, since there is no icon-list view switch at the moment
    xdescribe('WHEN "boxes view" switch button is pressed', function () {

        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
        });


        xit('THEN view is changed from list to boxes and Clinical study datasets names are displayed', function () {
            element(by.xpath("//*[contains(@class,'glyphicon-th-large')]")).click();
            portal.pause();
            var cStudy = element(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies').row(0)).getText();
            expect(cStudy).toContain('Clinical Study');

            //redirect back to list view
            element(by.css('span.glyphicon.glyphicon-list')).click();
            portal.pause();
        })
    });

    //Make sure that Search works correctly
    describe('WHEN Clinical study datasets is entered to Search field', function () {

        beforeEach(function () {
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
        });


        it('THEN the list of Clinical study datasets is reduced', function () {
            var expectedClinStudy = 'Dummy';
            element(by.id('searchInput')).sendKeys(expectedClinStudy);
            expect(element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).count()).toEqual(1);
        });

        it('THEN after pressing Clear button Search field is clear again', function () {
            element(by.id('searchInput')).sendKeys('Some Text');
            portal.pause();
            element(by.id('clearButton')).click();
            portal.pause();
            expect(element(by.model('PARENT_MODEL.searchTerm.name')).getText()).toBe('');
        })
    });

    describe('WHEN I expand study panel', function(){
        //Opening Clinical Studies projection
        beforeEach(function () {
            portal.getMainPage();
            element(by.partialLinkText('Analytics')).click();
            portal.pause();
            element(by.partialLinkText('Clinical study datasets')).click();
            portal.pause();
        });

        it('Setup button leads to clinical study setup',function(){
            element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function(allStudies){
                //This will be used to verify, that there is no duplicates.
                var previousHtmls = [];

                //We will check 5% of studies randomly
                var randomStudiesToBeChecked = [];
                for (n=0; n<Math.floor(allStudies.length/20); n++){
                    var candidate = Math.floor((Math.random() * allStudies.length) + 1);
                    if(randomStudiesToBeChecked.indexOf(candidate)==-1){
                        randomStudiesToBeChecked[randomStudiesToBeChecked.length] = candidate;
                    }
                }
                randomStudiesToBeChecked.sort(function(a, b){return a-b});
                console.log('Random studies: '+randomStudiesToBeChecked);

                for(i=0; i<randomStudiesToBeChecked.length; i++){
                    var studyName = allStudies[randomStudiesToBeChecked[i]].getText();
                    element(by.linkText(studyName)).click();
                    portal.pause();
                    //Panel got expanded

                    //Checking the setup link
                    var outerHtml = element(by.partialLinkText('Setup')).getOuterHtml();
                    //expect(outerHtml).toContain(ptor.baseUrl); //disabled until complete integration
                    expect(outerHtml).toContain('/study-setup?studyId=');

                    //Veryfiing that there is no duplicate links
                    expect(previousHtmls.indexOf(outerHtml)).toEqual(-1);
                    previousHtmls[previousHtmls.length] = outerHtml;

                    //panel collapsed
                    element(by.linkText(studyName)).click();
                    portal.pause();
                }
            });
            //5% of studies randomly checked
        });

        it('Data upload status link is correct',function(){
            element.all(by.repeater('study in PARENT_MODEL.allAcls.clinicalStudies')).then(function(allStudies){
                //This will be used to verify, that there is no duplicates.
                var previousHtmls = [];

                //We will check 5% of studies randomly
                var randomStudiesToBeChecked = [];
                for (n=0; n<Math.floor(allStudies.length/20); n++){
                    var candidate = Math.floor((Math.random() * allStudies.length) + 1);
                    if(randomStudiesToBeChecked.indexOf(candidate)==-1){
                        randomStudiesToBeChecked[randomStudiesToBeChecked.length] = candidate;
                    }
                }
                randomStudiesToBeChecked.sort(function(a, b){return a-b});
                console.log('Random studies: '+randomStudiesToBeChecked);

                for(i=0; i<randomStudiesToBeChecked.length; i++){
                    var studyName = allStudies[randomStudiesToBeChecked[i]].getText();
                    element(by.linkText(studyName)).click();
                    portal.pause();
                    //Panel got expanded

                    //Checking the setup link
                    var outerHtml = element(by.xpath("//*[contains(text(),'Data upload status:')]")).getOuterHtml();
                    //expect(outerHtml).toContain(ptor.baseUrl); //disabled until complete integration
                    expect(outerHtml).toContain('/report?studyCode=');

                    //Veryfiing that there is no duplicate links
                    expect(previousHtmls.indexOf(outerHtml)).toEqual(-1);
                    previousHtmls[previousHtmls.length] = outerHtml;

                    //panel collapsed
                    element(by.linkText(studyName)).click();
                    portal.pause();
                }
            });
            //5% of studies randomly checked
        });

    });
});
