-- This script creates all of the tables for the database.

create table Trainer (
    id integer primary key,
    name varchar(64),
    phone varchar(22) not null constraint TRAINER_PHONE_CHECK check ( regexp_like(phone, '^(\+\d\s?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}$') )
);

-- Level was renamed to Levels to avoid oracle db error.
create table Levels (
    name varchar(7) not null constraint LEVEL_CHECK check ( name in ('Regular', 'Gold', 'Diamond') ) primary key,
    discount integer check (discount >= 0 and discount <= 100), -- Percentage
    minSpendingAmount numeric
);

create table Member (
    id integer not null primary key,
    name varchar(64) not null,
    phone varchar(22) not null constraint MEMBER_PHONE_CHECK check ( regexp_like(phone, '^(\+\d\s?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}$') ),
    membershipLevel varchar(7) not null constraint MEMBERSHIP_CHECK check ( membershipLevel in ('Regular', 'Gold', 'Diamond') ),
    totalSpending numeric,
    foreign key (membershipLevel) references Levels(name)
);

-- Oracle does not support time type. >:(
-- todo need feedback on time variables.
create table Course (
    name varchar(64) primary key,
    trainerID integer,
    weeklyClassTime varchar(9) constraint WEEKDAY_CHECK check ( weeklyClassTime in ('monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday') ),
    -- duration float, -- fixme: why is this a float? or even needed?
    startDate date,
    endDate date,
    startTime integer constraint START_MILITARY_TIME check ( startTime >= 0 and startTime <= 2359 and MOD(startTime, 100) < 60 ), -- Military time in a 4 digit integer format.
    endTime integer constraint END_MILITARY_TIME check ( endTime >= 0 and endTime <= 2359 and MOD(endTime, 100) < 60 ), -- Military time in a 4 digit integer format.
    currentParticipants integer,
    maxParticipants integer,
    foreign key (trainerID) references Trainer(id)
);

create table Package (
    name varchar(64) primary key,
    price numeric
);

create table PackageCourse (
    packageName varchar(64),
    courseName varchar(64),
    primary key (packageName, courseName),
    foreign key (packageName) references Package(name),
    foreign key (courseName) references Course(name)
);

create table PackageMembers (
    packageName varchar(64),
    memberId integer,
    primary key (packageName, memberId),
    foreign key (packageName) references Package(name),
    foreign key (memberId) references Member(id)
);

create table Equipment (
    name varchar(64) primary key,
    available integer check ( available >= 0 ),
    quantity integer check ( quantity > 0 ),
    check ( quantity >= available )
);

create table Borrow (
    memberId integer,
    equipmentName varchar(64),
    checkoutTime timestamp,
    returnTime timestamp,
    primary key (memberId, equipmentName, checkoutTime),
    foreign key (memberId) references Member(id),
    foreign key (equipmentName) references Equipment(name)
);

create table Transaction (
    id integer primary key,
    memberID integer,
    amount numeric,
    transactionDate date, -- renamed date to transactionDate to avoid oracle db error.
    transactionStatus varchar(10) constraint TRANS_STATUS check ( transactionStatus in ('DUE', 'PROCESSING', 'FINISHED')),
    transactionType varchar(5) constraint TRANS_TYPE check ( transactionType in ('CASH', 'CARD', 'CHECK'))
);
