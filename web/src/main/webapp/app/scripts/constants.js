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

angular.module('acuityApp.constants', [])
    .constant('AcuityAclObjectEnum', {
        'DrugProgramme': {
            name: 'DrugProgramme',
            className: 'com.acuity.va.security.acl.domain.DrugProgramme',
            superClassName: 'com.acuity.va.security.acl.domain.DrugProgramme'
        },
        'ClinicalStudy': {
            name: 'ClinicalStudy',
            className: 'com.acuity.va.security.acl.domain.ClinicalStudy',
            superClassName: 'com.acuity.va.security.acl.domain.ClinicalStudy'
        },
        'AcuityDataset': {
            name: 'AcuityDataset',
            className: 'com.acuity.va.security.acl.domain.AcuityDataset',
            superClassName: 'com.acuity.va.security.acl.domain.Dataset'
        },        
        'DetectDataset': {
            name: 'DetectDataset',
            className: 'com.acuity.va.security.acl.domain.DetectDataset',
            superClassName: 'com.acuity.va.security.acl.domain.Dataset'
        }
    })
    .factory('AcuityAclObjectEnumService', ['AcuityAclObjectEnum', function (AcuityAclObjectEnum) {

        var service = {
            /**
             * Returns whether an AcuityAclObject is of acuity type
             *
             * @param {object} acuityAclObject ie object
             * @returns {boolean}
             */
            isAcuityType: function (acuityAclObject) {
                if (acuityAclObject.type === AcuityAclObjectEnum.AcuityDataset.className) {
                    return true;
                } else {
                    return false;
                }
            },
            
            isDetectType: function (acuityAclObject) {
                if (acuityAclObject.type === AcuityAclObjectEnum.DetectDataset.className) {
                    return true;
                } else {
                    return false;
                }
            },
            /**
             * Returns an AcuityAclObjectEnum from a given string, ie returns AcuityAclObjectEnum.drugProgramme
             * if the acuityAclObjectString is 'rugProgramme'
             *
             * @param {string} acuityAclObjectString ie 'DrugProgramme'
             * @returns {AcuityAclObjectEnum}
             */
            get: function (acuityAclObjectString) {
                if (acuityAclObjectString === AcuityAclObjectEnum.DrugProgramme.name) {
                    return AcuityAclObjectEnum.DrugProgramme;
                } else if (acuityAclObjectString === AcuityAclObjectEnum.ClinicalStudy.name) {
                    return AcuityAclObjectEnum.ClinicalStudy;
                } else if (acuityAclObjectString === AcuityAclObjectEnum.AcuityDataset.name) {
                    return AcuityAclObjectEnum.AcuityDataset;
                } else if (acuityAclObjectString === AcuityAclObjectEnum.DetectDataset.name) {
                    return AcuityAclObjectEnum.DetectDataset;
                }
            },
            /**
             * Returns an AcuityAclObjectEnum from a given string, ie returns AcuityAclObjectEnum.RrugProgramme
             * if the acuityAclObjectString is 'com.acuity.acuity.security.acl.domain.DrugProgramme'
             *
             * @param {string} acuityAclObjectClassString ie 'DrugProgramme'
             * @returns {*}
             */
            getFromClass: function (acuityAclObjectClassString) {
                if (acuityAclObjectClassString === AcuityAclObjectEnum.DrugProgramme.className) {
                    return AcuityAclObjectEnum.DrugProgramme;
                } else if (acuityAclObjectClassString === AcuityAclObjectEnum.ClinicalStudy.className) {
                    return AcuityAclObjectEnum.ClinicalStudy;
                } else if (acuityAclObjectClassString === AcuityAclObjectEnum.AcuityDataset.className) {
                    return AcuityAclObjectEnum.AcuityDataset;
                } else if (acuityAclObjectClassString === AcuityAclObjectEnum.DetectDataset.className) {
                    return AcuityAclObjectEnum.DetectDataset;
                }
            }
        };

        return service;
    }]);
