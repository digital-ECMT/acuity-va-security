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

angular.module('acuityApp.utilServices', [])

/**
 * Uses the notification service to determine the message give to the use from the status code.
 */
    .factory('httpNotification', ['toaster', function (toaster) {

        var determineMessage = function (status, item) {
            var itemDefined = true;
            if (_.isUndefined(item)) {
                itemDefined = false;
            }
            var statusCode = parseInt(status);

            return itemDefined ? getMessage(statusCode, item) : getDefaultMessage(statusCode);
        };

        var getMessage = function (statusCode, item) {
            var result = "Unknown status code returned " + statusCode;
            switch (statusCode) {
                case 401: // Unauthorised
                    result = "You do not have the correct permissions to perform this operation on " + item;
                    break;
                case 404: // Not found
                    result = item + " not found";
                    break;
                case 409: // Conflict
                    result = item + " already exists";
                    break;
                case 500: // Internal Server Error
                    result = "An error has occurred on the server";
                    break;
            }
            return result;
        };

        var getDefaultMessage = function (statusCode) {
            var result = "Unknown status code returned " + statusCode;
            switch (statusCode) {
                case 401: // Unauthorised
                    result = "You do not have the correct permissions to perform this operation";
                    break;
                case 404: // Not found
                    result = "Item not found";
                    break;
                case 409: // Conflict
                    result = "Item already exists";
                    break;
                case 500: // Internal Server Error
                    result = "An error has occurred on the server";
                    break;
            }
            return result;
        };

        var service = {
            info: function (title, content) {
                toaster.pop('note', title, content, 5000);
            },

            success: function (title, content) {
                toaster.pop('success', title, content, 5000);
            },

            error: function (title, status, item) {
                toaster.pop('error', title, determineMessage(status, item), 5000);
            },
            
            customError: function (title, status, item, errorMessage) {
                if (status === 400) {
                    toaster.pop('error', title, errorMessage, 5000);
                } else if (status !== 400) {
                    toaster.pop('error', title, determineMessage(status, item), 5000);
                }
            },

            warning: function (title, status, item) {
                toaster.pop('warning', title, determineMessage(status), 5000);
            },
            
            customWarning: function (title, status, item, warningMessage) {
                if (status === 400) {
                    toaster.pop('warning', title, warningMessage, 5000);
                } else if (status !== 400) {
                    toaster.pop('warning', title, determineMessage(status, item), 5000);
                }
            }
        };

        return service;
    }])

    .factory('utils', function () {
        var naturalOrderNullsLastComparator = function (a, b) {
            if (a === null) {
                return 1;
            }
            if (b === null) {
                return -1;
            }
            if (a > b) {
                return 1;
            }
            if (a < b) {
                return -1;
            }
            return 0;
        };

        return {
            naturalOrderNullsLastComparator: naturalOrderNullsLastComparator
        };
    })
/**
 * Opens the terms of use dialog, either in none or model mode
 */
    .factory('termsOfUseDialog', ['$localStorage', 'ngDialog', function ($localStorage, ngDialog) {
    
        /**
         * Opens the modal dialog, ths user can only close by pressing accept.
         * 
         * This only opens if the cookie isnt present that signifies that they have already accepted the terms of use
         */
        var openModalIfCookieNotPresent = function() {
            if (_.isUndefined($localStorage.termsOfUseKey)) {
            
                ngDialog.openConfirm({
                    className: 'ngdialog-theme-default',
                    showClose: false,
                    closeByEscape: false,
                    closeByDocument: false,                    
                    template: '/views/about/TermsOfUse.html' 
                }).then(function (reason) {
                    console.log("Accepted terms of use, adding cookie");
                    $localStorage.termsOfUseKey = 'accepted';
                }, function () {
                    console.log("They clicked cancel");
                });
            }
        };
        
        /**
         * Opens the modal dialog, ths user can only close by pressing accept
         */
        var openNoneModal = function() {
            ngDialog.openConfirm({
                className: 'ngdialog-theme-default',
                showClose: true,
                closeByEscape: true,
                closeByDocument: true,                    
                template: '/views/about/TermsOfUse.html' 
            });
        };
        
        return {
            openModal: openModalIfCookieNotPresent,
            openNoneModal: openNoneModal
        };
    }]);
