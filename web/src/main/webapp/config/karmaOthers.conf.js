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

// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function (config) {
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '../',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            'app/bower_components/jquery/dist/jquery.js',
            'app/bower_components/angular/angular.js',
            'app/bower_components/angular-mocks/angular-mocks.js',
            'app/bower_components/angular-resource/angular-resource.js',
            'app/bower_components/angular-cookies/angular-cookies.js',
            'app/bower_components/angular-sanitize/angular-sanitize.js',
            'app/bower_components/angular-animate/angular-animate.js',
            'app/bower_components/angular-route/angular-route.js',
            'app/bower_components/angular-bootstrap/ui-bootstrap.js',
            'app/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
            'app/bower_components/angular-ui-router/release/angular-ui-router.js',
            'app/bower_components/angular-native-dragdrop/draganddrop.js',
            'app/bower_components/AngularJS-Toaster/toaster.js',
            'app/bower_components/lodash/lodash.js',
            'app/bower_components/angular-underscore-module/angular-underscore-module.js',
            'app/bower_components/angular-rangeslider/angular.rangeSlider.js',
            'app/bower_components/angular-scroll/angular-scroll.js',
            'app/bower_components/ng-grid/build/ng-grid.js',
            'app/bower_components/ngstorage/ngStorage.js',
            'app/bower_components/ngDialog/js/ngDialog.js',
            'app/bower_components/bootstrap/dist/js/bootstrap.js',
            'app/bower_components/bootbox/bootbox.js',
            'app/bower_components/ngBootbox/dist/ngBootbox.js',
            'app/bower_components/bootstrap-ui-datetime-picker/dist/datetime-picker.min.js',
            'app/bower_components/ng-sortable/dist/ng-sortable.min.js',
            'app/bower_components/checklist-model/checklist-model.js',
            'app/scripts/*.js',
            'app/scripts/**/*.js',
            'test/mock/**/*.js',
            'test/spec/**/*.js'
        ],

        // generate js files from html templates to expose them during testing.
        preprocessors : {
            'app/scripts/**/*.js': ['coverage']
        },

        // list of files / patterns to exclude
        exclude: [
        ],

        // web server port
        port: 8081,
        browserNoActivityTimeout: 100000,
        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],

        plugins: [
            'karma-phantomjs-launcher',
            'karma-junit-reporter',
//            'karma-chrome-launcher',
//            'karma-firefox-launcher',
            'karma-script-launcher',
            'karma-jasmine',
            'karma-ng-html2js-preprocessor',
            'karma-coverage'
        ],


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false,

        reporters: ['progress', 'coverage', 'junit'],
        coverageReporter: { type : 'cobertura', dir : 'coverage/', file : 'js-coverage.xml' },
        junitReporter : {
          outputFile: 'test-results.xml'
        }
    });
};
