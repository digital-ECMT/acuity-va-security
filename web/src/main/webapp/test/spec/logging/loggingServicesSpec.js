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

describe('Service: loggingService', function () {

    'use strict';

    var $httpBackend, service;

    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.loggingServices'));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('logState stateLoggingService test 1', function () {

        var logResponse = '{}';

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;

                service = injector.get('StateLoggingService');
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('POST', '/resources/logging/dataset_log/webApp/true/moduleType/safety/analysisName/Aes?datasetId=145&tab=aes')
                    .respond(200, logResponse);
            });
        });

        it('should log state change state to server with tab and visualizationId', function () {

            var toState = {name: 'safetyAes',
                url: '/aes/',
                loggable: true,
                moduleType: 'safety',
                tab: 'aes'};

            var toParams = {visualizationId: 145};

            service.logState(toState, toParams).then(function (response) {
                console.log("Log call response " + response.status);
                expect(response.status).toBe(200);
            });

            $httpBackend.flush();
        });
    });

    describe('logState stateLoggingService test 2', function () {

        var logResponse = '{}';

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;

                service = injector.get('StateLoggingService');
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('POST', '/resources/logging/dataset_log/webApp/true/moduleType/safety/analysisName/Aes?datasetId=145')
                    .respond(200, logResponse);
            });
        });

        it('should log state change state to server with no tab but with a datasetId', function () {

            var toState = {name: 'safetyAes',
                url: '/aes/',
                loggable: true,
                moduleType: 'safety'
            };

            var toParams = {datasetId: 145};

            service.logState(toState, toParams).then(function (response) {
                console.log("Log call response " + response.status);
                expect(response.status).toBe(200);
            });

            $httpBackend.flush();
        });
    });

    describe('logState stateLoggingService test 4', function () {

        var logResponse = '{}';

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;

                service = injector.get('StateLoggingService');
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('POST', '/resources/logging/dataset_log/webApp/false/moduleType/safety/analysisName/spotfireProxy?datasetId=145')
                    .respond(200, logResponse);
            });
        });

        it('should log state change state to server with spotFire view', function () {

            var toState = {name: 'spotfireProxy',
                url: '/safety/:visualizationId/spotfireProxy',
                loggable: true,
                moduleType: 'safety'
            };

            var toParams = {visualizationId: 145};

            service.logState(toState, toParams).then(function (response) {
                console.log("Log call response " + response.status);
                expect(response.status).toBe(200);
            });

            $httpBackend.flush();
        });
    });

    describe('logState stateLoggingService test 5', function () {

        var logResponse = '{}';

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;

                service = injector.get('StateLoggingService');
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('POST', '/resources/logging/study_log/webApp/true/moduleType/reports/analysisName/reports?studyId=145')
                    .respond(200, logResponse);
            });
        });

        it('should log state change state to server for study reports', function () {

            var toState = {name: 'reports',
                url: '/report/:studyId',
                moduleType: 'Reports',
                loggable: true
            };

            var toParams = {studyId: 145};

            service.logState(toState, toParams).then(function (response) {
                console.log("Log call response " + response.status);
                expect(response.status).toBe(200);
            });

            $httpBackend.flush();
        });
    });

    describe('logState stateLoggingService test 6', function () {

        var logResponse = '{}';

        beforeEach(function () {

            inject(function ($injector) {
                var injector = $injector;

                service = injector.get('StateLoggingService');
                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('POST', '/resources/logging/drugprogramme_log/webApp/false/moduleType/safety/analysisName/spotfireProxyDashBoard?drugprogrammeId=145')
                    .respond(200, logResponse);
            });
        });

        it('should log state change state to server for spotfire dashboard', function () {

            var toState = {name: 'spotfireProxyDashBoard',
                url: '/spotfireProxy/:datasetType/:datasetId/:moduleType/:drugProgrammeId',
                controller: 'SpotfireController',
                loggable: true,
            };

            var toParams = {drugProgrammeId: 145, datasetId: 'DRUGNAME', moduleType: 'Safety'};

            service.logState(toState, toParams).then(function (response) {
                console.log("Log call response " + response.status);
                expect(response.status).toBe(200);
            });

            $httpBackend.flush();
        });
    });
    
    describe('logState stateLoggingService shouldnt log test 7', function () {

        it('should not log state change state to server if moduleType is missing', function () {

            var toState = {name: 'spotfireProxyDashBoard',
                url: '/spotfireProxy/:datasetType/:datasetId/:moduleType/:drugProgrammeId',
                controller: 'SpotfireController',
                loggable: true,
            };

            var toParams = {drugProgrammeId: 145, datasetId: 'DRUGNAME'};

            var returnedValue = service.logState(toState, toParams);

            expect(returnedValue).toBe(null);
        });
        
        it('should not log state change state to server if all ids missing', function () {

            var toState = {
                name: 'spotfireProxyDashBoard',
                url: '/spotfireProxy/:datasetType/:datasetId/:moduleType/:drugProgrammeId',
                controller: 'SpotfireController',
                loggable: true,
            };

            var toParams = {};

            var returnedValue = service.logState(toState, toParams);

            expect(returnedValue).toBe(null);
        });
    });
});



