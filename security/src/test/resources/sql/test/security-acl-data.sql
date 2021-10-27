-- class
insert into acl_class (id, class) values (10, 'com.acuity.va.security.acl.domain.DrugProgramme');
insert into acl_class (id, class) values (11, 'com.acuity.va.security.acl.domain.ClinicalStudy');
insert into acl_class (id, class) values (12, 'com.acuity.va.security.acl.domain.Visualisation');

-- object - external db mapping
--insert into acl_object (id, name, external_id) values (2, 'Drug A', 100);
--insert into acl_object (id, name, external_id) values (3, 'Drug B', 101);
--insert into acl_object (id, name, external_id) values (4, 'Study 1', 102);
--insert into acl_object (id, name, external_id) values (5, 'Study 2', 103);
--insert into acl_object (id, name, external_id) values (6, 'Study 3', 104);
--insert into acl_object (id, name, external_id) values (7, 'Instance 1', 105);
--insert into acl_object (id, name, external_id) values (8, 'Instance 2', 106); -- Study1 Parent
--insert into acl_object (id, name, external_id) values (9, 'Instance 2', 106); -- Study2 Parent
--insert into acl_object (id, name, external_id) values (10, 'Instance 3', 108);

-- SIDs
insert into acl_sid (id, principal, sid) values (20, 1, 'User1');
insert into acl_sid (id, principal, sid) values (21, 1, 'User2');
insert into acl_sid (id, principal, sid) values (22, 1, 'User3');
insert into acl_sid (id, principal, sid) values (23, 1, 'User4');
insert into acl_sid (id, principal, sid) values (24, 1, 'User5');

--insert into acl_sid (id, principal, sid) values (21, 0, 'ROLE_USER');
--insert into acl_sid (id, principal, sid) values (22, 0, 'ROLE_ADMIN');

-- object identity

-- Drug Programs
insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (30, 2, 10, null, 20, 0); -- Drug A

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (31, 3, 10, null, 20, 0); -- Drug B

-- Clinical Studies
insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (32, 4, 11, 30, 20, 1); -- Study 1 parent is Drug A

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (33, 5, 11, 30, 20, 1); -- Study 2 parent is Drug A

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (34, 6, 11, 31, 20, 1); -- Study 3 parent is Drug B

-- Vizualisations
insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (35, 7, 12, 32, 20, 1); -- Visualisation Instance 1 belongs to Study 1

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (36, 8, 12, 32, 20, 1); -- Visualisation Instance 2 S1 belongs to Study 1

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (37, 9, 12, 33, 20, 1); -- Visualisation Instance 2 S2 belongs to Study 2 

insert into acl_object_identity (id, object_id_identity, object_id_class, parent_object, owner_sid, entries_inheriting)
values (38, 10, 12, 34, 20, 1); -- Visualisation Instance 3 belongs to Study 3 


-- ACE list
-- mask == V == 32
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(1, 30, 1, 20, 32, 1, 1, 1); -- User1 VISUALISATION on Drug A
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(2, 32, 1, 21, 32, 1, 1, 1); -- User2 VISUALISATION on Study 1
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(3, 35, 1, 21, 32, 0, 1, 1); /*
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
 */ --23423
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(4, 36, 1, 21, 32, 0, 1, 1); -- Remove User2 VISUALISATION on Vis 2 S1
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(5, 37, 1, 21, 32, 0, 1, 1); -- Remove User2 VISUALISATION on Vis 2 S2
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(6, 38, 1, 21, 32, 0, 1, 1); -- Remove User2 VISUALISATION on Vis 3
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(7, 38, 2, 22, 32, 1, 1, 1); -- User3 VISUALISATION on visualisation Instance 3


-- Cumulative Masks
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(8, 30, 2, 23, 96, 1, 1, 1); -- User4 VISUALISATION/EDIT_CONFIG on Drug A
insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
    values(9, 30, 3, 24, 192, 1, 1, 1); -- User5 EDIT_CONFIG/EDIT ROLES on Drug A
--insert into acl_entry(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
  --  values(4, 32, 2, 20, 32, 0, 1, 1); -- User user001 remove VISUALISATION on Clinical Study 1
