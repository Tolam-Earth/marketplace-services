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

drop table EsgOffsetAttributes;

create table ESGOFFSET
(
    ID            BIGINT auto_increment
        primary key,
    TOKEN_ID      VARCHAR(255) not null,
    SERIAL_NUMBER BIGINT       not null
);

create unique index ESGOFFSET_NFT_INDEX
    on ESGOFFSET (TOKEN_ID, SERIAL_NUMBER);

create table ESGOFFSETATTRIBUTE
(
    ID            BIGINT auto_increment
        primary key,
    ESG_OFFSET_ID BIGINT       not null,
    TITLE         VARCHAR(255) not null,
    DESCRIPTION   VARCHAR(255) not null,
    "VALUE"         VARCHAR(255) not null,
    "TYPE"          VARCHAR(255) not null,
    SCHEMA_CID    VARCHAR(255) not null,
    SCHEMA_NAME   VARCHAR(255) not null,
    constraint ESGOFFSETATTRIBUTE_ESGOFFSET_ID_FK
        foreign key (ESG_OFFSET_ID) references ESGOFFSET (ID)
);

create unique index ESGOFFSETATTRIBUTE_OFFSET_TITLE_INDEX
    on ESGOFFSETATTRIBUTE (ESG_OFFSET_ID, TITLE);
