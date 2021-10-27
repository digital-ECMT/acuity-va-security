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

ALTER TABLE LOG_OPERATION ADD OBJECT_IDENTITY_CLASSNAME varchar(256);
ALTER TABLE LOG_OPERATION ADD OBJECT_IDENTITY_ID NUMBER(19);

update LOG_OPERATION set OBJECT_IDENTITY_ID = -1 WHERE OBJECT_IDENTITY_CLASSNAME IS NULL;
update LOG_OPERATION set OBJECT_IDENTITY_CLASSNAME = 'N/A' WHERE OBJECT_IDENTITY_CLASSNAME IS NULL;

ALTER TABLE LOG_OPERATION MODIFY OBJECT_IDENTITY_CLASSNAME varchar(256) DEFAULT 'N/A' NOT NULL;
ALTER TABLE LOG_OPERATION MODIFY OBJECT_IDENTITY_ID NUMBER(19) DEFAULT -1 NOT NULL;
