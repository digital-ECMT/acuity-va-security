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

exports.config = {
    // The address of a running selenium server. If this is specified,
    // seleniumServerJar and seleniumPort will be ignored.
    seleniumAddress: 'http://localhost:4444/wd/hub',

    // A base URL for your application under test. Calls to protractor.get()
    // with relative paths will be prepended with this.
    baseUrl: 'http://localhost:9000/',
    // Capabilities to be passed to the webdriver instance.
    capabilities: {
        'browserName': 'phantomjs',

        /*
         * Can be used to specify the phantomjs binary path.
         * This can generally be ommitted if you installed phantomjs globally.
         */
        'phantomjs.binary.path': './node_modules/phantomjs/bin/phantomjs',

        /*
         * Command line arugments to pass to phantomjs.
         * Can be ommitted if no arguments need to be passed.
         * Acceptable cli arugments: https://github.com/ariya/phantomjs/wiki/API-Reference#wiki-command-line-options
         */
        'phantomjs.cli.args': ['--logfile=PATH', '--loglevel=DEBUG']
    },


    // Spec patterns are relative to the current working directly when
    // protractor is called.
    specs: [
        // 'test/e2e/home/homePage.js'
        'test/e2e/myAnalytics/clinicalStudy.js',
        'test/e2e/myAnalytics/drugProgrammes.js',
        'test/e2e/myAnalytics/Search.js'
        // ,'test/e2e/userAccess/userAccessNegative.js'
    ],


    // Options to be passed to Jasmine-node.
    jasmineNodeOpts: {
        //onComplete: false,//use function to teardown database and rebuild etc....
        isVerbose: true,
        showColors: true,
        includeStackTrace: false,
        defaultTimeoutInterval: 60000
    }
};
