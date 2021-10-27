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

describe('GIVEN directives: ', function () {
    'use strict';
    var controller;
    var $scope;
    var form;

    beforeEach(module('acuityApp'));

    describe('validateDate', function() {

        beforeEach(inject(function($compile, $rootScope) {
            $scope = $rootScope;
            var element = angular.element(
                    '<form name="form">' +
                    '<input ng-model="model.d" name="d" validate-date type="text" />' +
                    '</form>'
            );
            $scope.model = { d: null };
            $compile(element)($scope);
            $scope.$digest();
            form = $scope.form;
        }));

        it('should pass with date', function() {
            form.d.$setViewValue('11-Aug-2014');
            $scope.$digest();
            expect(form.d.$valid).toBe(true);
        });
        it('should not pass with date', function() {
            form.d.$setViewValue('a');
            $scope.$digest();
            expect(form.d.$valid).toBe(false);
        });
    });
});
