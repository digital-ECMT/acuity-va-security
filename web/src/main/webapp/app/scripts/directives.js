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

/* Common Directives */

angular.module('acuityApp.directives', [])

   .directive('setfocus', function () {
       return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                if (attrs.setfocus === 'true') {
                    element[0].focus();
                }
            }
        };
    })
    .directive('validateDate', function () {
        return {
            require: 'ngModel',
            link: function (scope, elem, attr, ngModel) {
                function parseDate(dateStr) {
                    if (angular.isUndefined(dateStr) || !dateStr) {
                        return null;
                    }

                    if (angular.isDate(dateStr)) {
                        return dateStr;
                    }

                    dateStr = dateStr.toString().toLowerCase();
                    var re = /^(0[1-9]|[12][0-9]|3[01])-(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)-(19|20)\d\d$/;
                    var m = re.test(dateStr);
                    if (!m) {
                        return null;
                    }

                    var months = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];
                    var dArray = dateStr.split('-');
                    var d = new Date(dArray[2], months.indexOf(dArray[1]), dArray[0]);
                    return d;
                }

                function validate(value) {

                    if (angular.isDate(value)) {
                        ngModel.$setValidity('validDate', true);
                        return;
                    }

                    var d = parseDate(value);

                    // it is a date
                    if (!angular.isDate(d)) { // d.valueOf() could also work
                        ngModel.$setValidity('validDate', false);
                    } else {
                        ngModel.$setValidity('validDate', true);
                        return d;
                    }
                }

                scope.$watch(function () {
                    return ngModel.$viewValue;
                }, validate);
            }
        };
    })

    .directive('ngRightClick', function ($parse) {
        return function (scope, element, attrs) {
            var fn = $parse(attrs.ngRightClick);
            element.bind('contextmenu', function (event) {
                scope.$apply(function () {
                    event.preventDefault();
                    fn(scope, {$event: event});
                });
            });
        };
    });
//
//    .directive('tooltip', function () {
//        return {
//            restrict: 'A',
//            link: function (scope, element, attrs) {
//                $(element).hover(function () {
//                    // on mouseenter
//                    $(element).tooltip('show');
//                }, function () {
//                    // on mouseleave
//                    $(element).tooltip('hide');
//                });
//            }
//        };
//    });
