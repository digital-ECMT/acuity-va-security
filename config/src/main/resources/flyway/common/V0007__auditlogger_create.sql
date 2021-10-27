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

CREATE TABLE LOG_ARG (
    LOG_ARG_ID NUMBER(19) NOT NULL, 
    LOG_ARG_NAME VARCHAR2(255) NOT NULL, 
    LOG_ARG_STRING_VALUE VARCHAR2(255) NULL, 
    LOG_ARG_DATE_VALUE TIMESTAMP NULL, 
    LOG_ARG_LONG_VALUE NUMBER(19) NULL, 
    LOG_ARG_FLOAT_VALUE NUMBER(19,4) NULL, 
    IS_LOG_RETURNED_ERROR NUMBER(1) default 0 NOT NULL,   
    LOG_OPERATION_ID NUMBER(19) NOT NULL, 
    PRIMARY KEY (LOG_ARG_ID)
);

CREATE TABLE LOG_OPERATION (
    LOG_OPERATION_ID NUMBER(19) NOT NULL, 
    DATECREATED TIMESTAMP NULL, 
    LOG_NAME VARCHAR2(255) NULL, 
    OWNER VARCHAR2(255) NOT NULL, 
    PACKAGE_AND_METHOD_NAME VARCHAR2(255) NOT NULL, 
    SESSION_ID varchar2(50) default 'NONE' not null,
    PRIMARY KEY (LOG_OPERATION_ID)
);

ALTER TABLE LOG_ARG ADD CONSTRAINT FK_LOG_ARG_LOG_ID FOREIGN KEY (LOG_OPERATION_ID) REFERENCES LOG_OPERATION (LOG_OPERATION_ID);
CREATE SEQUENCE LOG_ARG_SEQ START WITH 1000;
CREATE SEQUENCE LOG_OPERATION_SEQ START WITH 1000;
