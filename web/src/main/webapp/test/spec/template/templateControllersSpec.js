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

describe('Controller: templateController', function() {
    'use strict';

    // load the controller's module
    beforeEach(module('acuityApp'));

    describe('TemplateController', function() {
        var TemplateController, scope, mockTermsOfUseDialog;

        // Initialize the controller and a mock scope
        beforeEach(inject(function($controller, $rootScope, $location) {

            //mock SecurityModel
            var mockSecurityModel = {
                getUserId: function() { return  "user001";},
                getFullName: function() { return  "Glen d";}
            };
            mockTermsOfUseDialog = {
                openNoneModal: function() { }
            };
            
            spyOn(mockTermsOfUseDialog, 'openNoneModal');

            scope = $rootScope.$new();

            TemplateController = $controller('TemplateController', {
                $scope: scope, $location: $location, SecurityModel: mockSecurityModel, termsOfUseDialog: mockTermsOfUseDialog
            });
        }));


        it('should attach username and acls list to the scope', function() {
            expect(scope.userId).toBe("user001");
            expect(scope.fullName).toBe("Glen d");
        });
        
        it('should run terms of use dialog', function() {
            scope.showTermsOfUse();
            
            expect(scope.showTermsOfUse).toBeDefined();
            expect(mockTermsOfUseDialog.openNoneModal).toHaveBeenCalled();
        });
    });
});
