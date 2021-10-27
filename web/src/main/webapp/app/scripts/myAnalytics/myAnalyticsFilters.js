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

/* Filters */

angular.module('acuityApp.myAnalyticsFilters', [])

/**
 * Reduces an array of Datasets objects to a set of drug programmes
 *
 * @argument {AcuityObjectIdentity} any array to search by  (name attribute)
 * @argument {String} searchByValue  value of the search term, comma seperated by , (meaning or)
 */
    .filter('filterOr', ['AcuityAclObjectEnum', function (AcuityAclObjectEnum) {
        return function (array, searchByValue) {

            // simple checking that the value has been entered and is not undefine or empty
            if (searchByValue) {
                if (searchByValue.name.trim() === "") {
                    return array;
                }
                var searchArray = searchByValue.name.toLowerCase().split(" or ");
                searchArray = _.map(searchArray, function (item) {
                    return item.trim();
                });
                searchArray = _.without(searchArray, '');
                var filteredArray = [];

                _.forEach(array, function (arrayItem) {
                    _.every(searchArray, function (searchTerm) {
                        // if contains : then its a dataset search
                        if (!_.isUndefined(searchTerm) && searchTerm.trim() !== "" && searchTerm.trim().indexOf(':') >= 0) {
                            var arrayItemName = arrayItem.clinicalStudyName + ':' + arrayItem.name;
                            
                            if (searchTerm.trim() !== "" && arrayItemName.toLowerCase() === searchTerm.trim()) {
                                filteredArray.push(arrayItem);
                                return false; //    found and added, no need to check other search term so break every loop
                            } else {
                                return true;
                            }
                        } else if (arrayItem.supertype === AcuityAclObjectEnum.ClinicalStudy.superClassName ||
                                arrayItem.supertype === AcuityAclObjectEnum.AcuityDataset.superClassName) { // if cs or dataset, search drug too
                            if (searchTerm.trim() !== "" && (arrayItem.name.toLowerCase().indexOf(searchTerm.trim().toLowerCase()) >= 0) ||
                                arrayItem.drugProgramme.toLowerCase().indexOf(searchTerm.trim().toLowerCase()) >= 0) {

                                filteredArray.push(arrayItem);
                                return false; // found and added, no need to check other search term so break every loop
                            } else {
                                return true;
                            }
                        } else {
                            if (searchTerm.trim() !== "" && arrayItem.name.toLowerCase().indexOf(searchTerm.trim().toLowerCase()) >= 0) {
                                filteredArray.push(arrayItem);
                                return false; //    found and added, no need to check other search term so break every loop
                            } else {
                                return true;
                            }
                        }
                    });
                });

                return filteredArray;
            } else {
                return array;
            }
        };
    }])

/**
 * Escaping of / and \ does not work in angular routing, as it escapes to %2F which is rejected.
 *
 * So need to double escape the urls
 *
 * See http://stackoverflow.com/questions/16630912/angular-js-route-doesnt-match-component-with-2f-encoded
 * @returns {Function}
 */
    .filter('doubleEscape', function () {
        return function (input) {
            return encodeURIComponent(encodeURIComponent(input));
        };
    })
    .filter('doubleUnescape', function () {
        return function (input) {
            return decodeURIComponent(input);
        };
    })
/**
 * Reduces an array of Datasets objects to a set of drug programmes
 *
 * @argument {Dataset} arrayDatasets array of Datasets AcuityObjectIdentities
 */
    .filter('filterByDrugProgramme', function () {
        return function (arrayDatasets) {
            var drugs = _.chain(arrayDatasets)
                .groupBy(function (x) {
                    return x.drugProgramme;
                })
                .keys()
                .value();
            return drugs;
        };
    })

/**
 * Reduces an array of Datasets objects to a set of moduleTypes
 *
 * @argument {Dataset} arrayDatasets array of Datasets AcuityObjectIdentities
 */
    .filter('filterByModuleType', function () {
        return function (arrayDatasets) {
            var moduleTypes = _.chain(arrayDatasets)
                .groupBy(function (x) {
                    return x.moduleType;
                })
                .keys()
                .value();
            return moduleTypes;
        };
    })

/**
 * Filters an array of persons by a key nameAndId by a value.
 *
 * @argument {Person} arrayPersons array of persons
 * @argument {String} searchByTerm search value
 */
    .filter('filterBySearchTerm', function () {
        function removeAccents (value) {
            return value
                .replace(/á/g, 'a')
                .replace(/é/g, 'e')
                .replace(/í/g, 'i')
                .replace(/ó/g, 'o')
                .replace(/ú/g, 'u')
                .replace(/ń/g, 'n')
                .replace(/ö/g, 'o')
                .replace(/å/g, 'a');
        }

        return function (arrayPersons, searchByTerm) {
            return _.filter(arrayPersons, function (person) {
                if (searchByTerm.name) {
                    return (removeAccents(person.nameAndId.toLowerCase())).indexOf(removeAccents(searchByTerm.name.toLowerCase())) >= 0;
                } else {
                    return true; //if user hasnt put anything into the search box, dont filter
                }
            });
        };
    })
