# create_tables_for_user_schedules

# --- !Ups

create table `userschedule` (
  `accountId` binary(16) not null,
  `scheduleId` binary(16) not null,
  `noticeMail` boolean not null
);

create table `schedule` (
  `scheduleId` binary(16) not null primary key,
  `scheduleName` varchar(191),
  `startTime` timestamp not null,
  `endTime` timestamp not null,
  `isFree` boolean,
  `isNotFree` boolean
);

# --- !Downs

drop table `userschedule`;
drop table `schedule`;
