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

/**
 * This controller extends the template, and provides universal UI component controls for all modules.
 * This functionality has been taken out of it's old place in SafetyController while creating the Tolerability module.
 */
angular.module('acuityApp.templateControllers')
    .controller('moduleTemplateController', ['$scope', '$timeout',
        function($scope, $timeout) {
            /**
             * Collection of unbind watcher functions
             * @type {Array}
             */
            var unbinds = [];
            /**
             * the initial width of the sidebar
             * @type {number}
             */
            var defaultSidebarWidth = 300;

            /**
             * below this width, the sidebar closes and is reset to default width (watch method below)
             * @type {number}
             */
            var autoCloseSidebarWidth = 100;

            /**
             * default height for the bottom bar
             * @type {number}
             */
            var defaultBottomBarHeight = 180;

            /**
             * Represents common safety UI settings shared between views,
             * like panel states and positions
             */
            $scope.UI = {
                showSidebar: true,
                showBottomBar: true,
                previewSidebar: false,
                timerRunning: false,
                timerCancelled: false,
                sidebarPosition: defaultSidebarWidth,
                maxSidebarWidth: defaultSidebarWidth,
                minSidebarWidth: 37,
                smSidebarWidth: 440,
                mdSidebarWidth: 640,
                lgSidebarWidth: 840,
                bottombarPosition: defaultBottomBarHeight,
                maxBottombarHeight: defaultBottomBarHeight,
                minBottombarHeight: 28
            };

            /**
             * Show sidebar
             */
            $scope.showSidebar = function () {
                if (!$scope.UI.showSidebar) {
                    $scope.UI.showSidebar = true;
                }
            };

            /**
             * Hide sidebar
             */
            $scope.hideSidebar = function () {
                if ($scope.UI.showSidebar) {
                    $scope.UI.showSidebar = false;
                    $scope.UI.maxSidebarWidth = $scope.UI.sidebarPosition;
                }
            };

            /**
             * Mouse enter function: if user waits for the timer without cancelling, opens sidebar.
             * mouse leave or mouse down events cancels timer.
             * NB: if the timer delay is reduced to 500ms or below, then the open animation is too slow
             *      to respond, causing the mouseleave event to be fired before the sidebar is fully open.
             *      THIS CAUSES A SITUATION WHERE THE SIDEBAR CANNOT BE OPENED.
             *
             * NB: This functionality has been temporarily removed from the page: see RCT-3564 for info.
             */
            $scope.openPreview = function () {
                $scope.UI.timerCancelled = false;
                $scope.UI.timerRunning = true;
                $scope.previewTimer = $timeout(function () {
                    if (!$scope.UI.showSidebar && !$scope.UI.previewSidebar) {
                        $scope.UI.previewSidebar = true;
                        $scope.showSidebar();
                    }
                    $scope.UI.timerRunning = false;
                }, 600);
            };

            /**
             * mouse leave event: cancels preview timer (if running). This stops the sidebar opening
             *      if the mouse leaves the sidebar before the timer has completed.
             * if sidebar is opened as a preview, closes sidebar.
             *
             * NB: This functionality has been temporarily removed from the page: see RCT-3564 for info.
             */
            $scope.closePreview = function () {
                $scope.cancelPreview();

                if ($scope.UI.previewSidebar) {
                    $scope.UI.previewSidebar = false;
                    if ($scope.UI.showSidebar) {
                        $scope.hideSidebar();
                    }
                }
            };

            /**
             * Cancels preview timer, if it exists
             * This method is bound to the mousedown event in the filter panel, as sometimes
             *      the mouseleave event is not called when the sidebar collapses. Binding here
             *      stops the preview appearing unsolicited when the sidebar is closed.
             *
             * NB: This functionality has been temporarily removed from the page: see RCT-3564 for info.
             */
            $scope.cancelPreview = function () {
                if ($scope.UI.timerRunning) {
                    $scope.UI.timerCancelled = $timeout.cancel($scope.previewTimer);
                    if ($scope.UI.timerCancelled) {
                        $scope.UI.timerRunning = false;
                    }
                }
            };

            /**
             * This method is only used for testing the sidebar size recognition methods
             * @param {number} newSize the new size for the sidebar.
             */
            $scope.setSidebarSize = function (newSize) {
                $scope.UI.sidebarPosition = newSize;
            };

            /**
             * used as an ng-class conditional to change number of filters in a line
             * @returns {boolean} true if small size, false otherwise
             */
            $scope.isSmSidebar = function () {
                return $scope.UI.sidebarPosition <= $scope.UI.smSidebarWidth && $scope.UI.sidebarPosition > $scope.UI.minSidebarWidth;
            };

            /**
             * used as an ng-class conditional to change number of filters in a line
             * @returns {boolean} true if medium size, false otherwise
             */
            $scope.isMdSidebar = function () {
                return $scope.UI.sidebarPosition <= $scope.UI.mdSidebarWidth && $scope.UI.sidebarPosition > $scope.UI.smSidebarWidth;
            };

            /**
             * used as an ng-class conditional to change number of filters in a line
             * @returns {boolean} true if large size, false otherwise
             */
            $scope.isLgSidebar = function () {
                return $scope.UI.sidebarPosition <= $scope.UI.lgSidebarWidth && $scope.UI.sidebarPosition > $scope.UI.mdSidebarWidth;
            };

            /**
             * used as an ng-class conditional to change number of filters in a line
             * @returns {boolean} true if above large size, false otherwise
             */
            $scope.isXlSidebar = function () {
                return $scope.UI.sidebarPosition > $scope.UI.lgSidebarWidth;
            };

            /**
             * If the sidebar is ever reduced to less than the autoclose width (set above),
             *      this hides the sidebar and resets max width to the initial width variable.
             */
            unbinds.push($scope.$watch("UI.sidebarPosition", function () {
                if ($scope.UI.sidebarPosition < autoCloseSidebarWidth) {
                    $scope.hideSidebar();
                    $scope.UI.maxSidebarWidth = defaultSidebarWidth;
                }
            }));

            /*
             * Store bottom bar position when display property is changed
             */
            unbinds.push($scope.$watch("UI.showBottomBar", function (newValue, oldValue) {
                if (newValue !== oldValue && !newValue) {
                    $scope.UI.maxBottombarHeight = $scope.UI.bottombarPosition;
                }
            }));

            unbinds.push($scope.$on('$stateChangeStart', function () {
                if ($scope.UI.showBottomBar) {
                    $scope.UI.maxBottombarHeight = $scope.UI.bottombarPosition;
                }
            }));

            $scope.$on('$destroy', function () {
                _.each(unbinds, function (unbind) {
                    unbind();
                });
            });
        }]);
