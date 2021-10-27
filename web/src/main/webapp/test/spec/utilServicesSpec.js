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

describe('GIVEN UtilServices: ', function () {
    'use strict';

    beforeEach(module('acuityApp'));
    beforeEach(module('acuityApp.utilServices'));

    describe('utils', function () {

        it('should sort items by natural order with nulls last', inject(function (utils) {
            var array;

            array = [-1, null, 1];
            array.sort(utils.naturalOrderNullsLastComparator);
            expect(_.isEqual(array, [-1, 1, null])).toBe(true);

            array = [1, null, -1];
            array.sort(utils.naturalOrderNullsLastComparator);
            expect(_.isEqual(array, [-1, 1, null])).toBe(true);

            array = [null, -1, 1];
            array.sort(utils.naturalOrderNullsLastComparator);
            expect(_.isEqual(array, [-1, 1, null])).toBe(true);

            array = [-1, 1, null];
            array.sort(utils.naturalOrderNullsLastComparator);
            expect(_.isEqual(array, [-1, 1, null])).toBe(true);

            array = [null, null, 1];
            array.sort(utils.naturalOrderNullsLastComparator);
            expect(_.isEqual(array, [1, null, null])).toBe(true);
        }));
    });

    describe('termsOfUseDialog', function () {
        var termsOfUseDialog;
        var mockNgDialog = {
            openConfirm: function () {
            }
        };

        beforeEach(function () {

            module(function ($provide) {
                $provide.factory('ngDialog', function () {
                    return mockNgDialog;
                });
            });
            inject(function (_termsOfUseDialog_, $q) {
                termsOfUseDialog = _termsOfUseDialog_;
                spyOn(mockNgDialog, 'openConfirm').andCallFake(function () {
                    var deferred = $q.defer();
                    deferred.resolve('Remote call result');
                    return deferred.promise;
                });
            });
        });

        it('should call show terms of use dialog when no local storage', function () {
            termsOfUseDialog.openModal();

            expect(mockNgDialog.openConfirm).toHaveBeenCalled();
        });

        it('should not call show terms of use dialog when no local storage', inject(function ($localStorage) {
            $localStorage.termsOfUseKey = "accepted";

            termsOfUseDialog.openModal();

            expect(mockNgDialog.openConfirm).not.toHaveBeenCalled();
        }));
        
        it('should always call show terms of use dialog for none modal', inject(function ($localStorage) {
            $localStorage.termsOfUseKey = "accepted";

            termsOfUseDialog.openNoneModal();

            expect(mockNgDialog.openConfirm).toHaveBeenCalled();
        }));
    });
});
