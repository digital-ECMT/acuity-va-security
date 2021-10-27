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

describe('GIVEN filters: ', function () {
    'use strict';
    var $filter;

    beforeEach(module('acuityApp'));

    beforeEach(inject(function($compile, $rootScope) {
        inject(function (_$filter_) {
            $filter = _$filter_;
        });
    }));

    describe('GIVEN cleanModuleName filter:', function() {

        describe('WHEN pass undefined module name', function() {
            it('THEN result equals module name', function() {
                var moduleName;
                var result = $filter('cleanModuleName')(moduleName);

                expect(result).toBeUndefined();
            });
        });

        describe('WHEN pass empty module name', function() {
            it('THEN result equals module name', function() {
                var moduleName = '';
                var result = $filter('cleanModuleName')(moduleName);

                expect(result).toBe(moduleName);
            });
        });

        describe('WHEN pass module type that is part of module name', function() {
            it('THEN result equals module name without module type', function() {
                var moduleName = 'ACUITY_Safety_ASD1234';
                var expectedResult = 'ASD1234';

                var result = $filter('cleanModuleName')(moduleName);

                expect(result).toBe(expectedResult);
            });
        });
    });

});
