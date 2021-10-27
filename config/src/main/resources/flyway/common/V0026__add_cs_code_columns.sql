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

ALTER TABLE acl_object ADD code VARCHAR(256) default NULL;
ALTER TABLE acl_object ADD parent_clinical_study_code VARCHAR(256) default NULL;
ALTER TABLE acl_object ADD acl_type VARCHAR(256) default NULL;

UPDATE acl_object SET code = name;
UPDATE acl_object SET parent_clinical_study_code = parent_clinical_study;

UPDATE acl_object SET acl_type = 'DS' WHERE parent_clinical_study IS NOT NULL AND parent_drug_programme IS NOT NULL;
UPDATE acl_object SET acl_type = 'CS' WHERE parent_clinical_study IS NULL AND parent_drug_programme IS NOT NULL;
UPDATE acl_object SET acl_type = 'DP' WHERE parent_clinical_study IS NULL AND parent_drug_programme IS NULL;
