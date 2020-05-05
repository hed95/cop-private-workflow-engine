--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- 7.10.5 to 7.10.14
-- https://app.camunda.com/jira/browse/CAM-10275
create index ACT_IDX_HI_IDENT_LNK_TIMESTAMP on ACT_HI_IDENTITYLINK(TIMESTAMP_);

-- https://app.camunda.com/jira/browse/CAM-10616
create index ACT_IDX_HI_JOB_LOG_JOB_CONF on ACT_HI_JOB_LOG(JOB_DEF_CONFIGURATION_);

-- https://app.camunda.com/jira/browse/CAM-11117
drop index ACT_IDX_HI_ACT_INST_START;
create index ACT_IDX_HI_ACT_INST_START_END on ACT_HI_ACTINST(START_TIME_, END_TIME_);


-- 7.10 to 7.11
-- https://app.camunda.com/jira/browse/CAM-9920
ALTER TABLE ACT_HI_OP_LOG
    ADD CATEGORY_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
    ADD EXTERNAL_TASK_ID_ varchar(64);

create table ACT_GE_SCHEMA_LOG (
                                   ID_ varchar(64),
                                   TIMESTAMP_ timestamp,
                                   VERSION_ varchar(255),
                                   primary key (ID_)
);

insert into ACT_GE_SCHEMA_LOG
values ('0', CURRENT_TIMESTAMP, '7.11.0');

-- https://app.camunda.com/jira/browse/CAM-10129
create index ACT_IDX_HI_OP_LOG_USER_ID on ACT_HI_OP_LOG(USER_ID_);
create index ACT_IDX_HI_OP_LOG_OP_TYPE on ACT_HI_OP_LOG(OPERATION_TYPE_);
create index ACT_IDX_HI_OP_LOG_ENTITY_TYPE on ACT_HI_OP_LOG(ENTITY_TYPE_);


-- 7.11 to 7.12
insert into ACT_GE_SCHEMA_LOG
values ('100', CURRENT_TIMESTAMP, '7.12.0');

-- https://app.camunda.com/jira/browse/CAM-10665
ALTER TABLE ACT_HI_OP_LOG
    ADD ANNOTATION_ varchar(4000);

-- https://app.camunda.com/jira/browse/CAM-9855
ALTER TABLE ACT_RU_JOB
    ADD REPEAT_OFFSET_ bigint default 0;

-- https://app.camunda.com/jira/browse/CAM-10672
ALTER TABLE ACT_HI_INCIDENT
    ADD HISTORY_CONFIGURATION_ varchar(255);

-- https://app.camunda.com/jira/browse/CAM-10600
create index ACT_IDX_HI_DETAIL_VAR_INST_ID on ACT_HI_DETAIL(VAR_INST_ID_);


-- 7.12 to 7.13
insert into ACT_GE_SCHEMA_LOG
values ('200', CURRENT_TIMESTAMP, '7.13.0');

-- https://jira.camunda.com/browse/CAM-10953
create index ACT_IDX_HI_VAR_PI_NAME_TYPE on ACT_HI_VARINST(PROC_INST_ID_, NAME_, VAR_TYPE_);

-- https://app.camunda.com/jira/browse/CAM-10784
ALTER TABLE ACT_HI_JOB_LOG
    ADD HOSTNAME_ varchar(255) default null;

-- https://jira.camunda.com/browse/CAM-10378
ALTER TABLE ACT_RU_JOB
    ADD FAILED_ACT_ID_ varchar(255);

ALTER TABLE ACT_HI_JOB_LOG
    ADD FAILED_ACT_ID_ varchar(255);

ALTER TABLE ACT_RU_INCIDENT
    ADD FAILED_ACTIVITY_ID_ varchar(255);
