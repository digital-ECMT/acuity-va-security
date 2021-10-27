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

/* Directives */

angular.module('acuityApp.securityDirectives', [])
        /**
         * Allows hiding or disabling of elements depending if they have the correct permission on an AcuityObjectIdentity
         *
         * 1) <a act-allowed="EDIT_DRUG_PROGRAMME" act-oi="currentDrugProgramme" href="/... />
         *
         * Tests whether user has EDIT_DRUG_PROGRAMME permission on {{currentDrugProgramme}}.  If not it hides it (default)
         *
         * 1) <div act-allowed="128" act-oi="currentDrugProgramme" act:hide="false" href="/... />
         *
         * Tests whether user has EDIT_ROLES (bitmask 128) permission on {{currentDrugProgramme}}.  If not it disables it
         *
         * @param {type} SecurityAuth
         * @returns {_L9.Anonym$2}
         */
        .directive('actAllowed', ['SecurityAuth', function (SecurityAuth) {
                return {
                    restrict: 'A',
                    scope: {
                        acuityObjectIdentity: '=actOi',
                        hide: '@actHide' //do we hide the element or disable it
                    },
                    /**
                     * Hides the element if the user hasnt got permission
                     *
                     * @param {type} scope  The div scope
                     * @param {type} elem   The container div element
                     * @param {type} attr   The div attributes
                     */
                    link: function (scope, elem, attr) {
                        console.log("linked");
                        if (_.isUndefined(scope.hide)) {
                            scope.hide = 'true';
                        }

                        if (_.isUndefined(scope.acuityObjectIdentity) || !SecurityAuth.hasPermission(elem.attr("act-allowed"), scope.acuityObjectIdentity)) {
                            if (scope.hide === 'true') {
                                elem.hide();
                            } else {
                                elem.attr("disabled", true);
                            }
                        }
                    }
                };
            }
        ])
        /**
         * Allows hiding or disabling of elements depending if they are acuity types on an AcuityObjectIdentity
         * @param {type} AcuityAclObjectEnumService
         * @returns {_L9.Anonym$2}
         */
        .directive('actIsAcuityType', ['AcuityAclObjectEnumService', function (AcuityAclObjectEnumService) {
                return {
                    restrict: 'A',
                    scope: {
                        acuityObjectIdentity: '=actOi',
                        hide: '@actHide' //do we hide the element or disable it
                    },
                    /**
                     * Hides the element if the user hasnt got permission
                     *
                     * @param {type} scope  The div scope
                     * @param {type} elem   The container div element
                     * @param {type} attr   The div attributes
                     */
                    link: function (scope, elem, attr) {
                        if (_.isUndefined(scope.hide)) {
                            scope.hide = 'true';
                        }

                        if (!AcuityAclObjectEnumService.isAcuityType(scope.acuityObjectIdentity)) {
                            if (scope.hide === 'true') {
                                elem.hide();
                            } else {
                                elem.attr("disabled", true);
                            }
                        }
                    }
                };
            }
        ])
        /**
         * Allows hiding or diabling of elements depending if they have any permission greater than or equal to
         * administrator on any of the Acls in the system
         *
         * 1) <a act-allowed-admin-or-above href="/... />
         *
         * Tests whether user any permission of administrator or above.  If not it hides it (default)
         *
         * 1) <div act-allowed-admin-or-above act:hide="false" href="/... />
         *
         * Tests whether user any permission of administrator or above.  If not it disables it
         *
         * @param {type} SecurityAuth
         * @returns {_L9.Anonym$2}
         */
        .directive('actAllowedAdminOrAbove', ['SecurityAuth', 'SecurityModel', function (SecurityAuth, SecurityModel) {
                return {
                    restrict: 'A',
                    scope: {
                        //acuityObjectIdentities: '=actOis',
                        hide: '@actHide' //do we hide the element or disable it
                    },
                    /**
                     * Hides the element if the user hasnt got permission
                     *
                     * @param {type} scope  The div scope
                     * @param {type} elem   The container div element
                     * @param {type} attr   The div attributes
                     */
                    link: function (scope, elem, attr) {
                        if (_.isUndefined(scope.hide)) {
                            scope.hide = 'true';
                        }

                        if (!SecurityAuth.hasAnyPermissionAdministratorOrAbove(SecurityModel.getAcls())) {
                            if (scope.hide === 'true') {
                                elem.hide();
                            } else {
                                elem.addClass("not-allowed");
                            }
                        }
                    }
                };
            }])

        /**
         * Allows hiding or diabling of elements depending if they have any permission developer on any of the Acls in the system
         *
         * 1) <a act-allowed-developer href="/... />
         *
         * Tests whether user any permission of developer.  If not it hides it (default)
         *
         * 1) <div act-allowed-developer act:hide="false" href="/... />
         *
         * Tests whether user any permission of developer.  If not it disables it
         *
         * @param {type} SecurityAuth
         * @returns {_L9.Anonym$2}
         */
        .directive('actAllowedDeveloper', ['SecurityAuth', 'SecurityModel', function (SecurityAuth, SecurityModel) {
                return {
                    restrict: 'A',
                    scope: {
                        //acuityObjectIdentities: '=actOis',
                        hide: '@actHide' //do we hide the element or disable it
                    },
                    /**
                     * Hides the element if the user hasnt got permission
                     *
                     * @param {type} scope  The div scope
                     * @param {type} elem   The container div element
                     * @param {type} attr   The div attributes
                     */
                    link: function (scope, elem, attr) {
                        if (_.isUndefined(scope.hide)) {
                            scope.hide = 'true';
                        }

                        if (!SecurityAuth.hasAnyPermissionDeveloper(SecurityModel.getAcls())) {
                            if (scope.hide === 'true') {
                                elem.hide();
                            } else {
                                elem.attr("disabled", true);
                            }
                        }
                    }
                };
            }])

        /**
         * Allows hiding or diabling of elements depending if they have any permission trained user on any of the Acls in the system
         *
         * 1) <a act-trained-user href="/... />
         *
         * Tests whether user any permission of trained user.  If not it hides it (default)
         *
         * 1) <div act-trained-user act:hide="false" href="/... />
         *
         * Tests whether user any permission of trained user.  If not it disables it
         *
         * @returns {_L9.Anonym$2}
         */
        .directive('actTrainedUser', function (SecurityModel) {
            return {
                restrict: 'A',
                scope: {
                    //acuityObjectIdentities: '=actOis',
                    hide: '@actHide' //do we hide the element or disable it
                },
                /**
                 * Hides the element if the user hasnt got permission
                 *
                 * @param {type} scope  The div scope
                 * @param {type} elem   The container div element
                 * @param {type} attr   The div attributes
                 */
                link: function (scope, elem, attr) {
                    if (_.isUndefined(scope.hide)) {
                        scope.hide = 'true';
                    }
                    var isTrainedUser = SecurityModel.getAcls().length !== 0 && _.findIndex(SecurityModel.getAuthorities(), function (authority) {
                        return authority === "TRAINED_USER";
                    }) !== -1;
                    if (!isTrainedUser) {
                        if (scope.hide === 'true') {
                            elem.hide();
                        } else {
                            elem.attr("disabled", true);
                        }
                    }
                }
            };
        });
