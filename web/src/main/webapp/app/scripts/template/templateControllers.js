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

angular.module('acuityApp.templateControllers')

/**
 * Sets the URL in scope, so that anchor URLs can be handled properly
 */
    .controller('TemplateController', ['$scope', '$location', 'SecurityModel', 'termsOfUseDialog',
        function ($scope, $location, SecurityModel, termsOfUseDialog) {
            $scope.getBaseUrl = function () {
                return $location.url();
            };

            $scope.userId = SecurityModel.getUserId();
            $scope.fullName = SecurityModel.getFullName();

            $scope.showTermsOfUse = function () {
                termsOfUseDialog.openNoneModal();
            };

            $scope.clearApplicationCache = function () {
                SecurityModel.clearApplicationCache();
            };
            $scope.isActive = function (viewLocation) {
                return $location.path().includes(viewLocation);
            };
        }]);
