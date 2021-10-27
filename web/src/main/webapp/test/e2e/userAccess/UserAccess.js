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

describe('User Management panel', function () {
    portal.getMainPage();

    beforeEach(function () {
        portal.setTestName("UserAccess");
    });

    afterEach(function () {
        portal.logResult();
    });

    //Check that Edit button on the selected drug program panel works
    describe('WHEN Edit button is pressed', function () {

        //Not verified yet.
        xit('THEN Edit Users panel is opened', function () {
            openedPanel = element.all(by.css('div.panel-body.ng-scope')).get(3);
            openedPanel.all(by.css('a.btn.btn-link.pull-right')).get(3).click();
            browser.waitForAngular();
            ptor.sleep(10000);
            takeScreenShot(testNumber);
            var expectedUser = 'Semenova';
            ptor.findElement(protractor.By.model('MODEL.searchUsersTerm.name')).sendKeys(expectedUser);
            expect(element.all(by.repeater('MODEL.permissions.trainedUsers | filterBySearchTerm:MODEL.searchUsersTerm')).count()).toBe(1);
            takeScreenShot(testNumber);
        })

    });
});
