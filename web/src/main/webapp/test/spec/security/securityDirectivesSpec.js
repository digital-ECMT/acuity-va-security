
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

describe('Directive: securityDirective', function () {

    'use strict';
    beforeEach(module('acuityApp.securityServices'));
    beforeEach(module('acuityApp.securityDirectives'));

    var elm, scope;

    function compileDirective(tpl) {
        // inject allows you to use AngularJS dependency injection
        // to retrieve and use other services
        inject(function ($compile) {
            elm = $compile(tpl)(scope);
        });
        
        console.log(elm);
        // $digest is necessary to finalize the directive generation
        scope.$digest();
    }

    describe('rctAllowed directive', function () {

        var mockAcl = {
            type: "com.acuity.acuity.security.acl.domain.AcuityDataset", id: 130, name: "ACUITY_Safety_STDY4321_Dummy_Instance",
            rolePermissionMask: 3, isOpen: false, drugProgramme: "STDY4321", moduleType: "Safety"
        };

        beforeEach(inject(function ($rootScope, $compile) {
            scope = $rootScope.$new();
            scope.currentDrugProgramme = mockAcl;
        }));

        it('should set as disabled if not got access', inject(function (SecurityAuth) {

            compileDirective('<div rct-allowed="EDIT_DRUG_PROGRAMME" rct:hide="false" rct-oi="currentDrugProgramme" />');

            expect(elm.attr("disabled")).toBe("disabled");
        }));

        it('should set as enabled if got access string mask', inject(function (SecurityAuth) {

            compileDirective('<div rct-allowed="VIEW_VISUALISATIONS" rct:hide="false" rct-oi="currentDrugProgramme" />');

            expect(elm.attr("disabled")).toBeUndefined();
        }));

        it('should set as enabled if got access by integer mask', inject(function (SecurityAuth) {

            compileDirective('<div rct-allowed="3" rct:hide="false" rct-oi="currentDrugProgramme" />');

            expect(elm.attr("disabled")).toBeUndefined();
        }));

        it('should set as disable if currentDrugProgramme undefined', inject(function (SecurityAuth) {

            scope.currentDrugProgramme = undefined;

            compileDirective('<div rct-allowed="3" rct:hide="false" rct-oi="currentDrugProgramme" />');

            expect(elm.attr("disabled")).toBeDefined();
        }));
    });

    describe('rctAllowedAdminOrAbove directive', function () {

        var aclResponseAsAdmin = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 32783},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aclResponseNoneAdmin = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aclResponseNoneDrugOwner = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 522240}
        ];

        it('should set as disable if acls has no admin', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseNoneAdmin;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-admin-or-above rct:hide="false" />');

                expect(elm.attr("disabled")).toBeDefined();
            });
        });

        it('should set as enabled if acls has admin', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseAsAdmin;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-admin-or-above rct:hide="false" />');

                expect(elm.attr("disabled")).toBeUndefined();
            });
        });

        it('should set as enabled if acls is data owner', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseNoneDrugOwner;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-admin-or-above rct:hide="false" />');

                expect(elm.attr("disabled")).toBeUndefined();
            });
        });
    });
    describe('rctAllowedDeveloper directive', function () {

        var aclResponseAsDataOwner = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 450575},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aclResponseNoneAdmin = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 3}
        ];

        var aclResponseDeveloper = [
            {type: "com.acuity.acuity.security.acl.domain.DrugProgramme", id: 1, name: "Drug B", rolePermissionMask: 3},
            {type: "com.acuity.acuity.security.acl.domain.ClinicalStudy", id: 7, name: "Clinical Study C", rolePermissionMask: 522240}
        ];

        it('should set as disable if acls has no admin', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseNoneAdmin;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-developer rct:hide="false" />');

                expect(elm.attr("disabled")).toBeDefined();
            });
        });

        it('should set as enabled if acls has developer', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseDeveloper;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-developer rct:hide="false" />');

                expect(elm.attr("disabled")).toBeUndefined();
            });
        });

        it('should set as disable if acls is data owner', function () {
            module(function ($provide) {
                $provide.factory('SecurityModel', function () {
                    return {getAcls: function () {
                        return aclResponseAsDataOwner;
                    }};
                });
            });

            inject(function (SecurityModel, SecurityAuth) {

                compileDirective('<div rct-allowed-developer rct:hide="false" />');

                expect(elm.attr("disabled")).toBeDefined();
            });
        });
    });
});