/**
 * Iterates of this, and returns an array of Persons filtering out only users with a certain permission mask
 *
 * [{"user":{"userId":"user001","fullName":"Bob B","authorities":[],"enabled":true},"permissionMask":480, granted: true},
 * {"user":{"userId":"User5","fullName":"User5 Fullname","authorities":[],"enabled":true},"permissionMask":480, granted: true},
 * {"user":{"userId":"User3","fullName":"User3 Fullname","authorities":[],"enabled":true},"permissionMask":224, granted: false}]
 */
    .filter('filterPermissionMask', ['Person', 'SecurityAuth', function (Person, SecurityAuth) {
        return function (arrayUsersWithPermission, permissionMask) {
            return _.chain(arrayUsersWithPermission).
                filter(function (userWithPermission) {
                    if (SecurityAuth.isOneOfRolesMasksExceptAuthorisedUser(userWithPermission.permissionMask)) { // permissionMask not one of auth user (3, 7, 11, 15)
                        return userWithPermission.permissionMask === permissionMask;
                    } else { // auth user (mask combination of PACKAGE ACCESS)                    
                        return SecurityAuth.checkPermission(permissionMask, userWithPermission.permissionMask);
                    }  
                }).
                map(function (userWithPermission) {
                    var person = new Person(userWithPermission.user.fullName, userWithPermission.user.userId, userWithPermission.granted,
                        undefined, userWithPermission.job, userWithPermission.user.group, userWithPermission.permissionMask);
                        
                    // viewPackagePermissions are only needed for authorised users. But can
                    // be determined by their 'role' permission
                    var viewPackagePermissions = SecurityAuth.getViewPackagePermissions(userWithPermission.permissionMask);  
                    person.viewPackagePermissions = viewPackagePermissions;
                    
                    return person;
                }).
                value();
        };
    }])
/**
 * Remove all the empty persons from the array
 * @param {type} Person
 * @returns {Function}
 */
    .filter('filterRemoveEmpty', ['Person', function (Person) {
        return function (arrayPersons) {
            return _.filter(arrayPersons, function (person) {
                    return !person.isEmpty();
                });
        };
    }])
/**
 * Custom filter for showing the display name of the Person by name, id or name and id showBy
 *
 * @argument {Person} person to be shown
 * @argument {String} showBy display type
 * @returns {String} value of
 */
    .filter('personDisplayName', [function () {
        return function (person, showBy) {
            if (!person) {
                return '';
            }

            if (person.group) {
                return 'GROUP: ' + person.name;
            }

            if (showBy === 'name') {
                return person.name;
            } else if (showBy === 'userId') {
                return person.userId;
            } else if (showBy === 'nameAndId') {
                return person.nameAndId;
            }
        };
    }])

/**
 * Swaps the status in the DB to more human readable status
 *
 * @argument {String} dbStatus either RED, AMBER, GREEN
 * @returns {string} either Fail, Warning, Success
 */
    .filter('dataUploadStatus', [function () {
        return function (dbStatus) {
            // change the name of the last status to something more readable
            if (dbStatus === 'RED') {
                return 'Fail';
            } else if (dbStatus === 'AMBER') {
                return 'Warning';
            } else if (dbStatus === 'GREEN') {
                return 'Success';
            } else {
                return 'Unknown';
            }
        };
    }])
/**
 * Either returns the full length of the array, or is filtered returns "filteredLength / allLength"
 *
 * @argument {number} filteredLength
 * @argument {allLength} allLength
 * @returns {string} if(filteredLength == allLength) return "allLength" otherwise return "filteredLength / allLength"
 */
    .filter('filterCounts', [function () {
        return function (filteredLength, allLength) {
            if (filteredLength === allLength) {
                return allLength;
            } else {
                return filteredLength + " /  " + allLength;
            }
        };
    }])
/**
 * Filters an array of persons that are present in another array of persons
 *
 * @argument {Person} arrayPerson array of persons
 * @argument {Person} arrayPersons already present
 */
    .filter('filterIfPresent', [function () {
        return function (users, items) {

            if (users.length === 0) {
                return users;
            }

            var filtered = [];

            for (var index = 0; index < users.length; index++) {
                var result = true;
                var user = users[index];
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.userId === user.userId) {
                        result = false;
                    }
                }
                if (result) {
                    filtered.push(user);
                }
            }

            return filtered;
        };
    }]);
