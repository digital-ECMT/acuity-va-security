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

DELETE FROM acl_class WHERE class = 'com.acuity.va.security.acl.domain.AcuityDrugProgramme' or class = 'com.acuity.va.security.acl.domain.AcuityClinicalStudy';

UPDATE acl_class SET class = 'com.acuity.va.security.acl.domain.DrugProgramme' WHERE class = 'com.acuity.va.security.acl.domain.DetectDrugProgramme';
UPDATE acl_class SET class = 'com.acuity.va.security.acl.domain.ClinicalStudy' WHERE class = 'com.acuity.va.security.acl.domain.DetectClinicalStudy';

UPDATE acl_class SET acl_remote_id = (select id from ACL_REMOTE where lower(name) = 'detect') WHERE class = 'com.acuity.va.security.acl.domain.AcuityDataset';
