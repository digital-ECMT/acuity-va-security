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

// A reference configuration file.
exports.config = {
    seleniumAddress: 'http://localhost:4444/wd/hub',
//    seleniumServerJar: './node_modules/selenium-server-standalone-jar/jar/selenium-server-standalone-2.40.0.jar',
//    seleniumServerJar: './lib/selenium-server-standalone-2.42.0.jar',
    seleniumPort: null,
    chromeOnly: false,
    chromeDriver: './node_modules/chromedriver/bin/chromedriver',
    seleniumArgs: [],
    sauceUser: null,
    sauceKey: null,

    /*
     v   1. Launch the grid server, which listens on 4444 by default: java -jar /path/to/selenium-server-standalone-<SELENIUM VERSION>.jar -role hub
     ??? 2. Register with the hub: phantomjs --webdriver=8080 --webdriver-selenium-grid-hub=http://127.0.0.1:4444
     3. Now you can use your normal webdriver client with http://127.0.0.1:4444 and just request browserName: phantomjs
     */

    allScriptsTimeout: 120000,

    specs: [
        // Portal
//        'test/e2e/myAnalytics/*.js',
        'test/e2e/myAnalytics/clinicalStudy.js',
        'test/e2e/myAnalytics/drugProgrammes.js',
        'test/e2e/myAnalytics/visualisation.js',
        'test/e2e/myAnalytics/Search.js'
////        'test/e2e/userAccess/userAccessNegative.js' //excluded since may interact with other tests

        //Adverse Events
//        'test/e2e/safety/AdverseEvents/*.js',
//        'test/e2e/safety/aeSafetyTest.js',
//        'test/e2e/safety/AdverseEvents/numberOfSubjectsWithAEChart.js',
//        'test/e2e/safety/AdverseEvents/numberOfAesPerCategoryChart.js',
//        'test/e2e/safety/AdverseEvents/aesTimelineChart.js',
//        'test/e2e/safety/AdverseEvents/Navigation.js',
//        'test/e2e/safety/AdverseEvents/detailsOnDemand.js'

        //Labs
//        'test/e2e/safety/LAB/*.js',
//        'test/e2e/safety/LAB/detailsOnDemand.js',
//        'test/e2e/safety/LAB/labTrellisBoxChart.js',
//        'test/e2e/safety/LAB/labTrellisLineChart.js'

        //Liver
//        'test/e2e/safety/LIVER/*.js'

        //RENAL
//        'test/e2e/safety/RENAL/*.js'

        //QTcF
//        'test/e2e/safety/QTcF/*.js'

        //LVEF
//        'test/e2e/safety/LVEF/*.js'

        //Vitals
//        'test/e2e/safety/Vitals/*.js'

        //Data Management
//        'test/e2e/safety/DataManagment/*.js'

        //Conmeds
//        'test/e2e/safety/Conmeds/*.js'


    ],
    // Patterns to exclude.
    exclude: [
              'test/e2e/debugTests.js'
    ],

    // Alternatively, suites may be used. When run without a command line parameter,
    // all suites will run. If run with --suite=smoke, only the patterns matched
    // by that suite will run.
    suites: {
        analytics: 'test/e2e/myAnalytics/*.js',
        full: 'spec/e2e/*.js'
    },

    // Maximum number of total browser sessions to run. Tests are queued in
    // sequence if number of browser sessions is limited by this parameter.
    // Use a number less than 1 to denote unlimited. Default is unlimited.
    maxSessions: -1,

    // ----- Capabilities to be passed to the webdriver instance ----
    //
    // For a list of available capabilities, see
    // https://code.google.com/p/selenium/wiki/DesiredCapabilities
    // and
    // https://code.google.com/p/selenium/source/browse/javascript/webdriver/capabilities.js
    // Additionally, you may specify count, shardTestFiles, and maxInstances.


    capabilities: {
//        browserName: 'chrome',
//        browserName: 'phantomjs',

        'phantomjs.ghostdriver.path': './node_modules/phantomjs/lib/phantom/phantomjs.exe',
        'phantomjs.binary.path': './node_modules/phantomjs/lib/phantom/phantomjs.exe',
        'phantomjs.cli.args': ['--logfile=PATH', '--loglevel=DEBUG', '--localToRemoteUrlAccessEnabled=true'],

        // Number of times to run this set of capabilities (in parallel, unless
        // limited by maxSessions). Default is 1.
        count: 1,

        // If this is set to be true, specs will be sharded by file (i.e. all
        // files to be run by this set of capabilities will run in parallel).
        // Default is false.
        shardTestFiles: false,
        // Maximum number of browser instances that can run in parallel for this
        // set of capabilities. This is only needed if shardTestFiles is true.
        // Default is 1.
        maxInstances: 1
    },

    // If you would like to run more than one instance of webdriver on the same
    // tests, use multiCapabilities, which takes an array of capabilities.
    // If this is specified, capabilities will be ignored.
    multiCapabilities: [],

    // ----- More information for your tests ----
    //
    // A base URL for your application under test. Calls to protractor.get()
    // with relative paths will be prepended with this.
    baseUrl: 'http://localhost:9000/',

    // Selector for the element housing the angular app - this defaults to
    // body, but is necessary if ng-app is on a descendant of <body>
    rootElement: 'body',

    // A callback function called once protractor is ready and available, and
    // before the specs are executed
    // You can specify a file containing code to run by setting onPrepare to
    // the filename string.
    onPrepare: function () {
        var exec = require('child_process').exec;
        exec('phantomjs', function (error, stdout, stderr) {
            // output is in stdout
        });
        // At this point, global 'protractor' object will be set up, and jasmine
        // will be available. For example, you can add a Jasmine reporter with:
        //     jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
        //         'outputdir/', true, true));
    },

    // The params object will be passed directly to the protractor instance,
    // and can be accessed from your test. It is an arbitrary object and can
    // contain anything you may need in your test.
    // This can be changed via the command line as:
    //   --params.login.user 'Joe'
    params: {

    },

    // ----- The test framework -----
    //
    // Jasmine and Cucumber are fully supported as a test and assertion framework.
    // Mocha has limited beta support. You will need to include your own
    // assertion framework if working with mocha.
    framework: 'jasmine',

    // ----- Options to be passed to minijasminenode -----
    //
    // See the full list at https://github.com/juliemr/minijasminenode
    jasmineNodeOpts: {
        // onComplete will be called just before the driver quits.
        onComplete: null,
        // If true, display spec names.
        isVerbose: true,
        // If true, print colors to the terminal.
        showColors: true,
        // If true, include stack traces in failures.
        includeStackTrace: false,
        // Default time to wait in ms before a test fails.
        defaultTimeoutInterval: 120000
    },

    // ----- Options to be passed to mocha -----
    //
    // See the full list at http://visionmedia.github.io/mocha/
    mochaOpts: {
        ui: 'bdd',
        reporter: 'list'
    },

    // ----- Options to be passed to cucumber -----
    cucumberOpts: {
        // Require files before executing the features.
        require: 'cucumber/stepDefinitions.js',
        // Only execute the features or scenarios with tags matching @dev.
        // This may be an array of strings to specify multiple tags to include.
        tags: '@dev',
        // How to format features (default: progress)
        format: 'summary'
    },

    // ----- The cleanup step -----
    //
    // A callback function called once the tests have finished running and
    // the webdriver instance has been shut down. It is passed the exit code
    // (0 if the tests passed or 1 if not).
    onCleanUp: function () {
    }
};
