-- GYM Equipment
insert into equipment values ('Barbell', 10, 10);
insert into equipment values ('Yoga mat', 20, 20);
insert into equipment values ('Medicine ball', 20, 20);
insert into equipment values ('Exercise ball', 20, 20);
insert into equipment values ('Bench set', 5, 5);
insert into equipment values ('Bell weight', 15, 15);
insert into equipment values ('Rowing machine', 5, 5);
insert into equipment values ('Squat rack', 5, 5);
insert into equipment values ('Dumbbell', 20, 20);
insert into equipment values ('Resistance band', 30, 30);

-- Trainers
insert into trainer values (2, 'Brock', '(123)555-7777');
insert into trainer values (3, 'Alexis Mann', '(123)555-8888');
insert into trainer values (4, 'Ronda Rousey', '(123)555-9999');
insert into trainer values (5, 'Harrison Dyer', '(123)555-1111');
insert into trainer values (6, 'Kylie Christian', '(123)555-1234');

-- Members
insert into member values (1, 'Christine Ballard', '(234)543-2412', 'Regular', 0);
insert into member values (2, 'Ellie Bauer', '(133)432-4543', 'Regular', 0);
insert into member values (3, 'Julian Webster', '(124)654-2542', 'Regular', 0);
insert into member values (4, 'Caiden Beard', '(534)122-4222', 'Regular', 0);
insert into member values (5, 'Pauline Patterson', '(432)654-3345', 'Regular', 0);
insert into member values (6, 'Tessa Mendez', '(432)534-2433', 'Regular', 0);
insert into member values (7, 'Alyssa Salas', '(432)888-6685', 'Regular', 0);
insert into member values (8, 'Carolyn Frederick', '(566)723-6633', 'Regular', 0);
insert into member values (9, 'Alana Cohen', '(112)211-4122', 'Regular', 0);
insert into member values (10, 'Nikodem Kramer', '(124)112-2211', 'Regular', 0);
insert into member values (11, 'Tiana Duffy', '(412)453-5533', 'Regular', 0);
insert into member values (12, 'Estelle Chase', '(444)111-1111', 'Regular', 0);
insert into member values (13, 'Zaynah Rocha', '(124)754-3322', 'Regular', 0);
insert into member values (14, 'Wendy Villanueva', '(555)474-1234', 'Regular', 0);
insert into member values (15, 'Leland Wilcox', '(543)754-5555', 'Regular', 0);

-- PackageMembers and Transactions
-- Christine buys the strength package with a card.
insert into transaction values (1, (select id from member where name = 'Christine Ballard'), (select price from package where name = 'Strength'), CURRENT_DATE, 'FINISHED', 'CARD');
insert into packagemembers values ('Strength', (select id from member where name = 'Christine Ballard'));
update member set totalspending = totalspending + (select price from package where name = 'Strength') where name = 'Christine Ballard';
update course set currentparticipants = currentparticipants + 1 where name in (select coursename from packagecourse where packagename = 'Strength');

-- Caiden buys the junior package with straight cash.
insert into transaction values (2, (select id from member where name = 'Caiden Beard'), (select price from package where name = 'Junior'), CURRENT_DATE, 'FINISHED', 'CASH');
insert into packagemembers values ('Junior', (select id from member where name = 'Caiden Beard'));
update member set totalspending = totalspending + (select price from package where name = 'Junior') where name = 'Caiden Beard';
update course set currentparticipants = currentparticipants + 1 where name in (select coursename from packagecourse where packagename = 'Junior');

-- Julian buys the senior package with straight cash.
insert into transaction values (3, (select id from member where name = 'Julian Webster'), (select price from package where name = 'Senior'), CURRENT_DATE, 'FINISHED', 'CASH');
insert into packagemembers values ('Senior', (select id from member where name = 'Julian Webster'));
update member set totalspending = totalspending + (select price from package where name = 'Senior') where name = 'Julian Webster';
update course set currentparticipants = currentparticipants + 1 where name in (select coursename from packagecourse where packagename = 'Senior');

-- Alyssa buys the strength package with a check.
insert into transaction values (4, (select id from member where name = 'Alyssa Salas'), (select price from package where name = 'Strength'), CURRENT_DATE, 'FINISHED', 'CHECK');
insert into packagemembers values ('Strength', (select id from member where name = 'Alyssa Salas'));
update member set totalspending = totalspending + (select price from package where name = 'Strength') where name = 'Alyssa Salas';
update course set currentparticipants = currentparticipants + 1 where name in (select coursename from packagecourse where packagename = 'Strength');

-- Leland Wilcox buys the strength package with a check.
insert into transaction values (5, (select id from member where name = 'Leland Wilcox'), (select price from package where name = 'Strength'), TO_DATE('2023-11-01', 'YYYY-MM-DD'), 'DUE', 'CHECK');
insert into packagemembers values ('Strength', (select id from member where name = 'Leland Wilcox'));
update course set CURRENTPARTICIPANTS = CURRENTPARTICIPANTS + 1 WHERE name = 'Strength 001'; 
update course set CURRENTPARTICIPANTS = CURRENTPARTICIPANTS + 1 WHERE name = 'Strength 002'; 
update member set totalspending = totalspending + (select price from package where name = 'Strength') where name = 'Leland Wilcox';

-- Alana Cohen buys the junior package with a card.
insert into transaction values (6, (select id from member where name = 'Alana Cohen'), (select price from package where name = 'Junior'), TO_DATE('2023-12-01', 'YYYY-MM-DD'), 'DUE', 'CARD');
insert into packagemembers values ('Junior', (select id from member where name = 'Alana Cohen'));
update course set CURRENTPARTICIPANTS = CURRENTPARTICIPANTS + 1 WHERE name = 'Yoga 001'; 
update course set CURRENTPARTICIPANTS = CURRENTPARTICIPANTS + 1 WHERE name = 'Strength 001'; 
update member set totalspending = totalspending + (select price from package where name = 'Junior') where name = 'Alana Cohen';

-- Borrows
-- Alana borrows a yoga mat for 1 day.
update equipment set available = available - 1 where name = 'Yoga mat';
insert into borrow values ((select id from member where name = 'Alana Cohen'), 'Yoga mat', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '1' day);

-- Tessa borrows a Rowing machine for 1 hour.
update equipment set available = available - 1 where name = 'Rowing machine';
insert into borrow values ((select id from member where name = 'Tessa Mendez'), 'Rowing machine', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '1' hour);

-- Zaynah borrows a Squat rack for 30 minutes.
update equipment set available = available - 1 where name = 'Squat rack';
insert into borrow values ((select id from member where name = 'Zaynah Rocha'), 'Squat rack', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '30' minute);

-- Nikodem borrows a Resistance band for 2 days.
update equipment set available = available - 1 where name = 'Resistance band';
insert into borrow values ((select id from member where name = 'Nikodem Kramer'), 'Resistance band', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '2' day);

-- Leland borrows a Dumbbell for 2 hours.
update equipment set available = available - 1 where name = 'Dumbbell';
insert into borrow values ((select id from member where name = 'Leland Wilcox'), 'Dumbbell', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '2' hour);
