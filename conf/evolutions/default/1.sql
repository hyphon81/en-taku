# create_en_taku_user

# --- !Ups
create table `en_taku_user` (
  `accountId` binary(16) not null primary key,
  `accountName` varchar(191) not null unique,
  `email` varchar(254),
  `userName` varchar(191),
  `avatarURL` varchar(254),
  `birthDay` date,
  `activated` boolean not null,
  `isAdmin` boolean not null,
  `changeableAccountName` boolean not null
);

create table `logininfo` (
  `id` bigint not null auto_increment primary key,
  `providerID` varchar(191) not null,
  `providerKey` varchar(191) not null,
  unique(`providerID`, `providerKey`)
);

create table `userlogininfo` (
  `accountId` binary(16) not null,
  `loginInfoId` bigint not null unique
);

create table `passwordinfo` (
  `hasher` varchar(254) not null,
  `password` varchar(254) not null,
  `salt` varchar(254),
  `loginInfoId` BIGINT not null
);

create table `oauth1info` (
  `id` bigint not null auto_increment primary key,
  `token` varchar(254) not null,
  `secret` varchar(254) not null,
  `loginInfoId` bigint not null
);

create table `oauth2info` (
  `id` bigint not null auto_increment primary key,
  `accesstoken` varchar(254) not null,
  `tokentype` varchar(254),
  `expiresin` integer,
  `refreshtoken` varchar(254),
  `logininfoid` bigint not null
);

create table `openidinfo` (
  `id` varchar(191) not null primary key,
  `logininfoid` bigint not null
);

create table `openidattributes` (
  `id` varchar(254) not null,
  `key` varchar(254) not null,
  `value` varchar(254) not null
);

# --- !Downs

drop table `openidattributes`;
drop table `openidinfo`;
drop table `oauth2info`;
drop table `oauth1info`;
drop table `passwordinfo`;
drop table `userlogininfo`;
drop table `logininfo`;
drop table `en_taku_user`;
