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

CREATE OR REPLACE VIEW view_datasets AS
  SELECT aoi.OBJECT_ID_IDENTITY AS dataset_id,
    ao."NAME"                   AS dataset_name,
    ao.PARENT_CLINICAL_STUDY    AS clinical_study,
    ao.PARENT_DRUG_PROGRAMME    AS drug_programm,
    CASE
      WHEN ac.CLASS LIKE '%Detect%'
      THEN 'detect'
      WHEN ac.CLASS LIKE '%Acuity%'
      THEN 'acuity'
      ELSE 'unknown'
    END AS dataset_type
  FROM ACL_OBJECT ao
  INNER JOIN ACL_OBJECT_IDENTITY aoi
  ON ao.ACL_OBJECT_IDENTITY_ID = aoi.ID
  INNER JOIN acl_class ac
  ON ac.ID                        = aoi.OBJECT_ID_CLASS
  WHERE ao.PARENT_CLINICAL_STUDY IS NOT NULL
  AND ao.PARENT_DRUG_PROGRAMME   IS NOT NULL;

