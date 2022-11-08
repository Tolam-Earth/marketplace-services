/*
 * Copyright 2022 Tolam Earth
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

alter table ESGOFFSETATTRIBUTE add column value_temp varchar;
update ESGOFFSETATTRIBUTE set ESGOFFSETATTRIBUTE.value_temp = ESGOFFSETATTRIBUTE.VALUE;
alter table ESGOFFSETATTRIBUTE drop column VALUE;
alter table ESGOFFSETATTRIBUTE add column "VALUE" varchar;
update ESGOFFSETATTRIBUTE set ESGOFFSETATTRIBUTE.VALUE = ESGOFFSETATTRIBUTE.value_temp;
alter table ESGOFFSETATTRIBUTE alter column "VALUE" set not null;
alter table ESGOFFSETATTRIBUTE drop column value_temp;

alter table ESGOFFSETATTRIBUTE alter column SCHEMA_CID set null;
alter table ESGOFFSETATTRIBUTE alter column SCHEMA_NAME set null;