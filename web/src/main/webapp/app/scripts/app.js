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

angular.module('acuityApp', [
    'ngAnimate',
    'ngCookies',
    'ngStorage',
    'ngResource',
    'ngSanitize',
    'ui.router',
    'as.sortable',
    'ui.bootstrap',
    'ui.bootstrap.datetimepicker',
    'toaster',
    'underscore',
    'ui-rangeSlider',
    'ang-drag-drop',
    'ngGrid',
    'duScroll',
    'ngDialog',
    'ngBootbox',
    'checklist-model',
    'acuityApp.directives',
    'acuityApp.filters',
    'acuityApp.utilServices',
    'acuityApp.loggingServices',
    'acuityApp.constants',
    'acuityApp.templateControllers',
    'acuityApp.securityDirectives',
    'acuityApp.securityServices',
    'acuityApp.myAnalyticsServices',
    'acuityApp.myAnalyticsControllers',
    'acuityApp.myAnalyticsUserAccessControllers',
    'acuityApp.myAnalyticsFilters'

]).config(function ($provide, $httpProvider, $stateProvider, $locationProvider, $urlRouterProvider) {

    // marks all ajax requests with this header so the server can determine them as ajax
    $httpProvider.defaults.headers.common = {
        'X-Requested-With': 'XMLHttpRequest'
    };

    // 419 means session timed out, so reload the page and force relogin
    $provide.factory('AuthHttpInterceptor', function ($q, $window) {
        return {
            responseError: function (rejection) {

                if (rejection.status === 419) {
                    console.log("Session timed out, reloading page");
                    bootbox.alert("Your session has timed out after 6 hours of inactivity, the application will reload to automatically log you back in.", function () {
                        // replace #/users/2/ with ?redirect=/users/2/
                        var hash = $window.location.hash;
                        var hashReplaced = hash.replace("#", "?redirect=");
                        console.log("Reloading.. " + $window.location.origin + "/" + hashReplaced);
                        $window.location.replace($window.location.origin + "/" + hashReplaced); //replace is history and reload
                    });
                } else {
                    return $q.reject(rejection);
                }
            }
        };
    });
    $httpProvider.interceptors.push('AuthHttpInterceptor');

    // Set up default /myAnalytics page
    $urlRouterProvider.when('/myAnalytics', '/myAnalytics/datasets');
    // For any unmatched URL, send to /home
    $urlRouterProvider.otherwise('/');

    $stateProvider
        .state('template', {
            /**
             * Defer the loading of the inital page until the security model has been initialised from the server
             */
            resolve: {
                username: function (SecurityModel) {
                    return SecurityModel.initWhoami();
                },
                acls: function (SecurityModel) {
                    return SecurityModel.initAcls();
                },
                permissionsConfiguration: function (SecurityModel) {
                    return SecurityModel.initPermissionsConfiguration();
                }
            },
            views: {
                '': {
                    templateUrl: 'views/template.html'
                }
            }
        })
    /**
     * RCT-3998, html5 mode didnt solve it, so the timeout now will add the redirect url to the ?redirect to the url
     */
        .state('homeRedirect', {
            url: '/?redirect',
            parent: 'template',
            templateUrl: 'views/home.html',
            controller: function ($scope, $window, SecurityModel) {
                // Pulls out all request params into array
                var parseQueryString = function () {

                    var str = $window.location.search;
                    var objURL = {};

                    str.replace(
                        new RegExp("([^?=&]+)(=([^&]*))?", "g"),
                        function ($0, $1, $2, $3) {
                            objURL[ $1 ] = $3;
                        }
                    );
                    return objURL;
                };
                $scope.isTrainedUser = SecurityModel.getAcls().length !== 0 && _.findIndex(SecurityModel.getAuthorities(), function (authority) {
                    return authority == "TRAINED_USER";
                }) != -1;
                var redirect = parseQueryString().redirect;
                if (redirect !== null && !_.isUndefined(redirect)) {
                    var redirectReplaced = "#" + redirect;
                    $window.location.replace($window.location.origin + "/" + redirectReplaced); //replace is history and reload
                }
            }
        })
        .state('home', {
            url: '/',
            parent: 'template',
            templateUrl: 'views/home.html',
            controller: function ($scope, SecurityModel) {
                $scope.isTrainedUser = SecurityModel.getAcls().length !== 0 && _.findIndex(SecurityModel.getAuthorities(), function (authority) {
                    return authority == "TRAINED_USER";
                }) != -1;
            }
        })

        .state('myAnalytics', {
            url: '/myAnalytics',
            parent: 'template',
            templateUrl: 'views/myAnalytics/analytics.html',
            controller: 'MyAnalyticsController'
        })
        .state('userAccess', {
            url: '/userAccess',
            parent: 'template',
            templateUrl: 'views/myAnalytics/userAccess.html',
            controller: 'UserAccessController'
        })
        .state('groupAccess', {
            url: '/groupAccess',
            parent: 'template',
            templateUrl: 'views/myAnalytics/groupAccess.html',
            controller: 'GroupUserAccessController as group',
            controllerAs: 'group'
        })
        .state('trainedUsers', {
            url: '/trainedUsers',
            parent: 'template',
            templateUrl: 'views/myAnalytics/trainedUsers.html',
            controller: 'TrainedUsersController'
        })
        .state('noneTrainedUsers', {
            url: '/noneTrainedUsers',
            parent: 'template',
            templateUrl: 'views/myAnalytics/noneTrainedUsers.html',
            controller: 'NoneTrainedUsersController'
        })
        .state('individualUsers', {
            url: '/individualUsers',
            parent: 'template',
            templateUrl: 'views/myAnalytics/individualUsers.html',
            controller: 'IndividualUsersController'
        })
        .state('drugProgrammes', {
            url: '/drug-programmes',
            parent: 'myAnalytics',
            templateUrl: 'views/myAnalytics/drugProgrammes.html',
            controller: function ($scope) {
                $scope.PARENT_MODEL.selectedRow.id = 0;
            }
        })
        .state('studies', {
            url: '/studies',
            parent: 'myAnalytics',
            templateUrl: 'views/myAnalytics/studies.html',
            controller: function ($scope) {
                $scope.PARENT_MODEL.selectedRow.id = 1;
            }
        })
        .state('studiesSearch', {
            url: '/studies/:searchTerm',
            parent: 'myAnalytics',
            templateUrl: 'views/myAnalytics/studies.html',
            controller: function ($scope, $stateParams) {
                $scope.PARENT_MODEL.searchTerm.name = decodeURIComponent($stateParams.searchTerm); //double esacped to allow / in path
                $scope.PARENT_MODEL.selectedRow.id = 1;
            }
        })
        .state('datasets', {
            url: '/datasets',
            parent: 'myAnalytics',
            templateUrl: 'views/myAnalytics/datasets.html',
            controller: function ($scope, $window, $state) {

                var hash = $window.location.hash;
                hash = hash.replace("#/", "?redirect=");
                var hashReplaced = hash.replace("?redirect=", "#/");
                // href = href
                console.log($window.location.origin + "/" + hashReplaced);
                $scope.PARENT_MODEL.selectedRow.id = 2;
            }
        })
        .state('datasetsSearch', {
            url: '/datasets/:searchTerm',
            parent: 'myAnalytics',
            templateUrl: 'views/myAnalytics/datasets.html',
            controller: function ($scope, $stateParams) {
                $scope.PARENT_MODEL.searchTerm.name = decodeURIComponent($stateParams.searchTerm); //double esacped to allow / in path
                $scope.PARENT_MODEL.selectedRow.id = 2;
            }
        })
        .state('moduleTemplate', {
            parent: 'template',
            controller: 'moduleTemplateController',
            templateUrl: 'views/template.html'
        });
})
/**
 * When running the application, watch for global state changes and log these to server.
 *
 * @param {type} $rootScope
 * @param {type} StateLoggingService
 * @returns {undefined}
 */
    .run(['$rootScope', 'StateLoggingService', function ($rootScope, StateLoggingService) {
        StateLoggingService.registerForLogging($rootScope);
    }]
);
