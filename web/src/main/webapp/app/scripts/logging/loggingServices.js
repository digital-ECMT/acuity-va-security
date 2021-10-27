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

'use strict';

angular.module('acuityApp.loggingServices', [])
    /**
     * Logging services module, contains methods to call logging endpoints in backend 
     */
    .factory('StateLoggingService', ['$http', '$log', '$q', 'httpNotification', '$rootScope',
        function ($http, log, $q, httpNotification, $rootScope) {

            var service = {
                logState: function (toState, toParams) {

                    var url = buildUrl(toState, toParams);
                    if (url === null) {
                        console.log("No id found in toParams, not logging");
                        return null;
                    }

                    log.info('logging info to url ' + url);

                    var promise = $http.post(url)
                        .success(function (data, status) {
                            if (status === 200) {
                                log.info('log info successfully sent');
                            }
                            return data;
                        }).error(function (data, status) {
                        httpNotification.error("Error logging state change", status);
                    });
                    return promise;
                },
                registerForLogging: function ($rootScope) {

                    $rootScope.$on('$stateChangeSuccess',
                        function (event, toState, toParams, fromState, fromParams) {
                            if (toState.loggable && toState.loggable === true && getId(toParams)) {
                                service.logState(toState, toParams);
                            }
                        }
                    );
                }};

            /**
             * In some cases the datasetId/visualizationId is actually a String, like drug program,
             * so sending visId object with this to denote the actual datasetId
             */
            var getId = function (toParams) {
                var drugProgrammeId = getDrugProgrammeId(toParams);
                var studyId = getStudyId(toParams);
                var datasetId = getDatasetId(toParams);

                if (drugProgrammeId) {
                    return drugProgrammeId;
                } else if (studyId) {
                    return studyId;
                } else if (datasetId) {
                    return datasetId;
                } else {
                    return null;
                }
            };

            /**
             * is a visualization, if toParams has either datasetId,visualizationId
             */
            var getDatasetId = function (toParams) {
                if (toParams.visualizationId || toParams.datasetId) {
                    if (!_.isUndefined(toParams.visualizationId)) {
                        return toParams.visualizationId;
                    } else if (!_.isUndefined(toParams.datasetId)) {
                        return toParams.datasetId;
                    }
                } else {
                    return null;
                }
            };
            /**
             * is a study, if toParams has either studyId
             */
            var getStudyId = function (toParams) {
                return (toParams.studyId) ? toParams.studyId : null;
            };
            /**
             * is a Drug Programme, if toParams has either drugProgrammeId
             */
            var getDrugProgrammeId = function (toParams) {
                return (toParams.drugProgrammeId) ? toParams.drugProgrammeId : null;
            };

            /**
             * gets the module type, first looks in the state, and then the params
             */
            var getModuleType = function (toState, toParams) {
                if (toState.moduleType) {
                    return toState.moduleType.toLowerCase();
                } else if (toParams.moduleType) {
                    return toParams.moduleType.toLowerCase();
                } else {
                    return null;
                }
            };

            var buildUrl = function (toState, toParams) {

                var isWebApp = toState.name.toLowerCase().indexOf('spotfire') === -1;
                var moduleType = getModuleType(toState, toParams);
                if (moduleType === null) {
                    return null;
                }

                var stateName = toState.name;
                if (moduleType !== stateName) {
                    stateName = stateName.replace(moduleType, '');
                }

                if (getDrugProgrammeId(toParams)) {

                    return '/resources/logging/drugprogramme_log/webApp/' + isWebApp + 
                        '/moduleType/' + moduleType + '/analysisName/' + stateName + '?drugprogrammeId=' + getId(toParams);
                } else if (getStudyId(toParams)) {

                    return '/resources/logging/study_log/webApp/' + isWebApp + 
                        '/moduleType/' + moduleType + '/analysisName/' + stateName + '?studyId=' + getId(toParams);
                } else if (getDatasetId(toParams)) {

                    var isTabbed = !_.isUndefined(toState.tab);

                    var url = '/resources/logging/dataset_log/webApp/' + isWebApp +
                        '/moduleType/' + moduleType + '/analysisName/' + stateName + '?datasetId=' + getId(toParams);

                    if (isTabbed) {
                        return url + '&tab=' + toState.tab;
                    } else {
                        return url;
                    }
                } else {
                    return null;
                }
            };

            return service;
        }]);
