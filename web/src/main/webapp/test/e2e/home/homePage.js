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

//Most test are commented, because most buttons do not work
//Goes through the ACUITY portal from the Homepage till all links.
//open homepage

var fs = require('fs');
var testNumber = 1;
var ptor;

//This function is to make screenshots in the PhantomJS browser
var takeScreenShot = function (number) {
    browser.takeScreenshot().then(function (png) {
        var stream = fs.createWriteStream("./screenshots/homePageTest" + number + ".png");
        stream.write(new Buffer(png, 'base64'));
        stream.end();
    });
};

var waitPageToLoad = function () { //waits for footer to appear
    var expectedElement = by.partialLinkText('ACUITY');
    browser.wait(function () {
        return ptor.isElementPresent(expectedElement);
    }, 30000);
};


var paws = function () {
    ptor.sleep(5000);
};


//Goes through the ACUITY portal from Home page tests till all links.
//open homepage
describe('Home page tests', function () {
    browser.driver.manage().window().setSize(1600, 800);
    ptor = protractor.getInstance();
    ptor.ignoreSynchronization = true;

    beforeEach(function () {
        ptor.get(ptor.baseUrl);
        waitPageToLoad();
    });

    afterEach(function () {
        takeScreenShot(testNumber);
        var passed = jasmine.getEnv().currentSpec.results().passed();
        if (passed) {
            console.log("Home page - TEST " + testNumber + " PASSED");
        }
        else {
            console.log("Home page - TEST " + testNumber + " FAILED");
        }
        testNumber++;
    });


//Check the top elements panel
    describe('Navigation bar tests', function () {
        var navBar;

        beforeEach(function () {
            ptor.get(targetUrl);
            browser.waitForAngular();
            navBar = element(By.css('div.navbar-collapse.collapse'));
        })

    });

//Click 'Analytics' button and make sure that you moved to the 'http://http://localhost:9000/#/myAnalytics/datasets' page
    describe('WHEN Analitics button clicked', function () {
        it('THEN user is redirected to dataset', function () {

            var AnButton = ptor.findElement(protractor.By.linkText('Analytics'));
            AnButton.click();
            paws();
            expect(ptor.getCurrentUrl()).toContain('/datasets');


        })
    });


//Click 'About' button and make sure that you moved to the 'http://http://localhost:9000/#/about/' page
    describe('WHEN About button clicked', function () {
        it('THEN user is redirected to about page', function () {

            var aboutBtn = ptor.findElement(protractor.By.linkText('About'));
            aboutBtn.click();
            paws();
            expect(ptor.getCurrentUrl()).toContain('/about');

        })
    });


//Click 'News' button and make sure that you moved to the 'http://http://localhost:9000/#/news' page
    describe('WHEN News button clicked', function () {
        it('THEN user is redirected to News page', function () {

            var NewsBtn = ptor.findElement(protractor.By.linkText('News'));
            NewsBtn.click();
            paws();
            expect(ptor.getCurrentUrl()).toContain('/news');

        })
    });


//Click 'Help' button and make sure that you moved to the 'http://http://localhost:9000/#/help' page
    describe('WHEN Help button clicked', function () {
        it('THEN user is redirected to Help page', function () {

            var HelpBtn = ptor.findElement(protractor.By.linkText('Help'));
            HelpBtn.click();
            paws();
            expect(ptor.getCurrentUrl()).toContain('/help');

        })
    });

//Check the login informations. Verify that dropdown menu contains 'Preferens...','My resources...'
    describe('Login information test', function () {
        var logInf;
        it('', function () {

            var logInf = ptor.findElement(protractor.By.css('a.dropdown-toggle.pull-right')).getText();
            expect(logInf).not.toBe(null);

        })
    });


//Check the Options. Verify that dropdown menu contains 'Set User Acces'
    xdescribe('Option test', function () {

        it('WHEN user  double clicked, menu is appears', function () {

            element.all(By.css('a.dropdown-toggle')).get(1).click();
            browser.waitForAngular();
            element.all(By.css('a.dropdown-toggle')).get(1).click();
            element.all(By.css('ul.dropdown-menu'))
                .get(0)
                .then(function (menu) {
                    expect(menu.all(by.css('a')).get(0).getText()).toContain('Set User Access');
                });

        })
    });

//Click 'Learn More' button and make sure that the detailed description ACUITY opened
    describe('WHEN Learn More button is clicked', function () {
        it('THEN user is redirected to about page', function () {
            ptor.findElement(protractor.By.linkText('Learn more')).click();
            browser.waitForAngular();
            expect(ptor.getCurrentUrl()).toContain('/about');
        })
    });


//Click 'Go to training' button and make sure that the training opened
    describe('WHEN Go to training button is clicked', function () {
        it('THEN user is redirected to about page', function () {
            ptor.findElement(protractor.By.linkText('Go to training')).click();
            browser.waitForAngular();
            expect(ptor.getCurrentUrl()).toContain('/training');
        })
    });

//Click 'Try it!' button and make sure that the Dummy modules are opened
    describe('WHEN Try it button is clicked', function () {
        it('THEN user is redirected to Dummy modules', function () {
            var target =element(by.linkText('Try it!')).getOuterHtml();
            expect(target).toContain('/datasets/STDY4321');
        })
    });

//Click 'Go to news' button and make sure that news opened
    describe('WHEN Go to news button is clicked', function () {
        it('THEN user is redirected to news page', function () {
            ptor.findElement(protractor.By.linkText('Go to news')).click();
            browser.waitForAngular();
            expect(ptor.getCurrentUrl()).toContain('/news');
        })
    });


//Check Training sections
    describe('Training section test', function () {
        it('Training section contains correct text ', function () {
            expect(ptor.findElement(protractor.By.id('trainingSummary')).getText()).toContain('Training');
            expect(ptor.findElement(protractor.By.id('trainingSummary')).getText()).toContain('An interactive training resource is available online to help you get to grips with ACUITY concepts. The training can be revisited at any time to refresh your memory.');
        });


//Check Discover ACUITY sections
        it('Discover ACUITY section contains correct text ', function () {
            expect(ptor.findElement(protractor.By.id('discoverSummary')).getText()).toContain('Discover ACUITY');
            expect(ptor.findElement(protractor.By.id('discoverSummary')).getText()).toContain('Fully operational, demo versions of ACUITY tools that contain mock-data are available for all users and guests to explore. Follow the link below to discover what ACUITY can do.');

        });

//Check Feedback sections
        it('Feedback section contains correct text ', function () {
            expect(ptor.findElement(protractor.By.id('feedbackSummary')).getText()).toContain('Feedback');
            expect(ptor.findElement(protractor.By.id('feedbackSummary')).getText()).toContain('If you have any feedback, queries or problems with ACUITY please email the team and we');

        });

//Check News sections

        it('News section contains correct text ', function () {
            expect(ptor.findElement(protractor.By.id('newsSummary')).getText()).toContain('News');
            expect(ptor.findElement(protractor.By.id('newsSummary')).getText()).toContain('In this section, you can see the latest news stories from the ACUITY team. These include the completion of new views, the release of new versions of ACUITY, and other things. From the archive section, you can also access all previous newsletters and stories.');

        })
    });

    //Make sure that Footer is not empty and contains information about Branch, Commit, Commit time, Commit user, Build time, Build user
    describe('Footer section test', function () {
        it('Click ACUITY link', function () {
            var btn = ptor.findElement(protractor.By.partialLinkText('ACUITY'));
            btn.click();
            paws();
            ptor.sleep(3000);
            expect(ptor.findElement(protractor.By.id('collapseVersionDetails')).isDisplayed());

            expect(ptor.findElement(protractor.By.css('dd:nth-child(2)')).getText()).toContain('origin/master');
            expect(ptor.findElement(protractor.By.css('dd:nth-child(4)')).getText()).not.toBe(null);
            expect(ptor.findElement(protractor.By.css('dd:nth-child(6)')).getText()).not.toBe(null);
            expect(ptor.findElement(protractor.By.css('dd:nth-child(8)')).getText()).toContain('CEST');
            expect(ptor.findElement(protractor.By.css('dd:nth-child(10)')).getText()).not.toBe(null);
            expect(ptor.findElement(protractor.By.css('dd:nth-child(12)')).getText()).toContain('CEST');
            expect(ptor.findElement(protractor.By.css('dd:nth-child(14)')).getText()).not.toBe(null);
        })


    })

});








