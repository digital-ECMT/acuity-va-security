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

insert into users (username, password, enabled) values ('user001', 'password', 1);
insert into users (username, password, enabled) values ('kfgt12354', 'password', 1);
insert into users (username, password, enabled) values ('ksdgh854', 'password', 0);
insert into users (username, password, enabled) values ('ksdf5465667', 'password', 1);

insert into authorities (username, authority) values ('user001', 'ROLE_USER_SINGLE');
insert into authorities (username, authority) values ('ksdf5465667', 'ROLE_USER');

insert into groups (id, group_name) values (1, 'Users');
insert into groups (id, group_name) values (2, 'Administrators');

insert into group_authorities (group_id, authority) values (1, 'ROLE_USER');
insert into group_authorities (group_id, authority) values (2, 'ROLE_ADMIN');
insert into group_authorities (group_id, authority) values (2, 'ROLE_USER');

insert into group_members (id, username, group_id) values (1, 'user001', 2);
insert into group_members (id, username, group_id) values (2, 'kfgt12354', 1);
insert into group_members (id, username, group_id) values (3, 'ksdgh854', 1);

