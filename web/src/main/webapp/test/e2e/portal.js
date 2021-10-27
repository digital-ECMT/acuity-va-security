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
var testName = 'unknown';
var ptor;
var initialized = false;
var disclaimerSeen = false;
var customLogging = false; // ==!protractor.jasmineNodeOpts.isVerbose
//module.exports.iniialized = initialized;
//module.exports.ptor = ptor;
module.exports.testName = testName;
module.exports.testNumber = testNumber;


// ██████
// █ Utils █
// ██████

var setTestName = function(name){
    if(testName != name){
        testName = name;
        testNumber = 1;
    }
};
module.exports.setTestName = setTestName;

//Screenshots
var takeScreenShot = function (postfix) {
    browser.takeScreenshot().then(function (png) {
        var stream = fs.createWriteStream("./screenshots/" +testName + postfix + ".png");
        stream.write(new Buffer(png, 'base64'));
        stream.end();
    });
};
module.exports.takeScreenShot = 'takeScreenShot';

//Waits and delays
var waitForElement = function (by) {
    browser.wait(function () {
        return ptor.isElementPresent(by);
    }, 30000);
};
module.exports.waitForElement = waitForElement;

var waitForElementToVanish = function (by) {
    browser.wait(function () {
        return !ptor.isElementPresent(by);
    }, 30000);
};
module.exports.waitForElementToVanish = waitForElementToVanish;

var waitForLink = function (linkText) {
    var expectedElement = by.partialLinkText(linkText);
    browser.wait(function () {
        return ptor.isElementPresent(expectedElement);
    }, 30000);
};
module.exports.waitForLink = waitForLink;

var waitPageToLoad = function () {
    waitForLink('ACUITY');
};
module.exports.waitPageToLoad = waitPageToLoad;

var pause = function () {
    ptor.waitForAngular();
    ptor.sleep(2000);
};
module.exports.pause = pause;

//Scrolls
var scrollToLocator = function(elemFinder) {
    var promise = browser.executeScript(function(elem) {
        elem.scrollIntoView(false);
    }, elemFinder);
    return promise;
};
module.exports.scrollToLocator = scrollToLocator;
//Util
var randomIndexes = function(size, percentage){
    if(percentage<=0){
        return [];
    }

    if(percentage>=100){
        percentage=100;
    }

    var ri = [];

    for (n=0; n<Math.floor(percentage*size/100); n++){
        var candidate = Math.floor(Math.random() * size);
        if(ri.indexOf(candidate)==-1){
            ri[ri.length] = candidate;
        }
    }
    ri.sort(function(a, b){return a-b});
    return ri;
};
module.exports.randomIndexes = randomIndexes;

var moveMouseOut = function(){
    browser.actions().mouseMove({x: 0, y: 0});
    pause();
};
module.exports.moveMouseOut = moveMouseOut;

//Logging
var logResult = function () {
    takeScreenShot(testNumber);
    //  Custom logging if isVerbose is false.
//    console.log('Ptor IsVerbose: = ' + customLogging);
    if(customLogging==true){
        var passed = jasmine.getEnv().currentSpec.results().passed();
        var specName = jasmine.getEnv().currentSpec.description;
        if (passed) {
            console.log("[PASSED] " + testName + " " + testNumber + ": '" + specName+"'");
        } else {
            console.log("[*FAILED*] " + testName + " " + testNumber + ": '" + specName+"'");
        }
    }

    testNumber++;
};
module.exports.logResult = logResult;

// Browser operations
var getCurrentUrl = function(){
    return ptor.getCurrentUrl();
};
module.exports.getCurrentUrl = getCurrentUrl;


// ████████
// █ Main Page █
// ████████

var getMainPage = function(){
    if(!initialized){
        console.log('Initializing protractor.');
//        browser.driver.manage().window().setSize(1600, 800);
        browser.driver.manage().window().setSize(1920, 1080);
        browser.driver.manage().window().maximize();
        ptor = protractor.getInstance();
        ptor.ignoreSynchronization = false;
        module.exports.ptor = ptor;
    }
    ptor.get(ptor.baseUrl);
    ptor.sleep(15000);
    takeScreenShot('PageLoad');
    waitPageToLoad();
    pause();

    if(!initialized) {
        ptor.sleep(5000);
        initialized = true;
    }
};
module.exports.getMainPage = getMainPage;


// ████████
// █ AE Safety █
// ████████

var getAESafety = function(){
    getMainPage();
    element(by.partialLinkText('Analytics')).click();
    pause();
    element(by.id('searchInput')).sendKeys('1234');
    element(by.partialLinkText('Safety\nSTDY4321_Dummy_Instance')).click();

    pause();

    if(!disclaimerSeen){
        element(by.css('button.ngdialog-button.ngdialog-button-primary')).click();
        disclaimerSeen = true;
    }

    waitForElement(by.xpath("//*[contains(text(),'Selected AE Summary')]"));
//    waitForElementToVanish(by.css(".trbl-loader"));
    pause(); // wait for animation to complete
};
module.exports.getAESafety = getAESafety;

getMainPage();
