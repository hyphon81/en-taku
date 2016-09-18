# create_tables_for_chatrooms

# --- !Ups
create table `rooms` (
  `roomNumber` binary(16) not null primary key,
  `roomName` varchar(191) not null, 
  `r18` boolean,
  `locked` boolean
);

create table `speechlogs` (
  `roomNumber` binary(16) not null,
  `accountId` binary(16) not null,
  `tabNumber` integer not null,
  `spokeTime` timestamp not null,
  `speechId`  binary(16) not null
);

create table `speeches` (
  `speechId` binary(16) not null primary key,
  `characterId` binary(16),
  `destinationTableId` binary(16),
  `speechText` text,
  `attachedIllustURL` varchar(254),
  `attachedBGMURL` varchar(254)
);

create table `tabtable` (
  `roomNumber` binary(16) not null,
  `tabNumber` integer not null,
  `tabName` varchar(191)
);

create table `activeusers` (
  `roomNumber` binary(16) not null,
  `accountId` binary(16) not null
);

create table `destinations` (
  `speechId` binary(16) not null,
  `accountId` binary(16) not null
);

create table `characters` (
  `characterId` binary(16) not null primary key,
  `characterName` varchar(191) not null,
  `accountId` binary(16) not null,
  `openCharacter` integer not null
);

create table `illusttable` (
  `characterId` binary(16) not null,
  `illustId` binary(16) not null
);

create table `illusts` (
  `illustId` binary(16) not null primary key,
  `accountId` binary(16) not null,
  `characterId` binary(16),
  `illustName` varchar(191),
  `illustURL` varchar(254) not null,
  `openIllust` integer not null
);

create table `status` (
  `characterId` binary(16) not null,
  `statusNumber` integer not null,
  `statusName` varchar(191),
  `isText` boolean,
  `textExpression` text,
  `isNumber` boolean,
  `numberExpression` integer
);

create table `history` (
  `historyId` binary(16) not null primary key,
  `roomNumber` binary(16),
  `accountId` binary(16),
  `characterId` binary(16),
  `commentAccountId` binary(16),
  `comment` text not null,
  `illustId` binary(16)
);


# --- !Downs

drop table `rooms`;
drop table `speechlogs`;
drop table `speeches`;
drop table `tabtable`;
drop table `activeusers`;
drop table `destinations`;
drop table `characters`;
drop table `illusttable`;
drop table `illusts`;
drop table `status`;
drop table `history`;
