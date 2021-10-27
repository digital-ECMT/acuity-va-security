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

-- authority that is allowed to modify Acls
insert into groups (id, group_name) values (1, 'DEVELOPMENT_GROUP');
insert into group_authorities (group_id, authority) values (1, 'DEVELOPMENT_TEAM');

-- authority that has access to all acls
insert into groups (id, group_name) values (2, 'ACL_ADMINISTRATOR_GROUP');
insert into group_authorities (group_id, authority) values (2, 'ACL_ADMINISTRATOR');

-- authority that shows the user has taken the training course
insert into groups (id, group_name) values (3, 'TRAINED_USER_GROUP');
insert into group_authorities (group_id, authority) values (3, 'TRAINED_USER');
