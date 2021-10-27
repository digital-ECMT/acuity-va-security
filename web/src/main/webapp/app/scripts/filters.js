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

/* Common Filters */

angular.module('acuityApp.filters', [])
        /**
         * Remove ACUITY module type form module name
         * Example:
         * moduleName = ACUITY_Safety_ASD1234
         * moduleType = Safety
         * return ASD1234
         */
        .filter('cleanModuleName', function () {
            /**
             * Remove ACUITY module type form module name
             * @param {String} moduleName - module name, ie ACUITY_Safety_STDY4321_Dummy_Instance
             * @returns {*} module name without info about module type
             */
            return function (moduleName) {
                if (angular.isString(moduleName)) {
                    var match = moduleName.match(/^ACUITY_[A-Za-z]+_(.+)$/);
                    if (match) {
                        return match[1];
                    }
                }
                return moduleName;
            };
        })

        .filter('cleanDatasetAndAppendType', ['AcuityAclObjectEnumService', function (AcuityAclObjectEnumService) {
            /**
             * Remove ACUITY module type form module name and add ongoing to end of study
             * @param {Object} dataset - roi object
             * @returns {*} module name without info about module type
             */
            return function (dataset) {

                var type = '';
                if (AcuityAclObjectEnumService.isAcuityType(dataset)) { // acuity
                    type = '(Ongoing)';
                } else if (AcuityAclObjectEnumService.isDetectType(dataset)) { //detect
                    type = '(End of study)';
                }
                var fullName = dataset.clinicalStudyName + ': ' + dataset.name + ' ' + type;

                var match = fullName.match(/^ACUITY_[A-Za-z]+_(.+)$/);
                if (match) {
                    return match[1];
                }
                return fullName;
            };
        }])
    
        .filter('appendType', ['AcuityAclObjectEnumService', function (AcuityAclObjectEnumService) {
            /**
             * Remove adds ongoing to end of study
             * @param {Object} dpOrStudy dp or study - roi object
             * @returns {*} module name without info about module type
             */
            return function (dpOrStudy) {

                return dpOrStudy.name;
            };
        }])

        .filter('cleanDatasetNameAndAppendType', function () {
            /**
             * Remove ACUITY module type form module name and add ongoing to end of study
             * @param {Object} dataset - roi object
             * @returns {*} module name without info about module type
             */
            return function (dataset) {

                var type = '(End of study)';
                if (dataset.type === 'com.acuity.va.security.acl.domain.DetectDataset') {
                    type = '(End of study)';
                } else if (dataset.type === 'com.acuity.va.security.acl.domain.AcuityDataset') {
                    type = '(Ongoing)';
                }
                var fullName = dataset.name + ' ' + type;

                var match = fullName.match(/^ACUITY_[A-Za-z]+_(.+)$/);
                if (match) {
                    return match[1];
                }
                return fullName;
            };
        })

        /*
         Convert time from minutes to the XXh.YY min representation
         */
        .filter('timeFilter', function () {
            return function (value) {
                var h = parseInt(value / 60);
                var m = parseInt(value % 60);

                var hStr = (h >= 10) ? h : "0" + h;
                var mStr = (m >= 10) ? m : "0" + m;
                var glue = (hStr && mStr) ? ':' : '';

                return hStr + glue + mStr;
            };
        })

        /*
         Convert time from server days.hours to client representation
         Example:from 5.5 to 5.12 (5 days 12 hours)
         */
        .filter('daysHoursFilter', function () {
            return function (value) {
                var days = parseInt(value);
                var hours = Math.abs(Math.round((value - days) * 24));
                return days + "." + hours;
            };
        });
